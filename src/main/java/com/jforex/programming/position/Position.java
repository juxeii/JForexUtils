package com.jforex.programming.position;

import static com.jforex.programming.misc.JForexUtil.pfs;
import static com.jforex.programming.order.OrderStaticUtil.isFilled;
import static com.jforex.programming.order.OrderStaticUtil.isOpened;
import static com.jforex.programming.order.OrderStaticUtil.isSLSetTo;
import static com.jforex.programming.order.OrderStaticUtil.isTPSetTo;
import static com.jforex.programming.order.event.OrderEventTypeSets.endOfOrderEventTypes;
import static java.util.stream.Collectors.toList;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.MapMaker;
import com.jforex.programming.misc.JFEventPublisherForRx;
import com.jforex.programming.order.OrderDirection;
import com.jforex.programming.order.OrderParams;
import com.jforex.programming.order.OrderStaticUtil;
import com.jforex.programming.order.OrderUtilObservable;
import com.jforex.programming.order.event.OrderEvent;

import com.dukascopy.api.IOrder;
import com.dukascopy.api.Instrument;

import rx.Observable;

public class Position {

    private final Instrument instrument;
    private final OrderUtilObservable orderUtilObservable;
    private final Set<IOrder> orderRepository =
            Collections.newSetFromMap(new MapMaker().weakKeys().<IOrder, Boolean> makeMap());
    private final RestoreSLTPPolicy restoreSLTPPolicy;
    private final JFEventPublisherForRx<PositionEventType> positionEventTypePublisher = new JFEventPublisherForRx<>();

    private static final Logger logger = LogManager.getLogger(Position.class);

    public Position(final Instrument instrument,
                    final OrderUtilObservable orderUtilObservable,
                    final RestoreSLTPPolicy restoreSLTPPolicy) {
        this.instrument = instrument;
        this.restoreSLTPPolicy = restoreSLTPPolicy;
        this.orderUtilObservable = orderUtilObservable;

        orderUtilObservable.orderEventObservable()
                .filter(orderEvent -> orderRepository.contains(orderEvent.order()))
                .doOnNext(orderEvent -> logger.info("Received " + orderEvent.type() + " for position "
                        + instrument + " with label " + orderEvent.order().getLabel()))
                .doOnNext(this::checkOnOrderCloseEvent)
                .subscribe();
    }

    private void checkOnOrderCloseEvent(final OrderEvent orderEvent) {
        final IOrder order = orderEvent.order();
        if (endOfOrderEventTypes.contains(orderEvent.type())) {
            orderRepository.remove(order);
            logger.info("Removed " + order.getLabel() + " from " + instrument
                    + " repositiory because of event type " + orderEvent.type());
        }
    }

    public Observable<PositionEventType> positionEventTypeObs() {
        return positionEventTypePublisher.observable();
    }

    public OrderDirection direction() {
        return OrderStaticUtil.combinedDirection(filter(isFilled));
    }

    public double signedExposure() {
        return filter(isFilled).stream()
                .mapToDouble(OrderStaticUtil::signedAmount)
                .sum();
    }

    public Collection<IOrder> filter(final Predicate<IOrder> orderPredicate) {
        return orderRepository.stream()
                .filter(orderPredicate)
                .collect(toList());
    }

    public Collection<IOrder> orders() {
        return ImmutableSet.copyOf(orderRepository);
    }

    private Collection<IOrder> filledOrders() {
        return filter(isFilled);
    }

    public synchronized void submit(final OrderParams orderParams) {
        startTaskObs(orderUtilObservable.submit(orderParams)
                .doOnNext(orderRepository::add), PositionEventType.SUBMITTED);
    }

    public synchronized void merge(final String mergeLabel) {
        final RestoreSLTPData restoreSLTPData = new RestoreSLTPData(restoreSLTPPolicy, filledOrders());
        startTaskObs(mergeSequenceObs(mergeLabel, restoreSLTPData), PositionEventType.MERGED);
    }

    public synchronized void close() {
        final Observable<IOrder> observable =
                Observable.from(filter(isFilled.or(isOpened)))
                        .doOnSubscribe(() -> logger.debug("Starting to close " + instrument + " position"))
                        .flatMap(order -> orderUtilObservable.close(order).retryWhen(this::shouldRetry))
                        .doOnNext(orderRepository::remove);
        startTaskObs(observable, PositionEventType.CLOSED);
    }

    private void startTaskObs(final Observable<IOrder> observable,
                              final PositionEventType positionEventType) {
        observable.subscribe(item -> {},
                             exc -> taskFinishWithException(exc, positionEventType),
                             () -> taskFinish(positionEventType));
    }

    private Observable<IOrder> mergeSequenceObs(final String mergeLabel,
                                                final RestoreSLTPData restoreSLTPData) {
        if (filledOrders().size() <= 1)
            return Observable.empty();

        final Observable<IOrder> mergeAndRestoreObs =
                mergeOrderObs(mergeLabel).flatMap(order -> restoreSLTPObs(order,
                                                                          restoreSLTPData.sl(),
                                                                          restoreSLTPData.tp()));
        return removeTPSLObs().concatWith(mergeAndRestoreObs);
    }

    private Observable<IOrder> removeTPSLObs() {
        return Observable.from(filledOrders())
                .flatMap(order -> Observable.concat(changeTPOrderObs(order, pfs.NO_TAKE_PROFIT_PRICE()),
                                                    changeSLOrderObs(order, pfs.NO_STOP_LOSS_PRICE())));
    }

    private Observable<IOrder> restoreSLTPObs(final IOrder mergedOrder,
                                              final double restoreSL,
                                              final double restoreTP) {
        return Observable.just(mergedOrder)
                .filter(isFilled::test)
                .flatMap(order -> Observable.concat(changeSLOrderObs(order, restoreSL),
                                                    changeTPOrderObs(order, restoreTP)));
    }

    private Observable<IOrder> mergeOrderObs(final String mergeLabel) {
        return Observable.just(mergeLabel)
                .doOnNext(label -> logger.debug("Start merge with label: " + label + " for " + instrument))
                .flatMap(label -> orderUtilObservable.merge(mergeLabel, filledOrders()))
                .retryWhen(this::shouldRetry);
    }

    private Observable<IOrder> changeSLOrderObs(final IOrder orderToChangeSL,
                                                final double newSL) {
        return Observable.just(orderToChangeSL)
                .filter(order -> !isSLSetTo(newSL).test(order))
                .doOnNext(order -> logger.debug("Start to change SL from " + order.getStopLossPrice() + " to "
                        + newSL + " for order " + order.getLabel() + " and position " + instrument))
                .flatMap(order -> orderUtilObservable.setSL(orderToChangeSL, newSL))
                .retryWhen(this::shouldRetry);
    }

    private Observable<IOrder> changeTPOrderObs(final IOrder orderToChangeTP,
                                                final double newTP) {
        return Observable.just(orderToChangeTP)
                .filter(order -> !isTPSetTo(newTP).test(order))
                .doOnNext(order -> logger.debug("Start to change TP from " + order.getTakeProfitPrice() + " to "
                        + newTP + " for order " + order.getLabel() + " and position " + instrument))
                .flatMap(order -> orderUtilObservable.setTP(orderToChangeTP, newTP))
                .retryWhen(this::shouldRetry);
    }

    private Observable<?> shouldRetry(final Observable<? extends Throwable> attempts) {
        return attempts.zipWith(Observable.range(1, pfs.MAX_NUM_RETRIES_ON_FAIL()),
                                (exc, att) -> exc)
                .doOnNext(exc -> logRetry((PositionTaskRejectException) exc))
                .flatMap(exc -> Observable.timer(pfs.ON_FAIL_RETRY_WAITING_TIME(), TimeUnit.MILLISECONDS));
    }

    private void logRetry(final PositionTaskRejectException rejectException) {
        final IOrder order = rejectException.orderEvent().order();
        logger.warn("Received reject type " + rejectException.orderEvent().type() + " for order " + order.getLabel()
                + "!" + " Will retry task in " + pfs.ON_FAIL_RETRY_WAITING_TIME() + " milliseconds...");
    }

    private void taskFinish(final PositionEventType positionEventType) {
        positionEventTypePublisher.onJFEvent(positionEventType);
    }

    private void taskFinishWithException(final Throwable throwable,
                                         final PositionEventType positionEventType) {
        positionEventTypePublisher.onJFEvent(positionEventType);
    }
}