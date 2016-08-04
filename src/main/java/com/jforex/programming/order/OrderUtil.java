package com.jforex.programming.order;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.jforex.programming.order.event.OrderEventTypeSets.createEvents;

import java.util.Collection;
import java.util.Set;

import org.aeonbits.owner.ConfigFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dukascopy.api.IEngine;
import com.dukascopy.api.IOrder;
import com.dukascopy.api.Instrument;
import com.jforex.programming.order.command.CloseCommand;
import com.jforex.programming.order.command.MergeCommand;
import com.jforex.programming.order.command.OrderCallCommand;
import com.jforex.programming.order.command.OrderChangeCommand;
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

import rx.Observable;

public class OrderUtil {

    private final IEngine engine;
    private final PositionFactory positionFactory;
    private final OrderUtilHandler orderUtilHandler;

    private static final PlatformSettings platformSettings = ConfigFactory.create(PlatformSettings.class);
    private static final Logger logger = LogManager.getLogger(OrderUtil.class);

    public OrderUtil(final IEngine engine,
                     final PositionFactory positionFactory,
                     final OrderUtilHandler orderUtilHandler) {
        this.engine = engine;
        this.positionFactory = positionFactory;
        this.orderUtilHandler = orderUtilHandler;
    }

    public PositionOrders positionOrders(final Instrument instrument) {
        return positionFactory.forInstrument(checkNotNull(instrument));
    }

    private Position position(final Collection<IOrder> orders) {
        return position(instrumentFromOrders(orders));
    }

    private Instrument instrumentFromOrders(final Collection<IOrder> orders) {
        return orders
                .iterator()
                .next()
                .getInstrument();
    }

    private Position position(final Instrument instrument) {
        return (Position) positionOrders(instrument);
    }

    public Observable<OrderEvent> submitOrder(final OrderParams orderParams) {
        checkNotNull(orderParams);

        return orderUtilObservable(new SubmitCommand(orderParams, engine))
                .doOnNext(this::addCreatedOrderToPosition);
    }

    private void addCreatedOrderToPosition(final OrderEvent orderEvent) {
        if (createEvents.contains(orderEvent.type())) {
            final IOrder order = orderEvent.order();
            position(order.getInstrument()).addOrder(order);
        }
    }

    public Observable<OrderEvent> submitAndMergePosition(final String mergeOrderLabel,
                                                         final OrderParams orderParams) {
        checkNotNull(mergeOrderLabel);
        checkNotNull(orderParams);

        return submitOrder(orderParams)
                .concatWith(mergePositionOrders(mergeOrderLabel, orderParams.instrument()));
    }

    public Observable<OrderEvent> mergeOrders(final String mergeOrderLabel,
                                              final Collection<IOrder> toMergeOrders) {
        checkNotNull(mergeOrderLabel);
        checkNotNull(toMergeOrders);

        return toMergeOrders.size() < 2
                ? Observable.empty()
                : Observable
                        .just(toMergeOrders)
                        .doOnSubscribe(() -> position(toMergeOrders).markOrdersActive(toMergeOrders))
                        .flatMap(this::removeTPSLObservable)
                        .toCompletable()
                        .andThen(orderUtilObservable(new MergeCommand(mergeOrderLabel, toMergeOrders, engine)))
                        .doOnNext(this::addCreatedOrderToPosition)
                        .doOnTerminate(() -> position(toMergeOrders).markOrdersIdle(toMergeOrders));
    }

    public Observable<OrderEvent> mergePositionOrders(final String mergeOrderLabel,
                                                      final Instrument instrument) {
        checkNotNull(mergeOrderLabel);
        checkNotNull(instrument);

        return mergeOrders(mergeOrderLabel, position(instrument).filled())
                .doOnSubscribe(() -> logger.info("Starting position merge for " +
                        instrument + " with label " + mergeOrderLabel))
                .doOnError(e -> logger.error("Position merge for " + instrument
                        + "  with label " + mergeOrderLabel + " failed!" +
                        "Exception: " + e.getMessage()))
                .doOnCompleted(() -> logger.info("Position merge for " + instrument
                        + "  with label " + mergeOrderLabel + " was successful."));
    }

    private Observable<OrderEvent> removeTPSLObservable(final Collection<IOrder> filledOrders) {
        final Instrument instrument = instrumentFromOrders(filledOrders);

        return Observable
                .from(filledOrders)
                .doOnSubscribe(() -> logger.info("Starting remove TPSL task for position "
                        + instrument))
                .flatMap(this::removeSingleTPSLObservable)
                .doOnError(e -> logger.error("Removing TPSL from " + instrument
                        + " failed! Exception: " + e.getMessage()))
                .doOnCompleted(() -> logger.info("Removing TPSL task from "
                        + instrument + " was successful."));
    }

    private final Observable<OrderEvent> removeSingleTPSLObservable(final IOrder orderToRemoveSLTP) {
        return setTakeProfitPrice(orderToRemoveSLTP, platformSettings.noSLPrice())
                .concatWith(setStopLossPrice(orderToRemoveSLTP, platformSettings.noTPPrice()));
    }

    public Observable<OrderEvent> closePosition(final Instrument instrument) {
        final Position position = position(checkNotNull(instrument));
        final Set<IOrder> ordersToClose = position.filledOrOpened();

        return ordersToClose.isEmpty()
                ? Observable.empty()
                : Observable
                        .from(ordersToClose)
                        .doOnSubscribe(() -> {
                            logger.info("Starting position close for " + instrument);
                            position.markOrdersActive(ordersToClose);
                        })
                        .flatMap(this::close)
                        .doOnTerminate(() -> position.markOrdersIdle(ordersToClose))
                        .doOnCompleted(() -> logger.info("Closing position "
                                + instrument + " was successful."))
                        .doOnError(e -> logger.error("Closing position " + instrument
                                + " failed! Exception: " + e.getMessage()));
    }

    public Observable<OrderEvent> close(final IOrder orderToClose) {
        return changeObservable(new CloseCommand(orderToClose));
    }

    public Observable<OrderEvent> setLabel(final IOrder orderToChangeLabel,
                                           final String newLabel) {
        return changeObservable(new SetLabelCommand(orderToChangeLabel, newLabel));
    }

    public Observable<OrderEvent> setGoodTillTime(final IOrder orderToChangeGTT,
                                                  final long newGTT) {
        return changeObservable(new SetGTTCommand(orderToChangeGTT, newGTT));
    }

    public Observable<OrderEvent> setOpenPrice(final IOrder orderToChangeOpenPrice,
                                               final double newOpenPrice) {
        return changeObservable(new SetOpenPriceCommand(orderToChangeOpenPrice, newOpenPrice));
    }

    public Observable<OrderEvent> setRequestedAmount(final IOrder orderToChangeAmount,
                                                     final double newRequestedAmount) {
        return changeObservable(new SetAmountCommand(orderToChangeAmount, newRequestedAmount));
    }

    public Observable<OrderEvent> setStopLossPrice(final IOrder orderToChangeSL,
                                                   final double newSL) {
        return changeObservable(new SetSLCommand(orderToChangeSL, newSL));
    }

    public Observable<OrderEvent> setTakeProfitPrice(final IOrder orderToChangeTP,
                                                     final double newTP) {
        return changeObservable(new SetTPCommand(orderToChangeTP, newTP));
    }

    private Observable<OrderEvent> changeObservable(final OrderChangeCommand<?> command) {
        return Observable
                .just(command)
                .filter(OrderChangeCommand::filter)
                .flatMap(this::orderUtilObservable);
    }

    private Observable<OrderEvent> orderUtilObservable(final OrderCallCommand command) {
        return orderUtilHandler.callObservable(command);
    }
}
