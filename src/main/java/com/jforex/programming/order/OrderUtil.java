package com.jforex.programming.order;

import static com.google.common.base.Preconditions.checkNotNull;
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
import java.util.Set;
import java.util.concurrent.Callable;

import org.aeonbits.owner.ConfigFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dukascopy.api.IEngine;
import com.dukascopy.api.IOrder;
import com.dukascopy.api.Instrument;
import com.jforex.programming.misc.JFRunnable;
import com.jforex.programming.order.call.OrderCallCommand;
import com.jforex.programming.order.call.OrderCallReason;
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

    private Position position(final Instrument instrument) {
        return (Position) positionOrders(instrument);
    }

    public Observable<OrderEvent> submitOrder(final OrderParams orderParams) {
        checkNotNull(orderParams);

        final Instrument instrument = orderParams.instrument();
        final String orderLabel = orderParams.label();
        final Callable<IOrder> callable = () -> engine.submitOrder(orderParams.label(),
                                                                   orderParams.instrument(),
                                                                   orderParams.orderCommand(),
                                                                   orderParams.amount(),
                                                                   orderParams.price(),
                                                                   orderParams.slippage(),
                                                                   orderParams.stopLossPrice(),
                                                                   orderParams.takeProfitPrice(),
                                                                   orderParams.goodTillTime(),
                                                                   orderParams.comment());

        return orderUtilObservable(new OrderCallCommand(callable, OrderCallReason.SUBMIT))
            .doOnSubscribe(() -> logger.info("Start submit task with label " + orderLabel + " for " + instrument))
            .doOnError(e -> logger.error("Submit task with label " + orderLabel
                    + " for " + instrument + " failed!Exception: " + e.getMessage()))
            .doOnNext(this::addCreatedOrderToPosition)
            .doOnCompleted(() -> logger.info("Submit task with label " + orderLabel
                    + " for " + instrument + " was successful."));
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
            .concatWith(Observable.defer(() -> mergePositionOrders(mergeOrderLabel, orderParams.instrument())));
    }

    public Observable<OrderEvent> submitAndMergePositionToParams(final String mergeOrderLabel,
                                                                 final OrderParams orderParams) {
        checkNotNull(mergeOrderLabel);
        checkNotNull(orderParams);

        final double signedPositionAmount = position(orderParams.instrument()).signedExposure();
        final double signedParamsAmount = OrderStaticUtil.signedAmount(orderParams);
        final double signedNeededAmount = signedParamsAmount - signedPositionAmount;
        final OrderParams adaptedOrderParams =
                OrderStaticUtil.adaptedOrderParamsForSignedAmount(orderParams, signedNeededAmount);

        return submitAndMergePosition(mergeOrderLabel, adaptedOrderParams);
    }

    public Observable<OrderEvent> mergeOrders(final String mergeOrderLabel,
                                              final Collection<IOrder> toMergeOrders) {
        checkNotNull(mergeOrderLabel);
        checkNotNull(toMergeOrders);

        return toMergeOrders.size() < 2
                ? Observable.empty()
                : Observable
                    .just(toMergeOrders)
                    .doOnSubscribe(() -> {
                        logger.info("Starting to merge with label " + mergeOrderLabel
                                + " for position " + instrumentFromOrders(toMergeOrders) + ".");
                        position(toMergeOrders).markOrdersActive(toMergeOrders);
                    })
                    .flatMap(this::removeTPSLObservable)
                    .toCompletable()
                    .andThen(orderUtilObservable(new OrderCallCommand(() -> engine.mergeOrders(mergeOrderLabel,
                                                                                               toMergeOrders),
                                                                      OrderCallReason.MERGE)))
                    .doOnNext(this::addCreatedOrderToPosition)
                    .doOnTerminate(() -> position(toMergeOrders).markOrdersIdle(toMergeOrders))
                    .doOnCompleted(() -> logger.info("Merging with label " + mergeOrderLabel
                            + " for position " + instrumentFromOrders(toMergeOrders) + " was successful."))
                    .doOnError(e -> logger.error("Merging with label " + mergeOrderLabel + " for position "
                            + instrumentFromOrders(toMergeOrders) + " failed! Exception: " + e.getMessage()));
    }

    public Observable<OrderEvent> mergePositionOrders(final String mergeOrderLabel,
                                                      final Instrument instrument) {
        checkNotNull(mergeOrderLabel);
        checkNotNull(instrument);

        return mergeOrders(mergeOrderLabel, position(instrument).filled())
            .doOnSubscribe(() -> logger.info("Starting position merge for " +
                    instrument + " with label " + mergeOrderLabel))
            .doOnError(e -> logger.error("Position merge for " + instrument
                    + "  with label " + mergeOrderLabel + " failed!" + "Exception: " + e.getMessage()))
            .doOnCompleted(() -> logger.info("Position merge for " + instrument
                    + "  with label " + mergeOrderLabel + " was successful."));
    }

    private Observable<OrderEvent> removeTPSLObservable(final Collection<IOrder> filledOrders) {
        final Instrument instrument = instrumentFromOrders(filledOrders);

        return Observable
            .from(filledOrders)
            .doOnSubscribe(() -> logger.info("Starting remove TPSL task for position " + instrument))
            .flatMap(this::removeSingleTPSLObservable)
            .doOnError(e -> logger.error("Removing TPSL from " + instrument + " failed! Exception: " + e.getMessage()))
            .doOnCompleted(() -> logger.info("Removing TPSL task from " + instrument + " was successful."));
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
                    .doOnCompleted(() -> logger.info("Closing position " + instrument + " was successful."))
                    .doOnError(e -> logger.error("Closing position " + instrument
                            + " failed! Exception: " + e.getMessage()));
    }

    public Observable<OrderEvent> close(final IOrder orderToClose) {
        return Observable
            .just(orderToClose)
            .filter(order -> !isClosed.test(order))
            .flatMap(order -> changeObservable(order,
                                               IOrder.State.CLOSED,
                                               order.getState(),
                                               () -> order.close(),
                                               OrderCallReason.CLOSE));
    }

    public Observable<OrderEvent> setLabel(final IOrder orderToChangeLabel,
                                           final String newLabel) {
        return Observable
            .just(orderToChangeLabel)
            .filter(order -> !isLabelSetTo(newLabel).test(order))
            .flatMap(order -> changeObservable(order,
                                               newLabel,
                                               order.getLabel(),
                                               () -> order.setLabel(newLabel),
                                               OrderCallReason.CHANGE_LABEL));
    }

    public Observable<OrderEvent> setGoodTillTime(final IOrder orderToChangeGTT,
                                                  final long newGTT) {
        return Observable
            .just(orderToChangeGTT)
            .filter(order -> !isGTTSetTo(newGTT).test(order))
            .flatMap(order -> changeObservable(order,
                                               newGTT,
                                               order.getGoodTillTime(),
                                               () -> order.setGoodTillTime(newGTT),
                                               OrderCallReason.CHANGE_GTT));
    }

    public Observable<OrderEvent> setOpenPrice(final IOrder orderToChangeOpenPrice,
                                               final double newOpenPrice) {
        return Observable
            .just(orderToChangeOpenPrice)
            .filter(order -> !isOpenPriceSetTo(newOpenPrice).test(order))
            .flatMap(order -> changeObservable(order,
                                               newOpenPrice,
                                               order.getOpenPrice(),
                                               () -> order.setOpenPrice(newOpenPrice),
                                               OrderCallReason.CHANGE_PRICE));
    }

    public Observable<OrderEvent> setRequestedAmount(final IOrder orderToChangeAmount,
                                                     final double newRequestedAmount) {
        return Observable
            .just(orderToChangeAmount)
            .filter(order -> !isAmountSetTo(newRequestedAmount).test(order))
            .flatMap(order -> changeObservable(order,
                                               newRequestedAmount,
                                               order.getRequestedAmount(),
                                               () -> order.setRequestedAmount(newRequestedAmount),
                                               OrderCallReason.CHANGE_AMOUNT));
    }

    public Observable<OrderEvent> setStopLossPrice(final IOrder orderToChangeSL,
                                                   final double newSL) {
        return Observable
            .just(orderToChangeSL)
            .filter(order -> !isSLSetTo(newSL).test(order))
            .flatMap(order -> changeObservable(order,
                                               newSL,
                                               order.getStopLossPrice(),
                                               () -> order.setStopLossPrice(newSL),
                                               OrderCallReason.CHANGE_SL));
    }

    public Observable<OrderEvent> setTakeProfitPrice(final IOrder orderToChangeTP,
                                                     final double newTP) {
        return Observable
            .just(orderToChangeTP)
            .filter(order -> !isTPSetTo(newTP).test(order))
            .flatMap(order -> changeObservable(order,
                                               newTP,
                                               order.getTakeProfitPrice(),
                                               () -> order.setTakeProfitPrice(newTP),
                                               OrderCallReason.CHANGE_TP));
    }

    private <T> Observable<OrderEvent> changeObservable(final IOrder order,
                                                        final T newValue,
                                                        final T currentValue,
                                                        final JFRunnable runnable,
                                                        final OrderCallReason callReason) {
        final Callable<IOrder> callable = OrderStaticUtil.runnableToCallable(runnable, order);
        final String commonLog = callReason + " from " + currentValue + " to " + newValue
                + " for order " + order.getLabel() + " and instrument " + order.getInstrument();

        return Observable
            .just(new OrderCallCommand(callable, callReason))
            .doOnSubscribe(() -> logger.info("Start to " + commonLog))
            .flatMap(this::orderUtilObservable)
            .doOnError(e -> logger.error("Failed to " + commonLog + "!Excpetion: " + e.getMessage()))
            .doOnCompleted(() -> logger.info("Successfuly " + commonLog));
    }

    private Observable<OrderEvent> orderUtilObservable(final OrderCallCommand orderCallCommand) {
        return orderUtilHandler.callObservable(orderCallCommand);
    }
}
