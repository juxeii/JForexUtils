package com.jforex.programming.position;

import static com.jforex.programming.misc.JForexUtil.pfs;
import static com.jforex.programming.order.OrderStaticUtil.isFilled;
import static com.jforex.programming.order.OrderStaticUtil.isOpened;
import static com.jforex.programming.order.event.OrderEventTypeSets.endOfOrderEventTypes;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.Sets;
import com.jforex.programming.misc.ConcurrentUtil;
import com.jforex.programming.misc.JFObservable;
import com.jforex.programming.order.OrderDirection;
import com.jforex.programming.order.OrderParams;
import com.jforex.programming.order.OrderUtilObservable;
import com.jforex.programming.order.event.OrderEvent;

import com.dukascopy.api.IOrder;
import com.dukascopy.api.Instrument;

import rx.Observable;

public class Position {

    private final Instrument instrument;
    private final OrderUtilObservable orderUtilObservable;
    private final RestoreSLTPPolicy restoreSLTPPolicy;
    private final ConcurrentUtil concurrentUtil;
    private final PositionOrders orderRepository = new PositionOrders();
    private final JFObservable<PositionEventType> positionEventTypePublisher = new JFObservable<>();

    private static final Logger logger = LogManager.getLogger(Position.class);

    public Position(final Instrument instrument,
                    final OrderUtilObservable orderUtilObservable,
                    final RestoreSLTPPolicy restoreSLTPPolicy,
                    final ConcurrentUtil concurrentUtil) {
        this.instrument = instrument;
        this.orderUtilObservable = orderUtilObservable;
        this.restoreSLTPPolicy = restoreSLTPPolicy;
        this.concurrentUtil = concurrentUtil;

        orderUtilObservable.orderEventObservable()
                .filter(orderEvent -> orderEvent.order().getInstrument() == instrument)
                .doOnNext(orderEvent -> logger.info("Received " + orderEvent.type() + " for position "
                        + instrument + " with label " + orderEvent.order().getLabel()))
                .filter(orderEvent -> orderRepository.contains(orderEvent.order()))
                .doOnNext(orderEvent -> logger.info("Received " + orderEvent.type() + " for position "
                        + instrument + " with label " + orderEvent.order().getLabel()))
                .doOnNext(this::checkOnOrderCloseEvent)
                .subscribe();
    }

    public Instrument instrument() {
        return instrument;
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
        return positionEventTypePublisher.get();
    }

    public OrderDirection direction() {
        return orderRepository.direction();
    }

    public double signedExposure() {
        return orderRepository.signedExposure();
    }

    public Collection<IOrder> filter(final Predicate<IOrder> orderPredicate) {
        return orderRepository.filter(orderPredicate);
    }

    public Collection<IOrder> orders() {
        return orderRepository.orders();
    }

    private Collection<IOrder> filledOrders() {
        return orderRepository.filterIdle(isFilled);
    }

    public synchronized void submit(final OrderParams orderParams) {
        logger.info("Start submit for " + orderParams.label());
        startTaskObs(orderUtilObservable.submit(orderParams)
                .doOnNext(orderRepository::add),
                     PositionEventType.SUBMITTED);
    }

    public synchronized void merge(final String mergeLabel) {
        final Set<IOrder> filledOrders = Sets.newHashSet(filledOrders());
        if (filledOrders.size() < 2) {
            positionEventTypePublisher.onNext(PositionEventType.MERGED);
            return;
        }

        orderRepository.markAllActive();
        final RestoreSLTPData restoreSLTPData = new RestoreSLTPData(restoreSLTPPolicy, filledOrders);
        startTaskObs(mergeSequenceObs(mergeLabel, filledOrders, restoreSLTPData),
                     PositionEventType.MERGED);
    }

    public synchronized void close() {
        final Set<IOrder> ordersToClose = Sets.newHashSet(orderRepository.filterIdle(isFilled.or(isOpened)));
        System.out.println("Closing ordersToClose " + ordersToClose.size());
        if (ordersToClose.isEmpty())
            return;

        orderRepository.markAllActive();
        final Observable<IOrder> observable = Observable.from(ordersToClose)
                .doOnSubscribe(() -> logger.debug("Starting to close " + instrument + " position"))
                .flatMap(order -> orderUtilObservable.close(order))
                .doOnNext(orderRepository::remove)
                .retryWhen(this::shouldRetry);
        startTaskObs(observable, PositionEventType.CLOSED);
    }

    private void startTaskObs(final Observable<IOrder> observable,
                              final PositionEventType positionEventType) {
        observable.subscribe(item -> {},
                             this::taskFinishWithException,
                             () -> taskFinish(positionEventType));
    }

    private Observable<IOrder> mergeSequenceObs(final String mergeLabel,
                                                final Set<IOrder> filledOrders,
                                                final RestoreSLTPData restoreSLTPData) {
        final Observable<IOrder> mergeAndRestoreObs = mergeOrderObs(mergeLabel, filledOrders)
                .flatMap(order -> restoreSLTPObs(order, restoreSLTPData.sl(), restoreSLTPData.tp()));

        return removeTPSLObs(filledOrders).concatWith(mergeAndRestoreObs);
    }

    private Observable<IOrder> removeTPSLObs(final Set<IOrder> filledOrders) {
        return Observable.from(filledOrders)
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

    private Observable<IOrder> mergeOrderObs(final String mergeLabel,
                                             final Set<IOrder> filledOrders) {
        return orderUtilObservable.merge(mergeLabel, filledOrders)
                .doOnNext(label -> logger.debug("Start merge with label: " + label + " for " + instrument))
                .retryWhen(this::shouldRetry)
                .doOnNext(orderRepository::add);
    }

    private Observable<IOrder> changeSLOrderObs(final IOrder orderToChangeSL,
                                                final double newSL) {
        return orderUtilObservable.setSL(orderToChangeSL, newSL)
                .doOnNext(order -> logger.debug("Start to change SL from " + order.getStopLossPrice() + " to "
                        + newSL + " for order " + order.getLabel() + " and position " + instrument))
                .retryWhen(this::shouldRetry)
                .doOnNext(order -> logger.debug("Changed SL from " + order.getStopLossPrice()
                        + " to " + order.getStopLossPrice() + " for order " + order.getLabel() + " and position "
                        + instrument));
    }

    private Observable<IOrder> changeTPOrderObs(final IOrder orderToChangeTP,
                                                final double newTP) {
        return orderUtilObservable.setTP(orderToChangeTP, newTP)
                .doOnNext(order -> logger.debug("Start to change TP from " + order.getTakeProfitPrice()
                        + " to " + newTP + " for order " + order.getLabel() + " and position " + instrument))
                .retryWhen(this::shouldRetry)
                .doOnNext(order -> logger.debug("Changed TP from " + order.getTakeProfitPrice()
                        + " to " + order.getTakeProfitPrice() + " for order " + order.getLabel() + " and position "
                        + instrument));
    }

    private Observable<?> shouldRetry(final Observable<? extends Throwable> attempts) {
        return attempts.zipWith(Observable.range(1, pfs.MAX_NUM_RETRIES_ON_FAIL()),
                                (exc, att) -> exc)
                .doOnNext(exc -> logRetry((PositionTaskRejectException) exc))
                .flatMap(exc -> concurrentUtil.timerObservable(pfs.ON_FAIL_RETRY_WAITING_TIME(),
                                                               TimeUnit.MILLISECONDS));
    }

    private void logRetry(final PositionTaskRejectException rejectException) {
        final IOrder order = rejectException.orderEvent().order();
        logger.warn("Received reject type " + rejectException.orderEvent().type() + " for order " + order.getLabel()
                + "!" + " Will retry task in " + pfs.ON_FAIL_RETRY_WAITING_TIME() + " milliseconds...");
    }

    private void taskFinish(final PositionEventType positionEventType) {
        positionEventTypePublisher.onNext(positionEventType);
    }

    private void taskFinishWithException(final Throwable throwable) {
        logger.error("Task finished with excpetion! " + throwable.getMessage());
        positionEventTypePublisher.onNext(PositionEventType.ERROR);
    }
}