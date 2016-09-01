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
import java.util.Set;
import java.util.concurrent.Callable;

import org.aeonbits.owner.ConfigFactory;

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

public class OrderUtilObservable {

    private final IEngine engine;
    private final PositionFactory positionFactory;
    private final OrderUtilHandler orderUtilHandler;

    private static final PlatformSettings platformSettings = ConfigFactory.create(PlatformSettings.class);

    public OrderUtilObservable(final IEngine engine,
                               final PositionFactory positionFactory,
                               final OrderUtilHandler orderUtilHandler) {
        this.engine = engine;
        this.positionFactory = positionFactory;
        this.orderUtilHandler = orderUtilHandler;
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

    public Observable<OrderEvent> submitOrder(final OrderParams orderParams) {
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
        return submitOrder(orderParams)
            .concatWith(Observable.defer(() -> mergePositionOrders(mergeOrderLabel, orderParams.instrument())));
    }

    public Observable<OrderEvent> submitAndMergePositionToParams(final String mergeOrderLabel,
                                                                 final OrderParams orderParams) {
        final double signedPositionAmount = position(orderParams.instrument()).signedExposure();
        final double signedParamsAmount = OrderStaticUtil.signedAmount(orderParams);
        final double signedNeededAmount = signedParamsAmount - signedPositionAmount;
        final OrderParams adaptedOrderParams = OrderStaticUtil
            .adaptedOrderParamsForSignedAmount(orderParams, signedNeededAmount);

        return submitAndMergePosition(mergeOrderLabel, adaptedOrderParams);
    }

    public Observable<OrderEvent> mergeOrders(final String mergeOrderLabel,
                                              final Collection<IOrder> toMergeOrders) {
        return toMergeOrders.size() < 2
                ? Observable.empty()
                : Observable
                    .just(toMergeOrders)
                    .doOnSubscribe(() -> position(toMergeOrders).markOrdersActive(toMergeOrders))
                    .flatMap(this::removeTPSLObservable)
                    .toCompletable()
                    .andThen(mergeObservable(mergeOrderLabel, toMergeOrders))
                    .doOnNext(this::addCreatedOrderToPosition)
                    .doOnTerminate(() -> position(toMergeOrders).markOrdersIdle(toMergeOrders));
    }

    private Observable<OrderEvent> mergeObservable(final String mergeOrderLabel,
                                                   final Collection<IOrder> toMergeOrders) {
        final Callable<IOrder> mergeCallable = () -> engine.mergeOrders(mergeOrderLabel, toMergeOrders);
        final OrderCallCommand command = new OrderCallCommand(mergeCallable, OrderCallReason.MERGE);
        return orderUtilObservable(command);
    }

    public Observable<OrderEvent> mergePositionOrders(final String mergeOrderLabel,
                                                      final Instrument instrument) {
        return mergeOrders(mergeOrderLabel, position(instrument).filled());
    }

    private Observable<OrderEvent> removeTPSLObservable(final Collection<IOrder> filledOrders) {
        return Observable
            .from(filledOrders)
            .flatMap(this::removeSingleTPSLObservable);
    }

    private final Observable<OrderEvent> removeSingleTPSLObservable(final IOrder orderToRemoveSLTP) {
        return setTakeProfitPrice(orderToRemoveSLTP, platformSettings.noSLPrice())
            .concatWith(setStopLossPrice(orderToRemoveSLTP, platformSettings.noTPPrice()));
    }

    public Observable<OrderEvent> closePosition(final Instrument instrument) {
        final Position position = position(instrument);
        final Set<IOrder> ordersToClose = position.filledOrOpened();

        return ordersToClose.isEmpty()
                ? Observable.empty()
                : Observable
                    .from(ordersToClose)
                    .doOnSubscribe(() -> position.markOrdersActive(ordersToClose))
                    .flatMap(this::close)
                    .doOnTerminate(() -> position.markOrdersIdle(ordersToClose));
    }

    public Observable<OrderEvent> close(final IOrder orderToClose) {
        return Observable
            .just(orderToClose)
            .filter(order -> !isClosed.test(order))
            .flatMap(order -> changeObservable(order,
                                               () -> order.close(),
                                               OrderCallReason.CLOSE));
    }

    public Observable<OrderEvent> setLabel(final IOrder orderToChangeLabel,
                                           final String newLabel) {
        return Observable
            .just(orderToChangeLabel)
            .filter(order -> !isLabelSetTo(newLabel).test(order))
            .flatMap(order -> changeObservable(order,
                                               () -> order.setLabel(newLabel),
                                               OrderCallReason.CHANGE_LABEL));
    }

    public Observable<OrderEvent> setGoodTillTime(final IOrder orderToChangeGTT,
                                                  final long newGTT) {
        return Observable
            .just(orderToChangeGTT)
            .filter(order -> !isGTTSetTo(newGTT).test(order))
            .flatMap(order -> changeObservable(order,
                                               () -> order.setGoodTillTime(newGTT),
                                               OrderCallReason.CHANGE_GTT));
    }

    public Observable<OrderEvent> setOpenPrice(final IOrder orderToChangeOpenPrice,
                                               final double newOpenPrice) {
        return Observable
            .just(orderToChangeOpenPrice)
            .filter(order -> !isOpenPriceSetTo(newOpenPrice).test(order))
            .flatMap(order -> changeObservable(order,
                                               () -> order.setOpenPrice(newOpenPrice),
                                               OrderCallReason.CHANGE_PRICE));
    }

    public Observable<OrderEvent> setRequestedAmount(final IOrder orderToChangeAmount,
                                                     final double newRequestedAmount) {
        return Observable
            .just(orderToChangeAmount)
            .filter(order -> !isAmountSetTo(newRequestedAmount).test(order))
            .flatMap(order -> changeObservable(order,
                                               () -> order.setRequestedAmount(newRequestedAmount),
                                               OrderCallReason.CHANGE_AMOUNT));
    }

    public Observable<OrderEvent> setStopLossPrice(final IOrder orderToChangeSL,
                                                   final double newSL) {
        return Observable
            .just(orderToChangeSL)
            .filter(order -> !isSLSetTo(newSL).test(order))
            .flatMap(order -> changeObservable(order,
                                               () -> order.setStopLossPrice(newSL),
                                               OrderCallReason.CHANGE_SL));
    }

    public Observable<OrderEvent> setTakeProfitPrice(final IOrder orderToChangeTP,
                                                     final double newTP) {
        return Observable
            .just(orderToChangeTP)
            .filter(order -> !isTPSetTo(newTP).test(order))
            .flatMap(order -> changeObservable(order,
                                               () -> order.setTakeProfitPrice(newTP),
                                               OrderCallReason.CHANGE_TP));
    }

    private Observable<OrderEvent> changeObservable(final IOrder order,
                                                    final JFRunnable runnable,
                                                    final OrderCallReason callReason) {
        final Callable<IOrder> callable = OrderStaticUtil.runnableToCallable(runnable, order);

        return Observable
            .just(new OrderCallCommand(callable, callReason))
            .flatMap(this::orderUtilObservable);
    }

    private Observable<OrderEvent> orderUtilObservable(final OrderCallCommand orderCallCommand) {
        return orderUtilHandler.callObservable(orderCallCommand);
    }
}
