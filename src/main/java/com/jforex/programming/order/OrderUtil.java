package com.jforex.programming.order;

import static com.jforex.programming.order.OrderStaticUtil.instrumentFromOrders;
import static com.jforex.programming.order.event.OrderEventTypeSets.createEvents;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dukascopy.api.IOrder;
import com.dukascopy.api.Instrument;
import com.jforex.programming.misc.IEngineUtil;
import com.jforex.programming.misc.JForexUtil;
import com.jforex.programming.order.command.CloseCommand;
import com.jforex.programming.order.command.MergeCommand;
import com.jforex.programming.order.command.OrderUtilCommand;
import com.jforex.programming.order.command.SetAmountCommand;
import com.jforex.programming.order.command.SetGTTCommand;
import com.jforex.programming.order.command.SetLabelCommand;
import com.jforex.programming.order.command.SetOpenPriceCommand;
import com.jforex.programming.order.command.SetSLCommand;
import com.jforex.programming.order.command.SetTPCommand;
import com.jforex.programming.order.command.SubmitCommand;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.position.Position;
import com.jforex.programming.position.PositionFactory;
import com.jforex.programming.position.PositionOrders;
import com.jforex.programming.settings.PlatformSettings;

import rx.Completable;
import rx.Observable;

public class OrderUtil {

    private final OrderUtilHandler orderUtilHandler;
    private final PositionFactory positionFactory;
    private final IEngineUtil engineUtil;

    public static final PlatformSettings platformSettings = JForexUtil.platformSettings;
    private static final Logger logger = LogManager.getLogger(OrderUtil.class);

    public OrderUtil(final OrderUtilHandler orderUtilHandler,
                     final PositionFactory positionFactory,
                     final IEngineUtil engineUtil) {
        this.orderUtilHandler = orderUtilHandler;
        this.positionFactory = positionFactory;
        this.engineUtil = engineUtil;
    }

    public final <T extends OrderUtilCommand> List<T> createBatchCommands(final Set<IOrder> orders,
                                                                          final Function<IOrder, T> commandCreator) {
        return orders
            .stream()
            .map(order -> commandCreator.apply(order))
            .collect(Collectors.toList());
    }

    public final Completable batchCompletable(final List<? extends OrderUtilCommand> batchCommands) {
        return Completable.merge(commandsToCompletables(batchCommands));
    }

    public final List<Completable> commandsToCompletables(final List<? extends OrderUtilCommand> commands) {
        return commands
            .stream()
            .map(OrderUtilCommand::completable)
            .collect(Collectors.toList());
    }

    public final Completable batchCompletable(final Set<IOrder> orders,
                                              final Function<IOrder, ? extends OrderUtilCommand> commandCreator) {
        return batchCompletable(createBatchCommands(orders, commandCreator));
    }

    public Completable commandSequence(final List<? extends OrderUtilCommand> commands) {
        return Completable.concat(commandsToCompletables(commands));
    }

    @SafeVarargs
    public final <T extends OrderUtilCommand> Completable commandSequence(final T... commands) {
        return commandSequence(Arrays.asList(commands));
    }

    public final Completable closePosition(final Instrument instrument,
                                           final Function<IOrder, CloseCommand> closeCreator) {
        final Position position = position(instrument);
        final Set<IOrder> ordersToClose = position.filledOrOpened();

        return batchCompletable(ordersToClose, closeCreator);
    }

    public final SubmitCommand.Option submitBuilder(final OrderParams orderParams) {
        return SubmitCommand.create(orderParams,
                                    engineUtil,
                                    this::submitOrder);
    }

    public final Completable submitOrder(final SubmitCommand command) {
        final OrderParams orderParams = command.orderParams();
        final Instrument instrument = orderParams.instrument();
        final String orderLabel = orderParams.label();

        return orderUtilHandler.callObservable(command)
            .doOnSubscribe(() -> logger.info("Start submit task with label " + orderLabel + " for " + instrument))
            .doOnError(e -> logger.error("Submit task with label " + orderLabel
                    + " for " + instrument + " failed!Exception: " + e.getMessage()))
            .doOnCompleted(() -> logger.info("Submit task with label " + orderLabel
                    + " for " + instrument + " was successful."))
            .doOnNext(this::addOrderToPosition)
            .toCompletable();
    }

    public final Completable mergeOrders(final MergeCommand command) {
        final String mergeOrderLabel = command.mergeOrderLabel();
        final Set<IOrder> toMergeOrders = command.toMergeOrders();

        return toMergeOrders.size() < 2
                ? Completable.complete()
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
                            + instrumentFromOrders(toMergeOrders) + " failed! Exception: " + e.getMessage()))
                    .toCompletable();
    }

    public final MergeCommand.Option mergeBuilder(final String mergeOrderLabel,
                                                  final Set<IOrder> toMergeOrders) {
        return MergeCommand.create(mergeOrderLabel,
                                   toMergeOrders,
                                   engineUtil,
                                   this::mergeOrders);
    }

    public final CloseCommand.Option closeBuilder(final IOrder orderToClose) {
        return CloseCommand.create(orderToClose,
                                   orderUtilHandler,
                                   this);
    }

    public final SetLabelCommand.Option setLabelBuilder(final IOrder order,
                                                        final String newLabel) {
        return SetLabelCommand.create(order,
                                      newLabel,
                                      orderUtilHandler);
    }

    public final SetGTTCommand.Option setGTTBuilder(final IOrder order,
                                                    final long newGTT) {
        return SetGTTCommand.create(order,
                                    newGTT,
                                    orderUtilHandler);
    }

    public final SetAmountCommand.Option setAmountBuilder(final IOrder order,
                                                          final double newAmount) {
        return SetAmountCommand.create(order,
                                       newAmount,
                                       orderUtilHandler);
    }

    public final SetOpenPriceCommand.Option setOpenPriceBuilder(final IOrder order,
                                                                final double newPrice) {
        return SetOpenPriceCommand.create(order,
                                          newPrice,
                                          orderUtilHandler);
    }

    public final SetSLCommand.Option setSLBuilder(final IOrder order,
                                                  final double newSL) {
        return SetSLCommand.create(order,
                                   newSL,
                                   orderUtilHandler);
    }

    public final SetTPCommand.Option setTPBuilder(final IOrder order,
                                                  final double newTP) {
        return SetTPCommand.create(order,
                                   newTP,
                                   orderUtilHandler);
    }

    public PositionOrders positionOrders(final Instrument instrument) {
        return positionFactory.forInstrument(instrument);
    }

    public Position position(final Collection<IOrder> orders) {
        return position(instrumentFromOrders(orders));
    }

    public Position position(final Instrument instrument) {
        return (Position) positionOrders(instrument);
    }

    public void addOrderToPosition(final OrderEvent orderEvent) {
        if (createEvents.contains(orderEvent.type())) {
            final IOrder order = orderEvent.order();
            position(order.getInstrument()).addOrder(order);
        }
    }
}
