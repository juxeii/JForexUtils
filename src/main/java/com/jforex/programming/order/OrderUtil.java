package com.jforex.programming.order;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.jforex.programming.order.OrderStaticUtil.isAmountSetTo;
import static com.jforex.programming.order.OrderStaticUtil.isClosed;
import static com.jforex.programming.order.OrderStaticUtil.isGTTSetTo;
import static com.jforex.programming.order.OrderStaticUtil.isLabelSetTo;
import static com.jforex.programming.order.OrderStaticUtil.isOpenPriceSetTo;
import static com.jforex.programming.order.OrderStaticUtil.isSLSetTo;
import static com.jforex.programming.order.OrderStaticUtil.isTPSetTo;

import java.util.Collection;
import java.util.Set;

import org.aeonbits.owner.ConfigFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.jforex.programming.order.command.CloseCommand;
import com.jforex.programming.order.command.MergeCommand;
import com.jforex.programming.order.command.OrderCallCommand;
import com.jforex.programming.order.command.SetAmountCommand;
import com.jforex.programming.order.command.SetGTTCommand;
import com.jforex.programming.order.command.SetLabelCommand;
import com.jforex.programming.order.command.SetOpenPriceCommand;
import com.jforex.programming.order.command.SetSLCommand;
import com.jforex.programming.order.command.SetTPCommand;
import com.jforex.programming.order.command.SubmitCommand;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.event.OrderEventTypeSets;
import com.jforex.programming.position.Position;
import com.jforex.programming.position.PositionFactory;
import com.jforex.programming.position.PositionOrders;
import com.jforex.programming.settings.PlatformSettings;

import com.dukascopy.api.IEngine;
import com.dukascopy.api.IOrder;
import com.dukascopy.api.Instrument;

import rx.Observable;

public class OrderUtil {

    private final IEngine engine;
    private final PositionFactory positionFactory;
    private final OrderUtilHandler orderUtilHandler;

    private static final PlatformSettings platformSettings =
            ConfigFactory.create(PlatformSettings.class);
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

    public Observable<OrderEvent> submitOrder(final OrderParams orderParams) {
        checkNotNull(orderParams);

        return orderUtilObservable(new SubmitCommand(orderParams, engine))
                .doOnNext(this::addOrderToPositionIfDone);
    }

    private void addOrderToPositionIfDone(final OrderEvent orderEvent) {
        if (OrderEventTypeSets.createEventTypes.contains(orderEvent.type())) {
            final IOrder order = orderEvent.order();
            position(order.getInstrument()).addOrder(order);
        }
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
                        .doOnNext(this::addOrderToPositionIfDone)
                        .doOnTerminate(() -> position(toMergeOrders).markOrdersIdle(toMergeOrders));
    }

    private Position position(final Collection<IOrder> orders) {
        return position(orders
                .iterator()
                .next()
                .getInstrument());
    }

    private Position position(final Instrument instrument) {
        return (Position) positionOrders(instrument);
    }

    public Observable<OrderEvent> mergePositionOrders(final String mergeOrderLabel,
                                                      final Instrument instrument) {
        checkNotNull(mergeOrderLabel);
        checkNotNull(instrument);

        return mergeOrders(mergeOrderLabel, position(instrument).filled())
                .doOnSubscribe(() -> logger.debug("Starting position merge for " +
                        instrument + " with label " + mergeOrderLabel))
                .doOnError(e -> logger.error("Position merge for " + instrument
                        + "  with label " + mergeOrderLabel + " failed!" +
                        "Exception: " + e.getMessage()))
                .doOnCompleted(() -> logger.debug("Position merge for " + instrument
                        + "  with label " + mergeOrderLabel + " was successful."));
    }

    private Observable<OrderEvent> removeTPSLObservable(final Collection<IOrder> filledOrders) {
        final Instrument instrument = filledOrders.iterator().next().getInstrument();

        return Observable
                .from(filledOrders)
                .doOnSubscribe(() -> logger.debug("Starting remove TPSL task for position "
                        + instrument))
                .flatMap(this::removeSingleTPSLObservable)
                .doOnCompleted(() -> logger.debug("Removing TPSL task from "
                        + instrument + " was successful."))
                .doOnError(e -> logger.error("Removing TPSL from " + instrument
                        + " failed! Exception: " + e.getMessage()));
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
                            logger.debug("Starting position close for " + instrument);
                            position.markOrdersActive(ordersToClose);
                        })
                        .flatMap(this::close)
                        .doOnTerminate(() -> position.markOrdersIdle(ordersToClose))
                        .doOnCompleted(() -> logger.debug("Closing position "
                                + instrument + " was successful."))
                        .doOnError(e -> logger.error("Closing position " + instrument
                                + " failed! Exception: " + e.getMessage()));
    }

    public Observable<OrderEvent> close(final IOrder orderToClose) {
        return Observable
                .just(checkNotNull(orderToClose))
                .filter(order -> !isClosed.test(order))
                .flatMap(order -> orderUtilObservable(new CloseCommand(order)));
    }

    public Observable<OrderEvent> setLabel(final IOrder orderToChangeLabel,
                                           final String newLabel) {
        return Observable
                .just(checkNotNull(orderToChangeLabel))
                .filter(order -> !isLabelSetTo(newLabel).test(order))
                .flatMap(order -> orderUtilObservable(new SetLabelCommand(orderToChangeLabel, newLabel)));
    }

    public Observable<OrderEvent> setGoodTillTime(final IOrder orderToChangeGTT,
                                                  final long newGTT) {
        return Observable
                .just(checkNotNull(orderToChangeGTT))
                .filter(order -> !isGTTSetTo(newGTT).test(order))
                .flatMap(order -> orderUtilObservable(new SetGTTCommand(orderToChangeGTT, newGTT)));
    }

    public Observable<OrderEvent> setOpenPrice(final IOrder orderToChangeOpenPrice,
                                               final double newOpenPrice) {
        return Observable
                .just(checkNotNull(orderToChangeOpenPrice))
                .filter(order -> !isOpenPriceSetTo(newOpenPrice).test(order))
                .flatMap(order -> orderUtilObservable(new SetOpenPriceCommand(orderToChangeOpenPrice, newOpenPrice)));
    }

    public Observable<OrderEvent> setRequestedAmount(final IOrder orderToChangeAmount,
                                                     final double newRequestedAmount) {
        return Observable
                .just(checkNotNull(orderToChangeAmount))
                .filter(order -> !isAmountSetTo(newRequestedAmount).test(order))
                .flatMap(order -> orderUtilObservable(new SetAmountCommand(orderToChangeAmount, newRequestedAmount)));
    }

    public Observable<OrderEvent> setStopLossPrice(final IOrder orderToChangeSL,
                                                   final double newSL) {
        return Observable
                .just(checkNotNull(orderToChangeSL))
                .filter(order -> !isSLSetTo(newSL).test(order))
                .flatMap(order -> orderUtilObservable(new SetSLCommand(orderToChangeSL, newSL)));
    }

    public Observable<OrderEvent> setTakeProfitPrice(final IOrder orderToChangeTP,
                                                     final double newTP) {
        return Observable
                .just(checkNotNull(orderToChangeTP))
                .filter(order -> !isTPSetTo(newTP).test(order))
                .flatMap(order -> orderUtilObservable(new SetTPCommand(orderToChangeTP, newTP)));
    }

    private Observable<OrderEvent> orderUtilObservable(final OrderCallCommand command) {
        return orderUtilHandler.callObservable(command);
    }
}
