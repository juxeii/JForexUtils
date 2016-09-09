package com.jforex.programming.order;

import static com.jforex.programming.order.OrderStaticUtil.instrumentFromOrders;
import static com.jforex.programming.order.OrderStaticUtil.isAmountSetTo;
import static com.jforex.programming.order.OrderStaticUtil.isClosed;
import static com.jforex.programming.order.OrderStaticUtil.isGTTSetTo;
import static com.jforex.programming.order.OrderStaticUtil.isLabelSetTo;
import static com.jforex.programming.order.OrderStaticUtil.isOpenPriceSetTo;
import static com.jforex.programming.order.OrderStaticUtil.isSLSetTo;
import static com.jforex.programming.order.OrderStaticUtil.isTPSetTo;

import java.util.Collection;
import java.util.Set;
import java.util.function.Function;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dukascopy.api.IOrder;
import com.dukascopy.api.Instrument;
import com.jforex.programming.misc.IEngineUtil;
import com.jforex.programming.order.command.CloseCommand;
import com.jforex.programming.order.command.MergeCommand;
import com.jforex.programming.order.command.SetAmountCommand;
import com.jforex.programming.order.command.SetGTTCommand;
import com.jforex.programming.order.command.SetLabelCommand;
import com.jforex.programming.order.command.SetOpenPriceCommand;
import com.jforex.programming.order.command.SetSLCommand;
import com.jforex.programming.order.command.SetTPCommand;
import com.jforex.programming.order.command.SubmitCommand;
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
import com.jforex.programming.position.PositionUtil;

import rx.Completable;
import rx.Observable;

public class OrderUtilImpl implements OrderUtil {

    private final OrderUtilHandler orderUtilHandler;
    private final PositionUtil positionUtil;
    private final IEngineUtil engineUtil;

    private static final Logger logger = LogManager.getLogger(OrderUtilImpl.class);

    public OrderUtilImpl(final OrderUtilHandler orderUtilHandler,
                         final PositionFactory positionFactory,
                         final IEngineUtil engineUtil) {
        this.orderUtilHandler = orderUtilHandler;
        this.positionUtil = positionFactory.positionUtil(this, positionFactory);
        this.engineUtil = engineUtil;
    }

    @Override
    public final SubmitOption submitBuilder(final OrderParams orderParams) {
        return SubmitCommand.create(orderParams,
                                    engineUtil,
                                    this::submitOrder);
    }

    public final Completable submitOrder(final SubmitCommand command) {
        return Completable.defer(() -> {
            final OrderParams orderParams = command.orderParams();
            final Instrument instrument = orderParams.instrument();
            final String orderLabel = orderParams.label();
            return orderUtilHandler.callObservable(command)
                .doOnSubscribe(() -> logger.info("Start submit task with label " + orderLabel + " for " + instrument))
                .doOnError(e -> logger.error("Submit task with label " + orderLabel
                        + " for " + instrument + " failed!Exception: " + e.getMessage()))
                .doOnCompleted(() -> logger.info("Submit task with label " + orderLabel
                        + " for " + instrument + " was successful."))
                .doOnNext(positionUtil::addOrderOfEventToPosition)
                .toCompletable();
        });
    }

    @Override
    public final MergeOption mergeBuilder(final String mergeOrderLabel,
                                          final Set<IOrder> toMergeOrders) {
        return MergeCommand.create(mergeOrderLabel,
                                   toMergeOrders,
                                   engineUtil,
                                   this::mergeOrders);
    }

    public final Completable mergeOrders(final MergeCommand command) {
        return Completable.defer(() -> {
            final String mergeOrderLabel = command.mergeOrderLabel();
            final Set<IOrder> toMergeOrders = command.toMergeOrders();
            return toMergeOrders.size() < 2
                    ? Completable.complete()
                    : Observable
                        .just(toMergeOrders)
                        .doOnSubscribe(() -> positionOfOrders(toMergeOrders).markOrdersActive(toMergeOrders))
                        .flatMap(orders -> orderUtilHandler.callObservable(command))
                        .doOnNext(positionUtil::addOrderOfEventToPosition)
                        .doOnTerminate(() -> positionOfOrders(toMergeOrders).markOrdersIdle(toMergeOrders))
                        .doOnSubscribe(() -> logger.info("Starting to merge with label " + mergeOrderLabel
                                + " for position " + instrumentFromOrders(toMergeOrders) + "."))
                        .doOnCompleted(() -> logger.info("Merging with label " + mergeOrderLabel
                                + " for position " + instrumentFromOrders(toMergeOrders) + " was successful."))
                        .doOnError(e -> logger.error("Merging with label " + mergeOrderLabel + " for position "
                                + instrumentFromOrders(toMergeOrders) + " failed! Exception: " + e.getMessage()))
                        .toCompletable();
        });
    }

    @Override
    public final CloseOption closeBuilder(final IOrder orderToClose) {
        return CloseCommand.create(orderToClose, this::close);
    }

    public final Completable close(final CloseCommand command) {
        return Completable.defer(() -> {
            final IOrder orderToClose = command.order();
            final Instrument instrument = orderToClose.getInstrument();
            final Position position = positionUtil.position(instrument);
            final String label = orderToClose.getLabel();
            return Observable
                .just(orderToClose)
                .filter(order -> !isClosed.test(order))
                .doOnSubscribe(() -> position.markOrderActive(orderToClose))
                .doOnSubscribe(() -> logger.info("Start to close order " + label + " with instrument " + instrument))
                .flatMap(order -> orderUtilHandler.callObservable(command))
                .doOnError(e -> logger.error("Failed to close order " + label + "!Excpetion: " + e.getMessage()))
                .doOnCompleted(() -> logger.info("Closed order " + label + " with instrument " + instrument))
                .doOnTerminate(() -> position.markOrderIdle(orderToClose))
                .toCompletable();
        });
    }

    @Override
    public final SetLabelOption setLabelBuilder(final IOrder order,
                                                final String newLabel) {
        return SetLabelCommand.create(order,
                                      newLabel,
                                      this::setLabel);
    }

    public final Completable setLabel(final SetLabelCommand command) {
        return Completable.defer(() -> {
            final IOrder orderToSetLabel = command.order();
            final Instrument instrument = orderToSetLabel.getInstrument();
            final String label = orderToSetLabel.getLabel();
            final String newLabel = command.newLabel();
            return Observable
                .just(orderToSetLabel)
                .filter(order -> !isLabelSetTo(newLabel).test(order))
                .flatMap(order -> orderUtilHandler.callObservable(command))
                .doOnSubscribe(() -> logger.info("Start to change label from " + label + " to " + newLabel
                        + " for order " + label + " and instrument " + instrument))
                .doOnError(e -> logger.error("Failed to change label from " + label + " to " + newLabel
                        + " for order " + label + " and instrument " + instrument + "!Excpetion: " + e.getMessage()))
                .doOnCompleted(() -> logger.info("Changed label from " + label + " to " + newLabel
                        + " for order " + label + " and instrument " + instrument))
                .toCompletable();
        });
    }

    @Override
    public final SetGTTOption setGTTBuilder(final IOrder order,
                                            final long newGTT) {
        return SetGTTCommand.create(order,
                                    newGTT,
                                    this::setGTT);
    }

    public final Completable setGTT(final SetGTTCommand command) {
        return Completable.defer(() -> {
            final IOrder orderToSetGTT = command.order();
            final Instrument instrument = orderToSetGTT.getInstrument();
            final String label = orderToSetGTT.getLabel();
            final long currentGTT = orderToSetGTT.getGoodTillTime();
            final long newGTT = command.newGTT();
            return Observable
                .just(orderToSetGTT)
                .filter(order -> !isGTTSetTo(newGTT).test(order))
                .flatMap(order -> orderUtilHandler.callObservable(command))
                .doOnSubscribe(() -> logger.info("Start to change GTT from " + currentGTT + " to " + newGTT
                        + " for order " + label + " and instrument " + instrument))
                .doOnError(e -> logger.error("Failed to change GTT from " + currentGTT + " to " + newGTT
                        + " for order " + label + " and instrument " + instrument + "!Excpetion: " + e.getMessage()))
                .doOnCompleted(() -> logger.info("Changed GTT from " + currentGTT + " to " + newGTT
                        + " for order " + label + " and instrument " + instrument))
                .toCompletable();
        });
    }

    @Override
    public final SetAmountOption setAmountBuilder(final IOrder order,
                                                  final double newAmount) {
        return SetAmountCommand.create(order,
                                       newAmount,
                                       this::setAmount);
    }

    public final Completable setAmount(final SetAmountCommand command) {
        return Completable.defer(() -> {
            final IOrder orderToSetAmount = command.order();
            final Instrument instrument = orderToSetAmount.getInstrument();
            final String label = orderToSetAmount.getLabel();
            final double currentAmount = orderToSetAmount.getRequestedAmount();
            final double newAmount = command.newAmount();
            return Observable
                .just(orderToSetAmount)
                .filter(order -> !isAmountSetTo(newAmount).test(order))
                .flatMap(order -> orderUtilHandler.callObservable(command))
                .doOnSubscribe(() -> logger.info("Start to change amount from " + currentAmount + " to " + newAmount
                        + " for order " + label + " and instrument " + instrument))
                .doOnError(e -> logger.error("Failed to change amount from " + currentAmount + " to " + newAmount
                        + " for order " + label + " and instrument " + instrument + "!Excpetion: " + e.getMessage()))
                .doOnCompleted(() -> logger.info("Changed amount from " + currentAmount + " to " + newAmount
                        + " for order " + label + " and instrument " + instrument))
                .toCompletable();
        });
    }

    @Override
    public final SetOpenPriceOption setOpenPriceBuilder(final IOrder order,
                                                        final double newPrice) {
        return SetOpenPriceCommand.create(order,
                                          newPrice,
                                          this::setOpenPrice);
    }

    public final Completable setOpenPrice(final SetOpenPriceCommand command) {
        return Completable.defer(() -> {
            final IOrder orderToSetOpenPrice = command.order();
            final Instrument instrument = orderToSetOpenPrice.getInstrument();
            final String label = orderToSetOpenPrice.getLabel();
            final double currentOpenPrice = orderToSetOpenPrice.getOpenPrice();
            final double newOpenPrice = command.newOpenPrice();
            return Observable
                .just(orderToSetOpenPrice)
                .filter(order -> !isOpenPriceSetTo(newOpenPrice).test(order))
                .flatMap(order -> orderUtilHandler.callObservable(command))
                .doOnSubscribe(() -> logger.info("Start to change open price from " + currentOpenPrice + " to "
                        + newOpenPrice + " for order " + label + " and instrument " + instrument))
                .doOnError(e -> logger
                    .error("Failed to change open price from " + currentOpenPrice + " to " + newOpenPrice
                            + " for order " + label + " and instrument " + instrument + "!Excpetion: "
                            + e.getMessage()))
                .doOnCompleted(() -> logger.info("Changed open price from " + currentOpenPrice + " to " + newOpenPrice
                        + " for order " + label + " and instrument " + instrument))
                .toCompletable();
        });
    }

    @Override
    public final SetSLOption setSLBuilder(final IOrder order,
                                          final double newSL) {
        return SetSLCommand.create(order,
                                   newSL,
                                   this::setSL);
    }

    public final Completable setSL(final SetSLCommand command) {
        return Completable.defer(() -> {
            final IOrder orderToSetSL = command.order();
            final Instrument instrument = orderToSetSL.getInstrument();
            final String label = orderToSetSL.getLabel();
            final double currentSL = orderToSetSL.getStopLossPrice();
            final double newSL = command.newSL();
            return Observable
                .just(orderToSetSL)
                .filter(order -> !isSLSetTo(newSL).test(order))
                .flatMap(order -> orderUtilHandler.callObservable(command))
                .doOnSubscribe(() -> logger.info("Start to change SL from " + currentSL + " to " + newSL
                        + " for order " + label + " and instrument " + instrument))
                .doOnError(e -> logger.error("Failed to change SL from " + currentSL + " to " + newSL
                        + " for order " + label + " and instrument " + instrument + "!Excpetion: " + e.getMessage()))
                .doOnCompleted(() -> logger.info("Changed SL from " + currentSL + " to " + newSL
                        + " for order " + label + " and instrument " + instrument))
                .toCompletable();
        });
    }

    @Override
    public final SetTPOption setTPBuilder(final IOrder order,
                                          final double newTP) {
        return SetTPCommand.create(order,
                                   newTP,
                                   this::setTP);
    }

    public final Completable setTP(final SetTPCommand command) {
        return Completable.defer(() -> {
            final IOrder orderToSetTP = command.order();
            final Instrument instrument = orderToSetTP.getInstrument();
            final String label = orderToSetTP.getLabel();
            final double currentTP = orderToSetTP.getTakeProfitPrice();
            final double newTP = command.newTP();
            return Observable
                .just(orderToSetTP)
                .filter(order -> !isTPSetTo(newTP).test(order))
                .flatMap(order -> orderUtilHandler.callObservable(command))
                .doOnSubscribe(() -> logger.info("Start to change TP from " + currentTP + " to " + newTP
                        + " for order " + label + " and instrument " + instrument))
                .doOnError(e -> logger.error("Failed to change TP from " + currentTP + " to " + newTP
                        + " for order " + label + " and instrument " + instrument + "!Excpetion: " + e.getMessage()))
                .doOnCompleted(() -> logger.info("Changed TP from " + currentTP + " to " + newTP
                        + " for order " + label + " and instrument " + instrument))
                .toCompletable();
        });
    }

    @Override
    public final Completable mergePosition(final Instrument instrument,
                                           final Function<Set<IOrder>, MergeCommand> mergeCommandFactory) {
        return positionUtil.mergePosition(instrument, mergeCommandFactory);
    }

    @Override
    public final Completable mergeAllPositions(final Function<Set<IOrder>, MergeCommand> mergeCommandFactory) {
        return positionUtil.mergeAllPositions(mergeCommandFactory);
    }

    @Override
    public final Completable closePosition(final Instrument instrument,
                                           final Function<Set<IOrder>, MergeCommand> mergeCommandFactory,
                                           final Function<IOrder, CloseCommand> closeCommandFactory) {
        return positionUtil.closePosition(instrument, mergeCommandFactory, closeCommandFactory);
    }

    @Override
    public final Completable closeAllPositions(final Function<Set<IOrder>, MergeCommand> mergeCommandFactory,
                                               final Function<IOrder, CloseCommand> closeCommandFactory) {
        return positionUtil.closeAllPositions(mergeCommandFactory, closeCommandFactory);
    }

    @Override
    public PositionOrders positionOrders(final Instrument instrument) {
        return positionUtil.positionOrders(instrument);
    }

    private Position positionOfOrders(final Collection<IOrder> orders) {
        return positionUtil.position(instrumentFromOrders(orders));
    }
}
