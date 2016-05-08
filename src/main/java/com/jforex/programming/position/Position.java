package com.jforex.programming.position;

import static com.jforex.programming.misc.JForexUtil.pfs;
import static com.jforex.programming.order.OrderStaticUtil.isClosed;
import static com.jforex.programming.order.OrderStaticUtil.isFilled;
import static com.jforex.programming.order.OrderStaticUtil.isOpened;
import static com.jforex.programming.order.OrderStaticUtil.isSLSetTo;
import static com.jforex.programming.order.OrderStaticUtil.isTPSetTo;
import static com.jforex.programming.order.event.OrderEventTypeSets.endOfOrderEventTypes;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.jforex.programming.misc.ConcurrentUtil;
import com.jforex.programming.order.OrderDirection;
import com.jforex.programming.order.OrderParams;
import com.jforex.programming.order.OrderUtil;
import com.jforex.programming.order.event.OrderEvent;

import com.dukascopy.api.IOrder;
import com.dukascopy.api.Instrument;

import rx.Observable;

public class Position {

    private final Instrument instrument;
    private final OrderUtil orderUtil;
    private final RestoreSLTPPolicy restoreSLTPPolicy;
    private final ConcurrentUtil concurrentUtil;
    private final PositionOrders orderRepository = new PositionOrders();

    private static final Logger logger = LogManager.getLogger(Position.class);

    public Position(final Instrument instrument,
                    final OrderUtil orderUtil,
                    final Observable<OrderEvent> orderEventObservable,
                    final RestoreSLTPPolicy restoreSLTPPolicy,
                    final ConcurrentUtil concurrentUtil) {
        this.instrument = instrument;
        this.orderUtil = orderUtil;
        this.restoreSLTPPolicy = restoreSLTPPolicy;
        this.concurrentUtil = concurrentUtil;

        orderEventObservable
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

    public OrderDirection direction() {
        return orderRepository.direction();
    }

    public double signedExposure() {
        return orderRepository.signedExposure();
    }

    public Collection<IOrder> filter(final Predicate<IOrder> orderPredicate) {
        return orderRepository.filter(orderPredicate);
    }

    public Set<IOrder> orders() {
        return orderRepository.orders();
    }

    private Set<IOrder> filledOrders() {
        return orderRepository.filterIdle(isFilled);
    }

    public Observable<OrderEvent> submit(final OrderParams orderParams) {
        logger.info("Start submit for " + orderParams.label());
        final Observable<OrderEvent> submitObs = orderUtil.submit(orderParams);
        submitObs.subscribe(this::onSubmitEvent);
        return submitObs;
    }

    private void onSubmitEvent(final OrderEvent orderEvent) {
        final IOrder order = orderEvent.order();
        if (isOpened.test(order))
            orderRepository.add(order);
    }

    public synchronized void merge(final String mergeLabel) {
        final Set<IOrder> filledOrders = filledOrders();
        if (filledOrders.size() < 2)
            return;

        orderRepository.markAllActive();
        final RestoreSLTPData restoreSLTPData = new RestoreSLTPData(restoreSLTPPolicy, filledOrders);
        mergeSequenceObs(mergeLabel, filledOrders, restoreSLTPData);
    }

    public synchronized void close() {
        orderRepository.markAllActive();
        Observable.from(orderRepository.filterIdle(isFilled.or(isOpened)))
                .doOnSubscribe(() -> logger.debug("Starting to close " + instrument + " position"))
                .filter(order -> !isClosed.test(order))
                .flatMap(order -> orderUtil.close(order))
                .retryWhen(this::shouldRetry)
                .subscribe(this::onCloseEvent);
    }

    private void onCloseEvent(final OrderEvent orderEvent) {
        final IOrder order = orderEvent.order();
        if (isClosed.test(order))
            orderRepository.remove(order);
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
        return Observable.just(mergeLabel)
                .flatMap(order -> orderUtil.merge(mergeLabel, filledOrders))
                .doOnNext(label -> logger.debug("Start merge with label: " + label + " for " + instrument))
                .retryWhen(this::shouldRetry)
                .doOnNext(this::onSubmitEvent)
                .flatMap(oe -> Observable.just(oe.order()));
    }

    private Observable<IOrder> changeSLOrderObs(final IOrder orderToChangeSL,
                                                final double newSL) {
        return Observable.just(orderToChangeSL)
                .filter(order -> isSLSetTo(newSL).test(orderToChangeSL))
                .flatMap(order -> orderUtil.setSL(order, newSL))
                .doOnNext(oe -> logger.debug("Start to change SL from " + oe.order().getStopLossPrice() + " to "
                        + newSL + " for order " + oe.order().getLabel() + " and position " + instrument))
                .retryWhen(this::shouldRetry)
                .doOnNext(oe -> logger.debug("Changed SL from " + oe.order().getStopLossPrice()
                        + " to " + oe.order().getStopLossPrice() + " for order " + oe.order().getLabel()
                        + " and position "
                        + instrument))
                .flatMap(oe -> Observable.just(orderToChangeSL));
    }

    private Observable<IOrder> changeTPOrderObs(final IOrder orderToChangeTP,
                                                final double newTP) {
        return Observable.just(orderToChangeTP)
                .filter(order -> isTPSetTo(newTP).test(orderToChangeTP))
                .flatMap(order -> orderUtil.setTP(order, newTP))
                .doOnNext(oe -> logger.debug("Start to change TP from " + oe.order().getTakeProfitPrice() + " to "
                        + newTP + " for order " + oe.order().getLabel() + " and position " + instrument))
                .retryWhen(this::shouldRetry)
                .doOnNext(oe -> logger.debug("Changed TP from " + oe.order().getTakeProfitPrice()
                        + " to " + oe.order().getTakeProfitPrice() + " for order " + oe.order().getLabel()
                        + " and position "
                        + instrument))
                .flatMap(oe -> Observable.just(orderToChangeTP));
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
}