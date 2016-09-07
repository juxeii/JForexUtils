package com.jforex.programming.order;

import static com.jforex.programming.order.OrderStaticUtil.instrumentFromOrders;
import static com.jforex.programming.order.OrderStaticUtil.isAmountSetTo;
import static com.jforex.programming.order.OrderStaticUtil.isClosed;
import static com.jforex.programming.order.OrderStaticUtil.isGTTSetTo;
import static com.jforex.programming.order.OrderStaticUtil.isLabelSetTo;
import static com.jforex.programming.order.OrderStaticUtil.isOpenPriceSetTo;
import static com.jforex.programming.order.OrderStaticUtil.isSLSetTo;
import static com.jforex.programming.order.OrderStaticUtil.isTPSetTo;
import static com.jforex.programming.order.event.OrderEventTypeSets.createEvents;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dukascopy.api.IOrder;
import com.dukascopy.api.Instrument;
import com.jforex.programming.misc.IEngineUtil;
import com.jforex.programming.order.command.CloseCommand;
import com.jforex.programming.order.command.CommandUtil;
import com.jforex.programming.order.command.CommonCommand;
import com.jforex.programming.order.command.MergeCommand;
import com.jforex.programming.order.command.SetAmountCommand;
import com.jforex.programming.order.command.SetGTTCommand;
import com.jforex.programming.order.command.SetLabelCommand;
import com.jforex.programming.order.command.SetOpenPriceCommand;
import com.jforex.programming.order.command.SetSLCommand;
import com.jforex.programming.order.command.SetTPCommand;
import com.jforex.programming.order.command.SubmitCommand;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.process.option.CloseOption;
import com.jforex.programming.order.process.option.MergeOption;
import com.jforex.programming.order.process.option.SetAmountOption;
import com.jforex.programming.order.process.option.SetGTTOption;
import com.jforex.programming.order.process.option.SetLabelOption;
import com.jforex.programming.order.process.option.SetOpenPriceOption;
import com.jforex.programming.order.process.option.SetSLOption;
import com.jforex.programming.order.process.option.SetTPOption;
import com.jforex.programming.order.process.option.SubmitOption;
import com.jforex.programming.position.Position;
import com.jforex.programming.position.PositionFactory;
import com.jforex.programming.position.PositionOrders;

import rx.Completable;
import rx.Observable;

public class OrderUtilImpl implements OrderUtil {

    private final OrderUtilHandler orderUtilHandler;
    private final PositionFactory positionFactory;
    private final IEngineUtil engineUtil;

    private static final Logger logger = LogManager.getLogger(OrderUtilImpl.class);

    public OrderUtilImpl(final OrderUtilHandler orderUtilHandler,
                         final PositionFactory positionFactory,
                         final IEngineUtil engineUtil) {
        this.orderUtilHandler = orderUtilHandler;
        this.positionFactory = positionFactory;
        this.engineUtil = engineUtil;
    }

    public final SubmitOption submitBuilder(final OrderParams orderParams) {
        return SubmitCommand.create(orderParams,
                                    engineUtil,
                                    this::submitOrder);
    }

    public final Completable submitOrder(final SubmitCommand command) {
        final OrderParams orderParams = command.orderParams();
        final Instrument instrument = orderParams.instrument();
        final String orderLabel = orderParams.label();

        final Observable<OrderEvent> observable = Observable.defer(() -> orderUtilHandler.callObservable(command)
            .doOnSubscribe(() -> logger.info("Start submit task with label " + orderLabel + " for " + instrument))
            .doOnError(e -> logger.error("Submit task with label " + orderLabel
                    + " for " + instrument + " failed!Exception: " + e.getMessage()))
            .doOnCompleted(() -> logger.info("Submit task with label " + orderLabel
                    + " for " + instrument + " was successful."))
            .doOnNext(this::addOrderToPosition));

        return addHandlersFromCommand(command, observable);
    }

    public final MergeOption mergeBuilder(final String mergeOrderLabel,
                                          final Set<IOrder> toMergeOrders) {
        return MergeCommand.create(mergeOrderLabel,
                                   toMergeOrders,
                                   engineUtil,
                                   this::mergeOrders);
    }

    public final Completable mergeOrders(final MergeCommand command) {
        final String mergeOrderLabel = command.mergeOrderLabel();
        final Set<IOrder> toMergeOrders = command.toMergeOrders();

        final Observable<OrderEvent> observable = Observable.defer(() -> toMergeOrders.size() < 2
                ? Observable.empty()
                : Observable
                    .just(toMergeOrders)
                    .doOnSubscribe(() -> position(toMergeOrders).markOrdersActive(toMergeOrders))
                    .flatMap(orders -> orderUtilHandler.callObservable(command))
                    .doOnNext(this::addOrderToPosition)
                    .doOnTerminate(() -> position(toMergeOrders).markOrdersIdle(toMergeOrders))
                    .doOnSubscribe(() -> logger.info("Starting to merge with label " + mergeOrderLabel
                            + " for position " + instrumentFromOrders(toMergeOrders) + "."))
                    .doOnCompleted(() -> logger.info("Merging with label " + mergeOrderLabel
                            + " for position " + instrumentFromOrders(toMergeOrders) + " was successful."))
                    .doOnError(e -> logger.error("Merging with label " + mergeOrderLabel + " for position "
                            + instrumentFromOrders(toMergeOrders) + " failed! Exception: " + e.getMessage())));

        return addHandlersFromCommand(command, observable);
    }

    public final CloseOption closeBuilder(final IOrder orderToClose) {
        return CloseCommand.create(orderToClose, this::close);
    }

    public final Completable close(final CloseCommand command) {
        final IOrder orderToClose = command.order();
        final Instrument instrument = orderToClose.getInstrument();
        final Position position = position(instrument);
        final String label = orderToClose.getLabel();

        final Observable<OrderEvent> observable = Observable.defer(() -> Observable
            .just(orderToClose)
            .filter(order -> !isClosed.test(order))
            .doOnSubscribe(() -> position.markOrderActive(orderToClose))
            .doOnSubscribe(() -> logger.info("Start to close order " + label + " with instrument " + instrument))
            .flatMap(order -> orderUtilHandler.callObservable(command))
            .doOnError(e -> logger.error("Failed to close order " + label + "!Excpetion: " + e.getMessage()))
            .doOnCompleted(() -> logger.info("Closed order " + label + " with instrument " + instrument))
            .doOnTerminate(() -> position.markOrderIdle(orderToClose)));

        return addHandlersFromCommand(command, observable);
    }

    public final SetLabelOption setLabelBuilder(final IOrder order,
                                                final String newLabel) {
        return SetLabelCommand.create(order,
                                      newLabel,
                                      this::setLabel);
    }

    public final Completable setLabel(final SetLabelCommand command) {
        final IOrder orderToSetLabel = command.order();
        final Instrument instrument = orderToSetLabel.getInstrument();
        final String label = orderToSetLabel.getLabel();
        final String newLabel = command.newLabel();

        final Observable<OrderEvent> observable = Observable.defer(() -> Observable
            .just(orderToSetLabel)
            .filter(order -> !isLabelSetTo(newLabel).test(order))
            .flatMap(order -> orderUtilHandler.callObservable(command))
            .doOnSubscribe(() -> logger.info("Start to change label from " + label + " to " + newLabel
                    + " for order " + label + " and instrument " + instrument))
            .doOnError(e -> logger.error("Failed to change label from " + label + " to " + newLabel
                    + " for order " + label + " and instrument " + instrument + "!Excpetion: " + e.getMessage()))
            .doOnCompleted(() -> logger.info("Changed label from " + label + " to " + newLabel
                    + " for order " + label + " and instrument " + instrument)));

        return addHandlersFromCommand(command, observable);
    }

    public final SetGTTOption setGTTBuilder(final IOrder order,
                                            final long newGTT) {
        return SetGTTCommand.create(order,
                                    newGTT,
                                    this::setGTT);
    }

    public final Completable setGTT(final SetGTTCommand command) {
        final IOrder orderToSetGTT = command.order();
        final Instrument instrument = orderToSetGTT.getInstrument();
        final String label = orderToSetGTT.getLabel();
        final long currentGTT = orderToSetGTT.getGoodTillTime();
        final long newGTT = command.newGTT();

        final Observable<OrderEvent> observable = Observable.defer(() -> Observable
            .just(orderToSetGTT)
            .filter(order -> !isGTTSetTo(newGTT).test(order))
            .flatMap(order -> orderUtilHandler.callObservable(command))
            .doOnSubscribe(() -> logger.info("Start to change GTT from " + currentGTT + " to " + newGTT
                    + " for order " + label + " and instrument " + instrument))
            .doOnError(e -> logger.error("Failed to change GTT from " + currentGTT + " to " + newGTT
                    + " for order " + label + " and instrument " + instrument + "!Excpetion: " + e.getMessage()))
            .doOnCompleted(() -> logger.info("Changed GTT from " + currentGTT + " to " + newGTT
                    + " for order " + label + " and instrument " + instrument)));

        return addHandlersFromCommand(command, observable);
    }

    public final SetAmountOption setAmountBuilder(final IOrder order,
                                                  final double newAmount) {
        return SetAmountCommand.create(order,
                                       newAmount,
                                       this::setAmount);
    }

    public final Completable setAmount(final SetAmountCommand command) {
        final IOrder orderToSetAmount = command.order();
        final Instrument instrument = orderToSetAmount.getInstrument();
        final String label = orderToSetAmount.getLabel();
        final double currentAmount = orderToSetAmount.getRequestedAmount();
        final double newAmount = command.newAmount();

        final Observable<OrderEvent> observable = Observable.defer(() -> Observable
            .just(orderToSetAmount)
            .filter(order -> !isAmountSetTo(newAmount).test(order))
            .flatMap(order -> orderUtilHandler.callObservable(command))
            .doOnSubscribe(() -> logger.info("Start to change amount from " + currentAmount + " to " + newAmount
                    + " for order " + label + " and instrument " + instrument))
            .doOnError(e -> logger.error("Failed to change amount from " + currentAmount + " to " + newAmount
                    + " for order " + label + " and instrument " + instrument + "!Excpetion: " + e.getMessage()))
            .doOnCompleted(() -> logger.info("Changed amount from " + currentAmount + " to " + newAmount
                    + " for order " + label + " and instrument " + instrument)));

        return addHandlersFromCommand(command, observable);
    }

    public final SetOpenPriceOption setOpenPriceBuilder(final IOrder order,
                                                        final double newPrice) {
        return SetOpenPriceCommand.create(order,
                                          newPrice,
                                          this::setOpenPrice);
    }

    public final Completable setOpenPrice(final SetOpenPriceCommand command) {
        final IOrder orderToSetOpenPrice = command.order();
        final Instrument instrument = orderToSetOpenPrice.getInstrument();
        final String label = orderToSetOpenPrice.getLabel();
        final double currentOpenPrice = orderToSetOpenPrice.getOpenPrice();
        final double newOpenPrice = command.newOpenPrice();

        final Observable<OrderEvent> observable = Observable.defer(() -> Observable
            .just(orderToSetOpenPrice)
            .filter(order -> !isOpenPriceSetTo(newOpenPrice).test(order))
            .flatMap(order -> orderUtilHandler.callObservable(command))
            .doOnSubscribe(() -> logger.info("Start to change open price from " + currentOpenPrice + " to "
                    + newOpenPrice + " for order " + label + " and instrument " + instrument))
            .doOnError(e -> logger.error("Failed to change open price from " + currentOpenPrice + " to " + newOpenPrice
                    + " for order " + label + " and instrument " + instrument + "!Excpetion: " + e.getMessage()))
            .doOnCompleted(() -> logger.info("Changed open price from " + currentOpenPrice + " to " + newOpenPrice
                    + " for order " + label + " and instrument " + instrument)));

        return addHandlersFromCommand(command, observable);
    }

    public final SetSLOption setSLBuilder(final IOrder order,
                                          final double newSL) {
        return SetSLCommand.create(order,
                                   newSL,
                                   this::setSL);
    }

    public final Completable setSL(final SetSLCommand command) {
        final IOrder orderToSetSL = command.order();
        final Instrument instrument = orderToSetSL.getInstrument();
        final String label = orderToSetSL.getLabel();
        final double currentSL = orderToSetSL.getStopLossPrice();
        final double newSL = command.newSL();

        final Observable<OrderEvent> observable = Observable.defer(() -> Observable
            .just(orderToSetSL)
            .filter(order -> !isSLSetTo(newSL).test(order))
            .flatMap(order -> orderUtilHandler.callObservable(command))
            .doOnSubscribe(() -> logger.info("Start to change SL from " + currentSL + " to " + newSL
                    + " for order " + label + " and instrument " + instrument))
            .doOnError(e -> logger.error("Failed to change SL from " + currentSL + " to " + newSL
                    + " for order " + label + " and instrument " + instrument + "!Excpetion: " + e.getMessage()))
            .doOnCompleted(() -> logger.info("Changed SL from " + currentSL + " to " + newSL
                    + " for order " + label + " and instrument " + instrument)));

        return addHandlersFromCommand(command, observable);
    }

    public final SetTPOption setTPBuilder(final IOrder order,
                                          final double newTP) {
        return SetTPCommand.create(order,
                                   newTP,
                                   this::setTP);
    }

    public final Completable setTP(final SetTPCommand command) {
        final IOrder orderToSetTP = command.order();
        final Instrument instrument = orderToSetTP.getInstrument();
        final String label = orderToSetTP.getLabel();
        final double currentTP = orderToSetTP.getTakeProfitPrice();
        final double newTP = command.newTP();

        final Observable<OrderEvent> observable = Observable.defer(() -> Observable
            .just(orderToSetTP)
            .filter(order -> !isTPSetTo(newTP).test(order))
            .flatMap(order -> orderUtilHandler.callObservable(command))
            .doOnSubscribe(() -> logger.info("Start to change TP from " + currentTP + " to " + newTP
                    + " for order " + label + " and instrument " + instrument))
            .doOnError(e -> logger.error("Failed to change TP from " + currentTP + " to " + newTP
                    + " for order " + label + " and instrument " + instrument + "!Excpetion: " + e.getMessage()))
            .doOnCompleted(() -> logger.info("Changed TP from " + currentTP + " to " + newTP
                    + " for order " + label + " and instrument " + instrument)));

        return addHandlersFromCommand(command, observable);
    }

    public final Completable closePosition(final Instrument instrument,
                                           final Function<IOrder, CloseCommand> closeCreator) {
        final Position position = position(instrument);
        final Set<IOrder> ordersToClose = position.filledOrOpened();
        final List<CloseCommand> closeCommands = CommandUtil.createBatchCommands(ordersToClose, closeCreator);

        return CommandUtil.runCommands(closeCommands);
    }

    public final Completable closeAllPositions(final Function<IOrder, CloseCommand> closeCreator) {
        final List<Completable> completables = positionFactory
            .allPositions()
            .stream()
            .map(position -> closePosition(position.instrument(), closeCreator))
            .collect(Collectors.toList());

        return Completable.merge(completables);
    }

    public final Completable mergePosition(final Instrument instrument,
                                           final String mergeOrderLabel,
                                           final BiFunction<Set<IOrder>, String, MergeCommand> mergeCreator) {
        final Position position = position(instrument);

        return Completable.defer(() -> {
            final Set<IOrder> toMergeOrders = position.filled();
            if (toMergeOrders.size() < 2)
                return Completable.complete();
            final MergeCommand command = mergeCreator.apply(toMergeOrders, mergeOrderLabel);
            return mergeOrders(command);
//                .doOnSubscribe(s -> logger.info("Start to merge position for " + instrument))
//                .doOnError(e -> logger.error("Failed to merge position for " + instrument
//                        + "!Excpetion: " + e.getMessage()))
//                .doOnCompleted(() -> logger.info("Merged position for " + instrument));
        });
    }

    private final Completable addHandlersFromCommand(final CommonCommand command,
                                                     final Observable<OrderEvent> observable) {
        return observable
            .doOnSubscribe(command.startAction()::call)
            .doOnCompleted(command.completedAction()::call)
            .doOnError(command.errorAction()::accept)
            .toCompletable();
    }

    public PositionOrders positionOrders(final Instrument instrument) {
        return positionFactory.forInstrument(instrument);
    }

    private Position position(final Collection<IOrder> orders) {
        return position(instrumentFromOrders(orders));
    }

    private Position position(final Instrument instrument) {
        return (Position) positionOrders(instrument);
    }

    private final void addOrderToPosition(final OrderEvent orderEvent) {
        if (createEvents.contains(orderEvent.type())) {
            final IOrder order = orderEvent.order();
            position(order.getInstrument()).addOrder(order);
        }
    }
}
