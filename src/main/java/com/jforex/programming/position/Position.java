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

import com.dukascopy.api.IOrder;
import com.dukascopy.api.Instrument;
import com.jforex.programming.misc.ConcurrentUtil;
import com.jforex.programming.order.OrderDirection;
import com.jforex.programming.order.OrderParams;
import com.jforex.programming.order.OrderUtil;
import com.jforex.programming.order.call.OrderCallRejectException;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.event.OrderEventType;

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
        final OrderEventType orderEventType = orderEvent.type();
        if (endOfOrderEventTypes.contains(orderEventType)) {
            orderRepository.remove(order);
            logger.info("Removed " + order.getLabel() + " from " + instrument
                    + " repositiory because of event type " + orderEventType);
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
        final Observable<OrderEvent> submitObs = orderUtil.submitOrder(orderParams);
        submitObs.subscribe(this::onSubmitEvent, e -> logger.error("Position submit for " + instrument + " failed!"));
        return submitObs;
    }

    private void onSubmitEvent(final OrderEvent orderEvent) {
        final IOrder order = orderEvent.order();
        if (isFilled.test(order))
            orderRepository.add(order);
    }

    public Observable<OrderEvent> merge(final String mergeLabel) {
        final Set<IOrder> filledOrders = filledOrders();
        if (filledOrders.size() < 2)
            return Observable.empty();

        orderRepository.markAllActive();
        final RestoreSLTPData restoreSLTPData = new RestoreSLTPData(restoreSLTPPolicy, filledOrders);

        final Observable<OrderEvent> mergeObs =
                removeTPSLObs(filledOrders)
                        .concatWith(mergeOrderObs(mergeLabel, filledOrders))
                        .flatMap(oe -> restoreSLTPObs(oe.order(), restoreSLTPData.sl(), restoreSLTPData.tp()));
        mergeObs.subscribe(o -> {}, e -> logger.error("Position merge for " + instrument + " failed!"));
        return mergeObs;
    }

    public void close() {
        final Set<IOrder> ordersToClose = orderRepository.filterIdle(isFilled.or(isOpened));
        orderRepository.markAllActive();
        Observable.from(ordersToClose)
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

    private Observable<OrderEvent> removeTPSLObs(final Set<IOrder> filledOrders) {
        return Observable.from(filledOrders)
                .flatMap(order -> Observable.concat(changeTPOrderObs(order, pfs.NO_TAKE_PROFIT_PRICE()),
                                                    changeSLOrderObs(order, pfs.NO_STOP_LOSS_PRICE())));
    }

    private Observable<OrderEvent> restoreSLTPObs(final IOrder mergedOrder,
                                                  final double restoreSL,
                                                  final double restoreTP) {
        return Observable.just(mergedOrder)
                .filter(isFilled::test)
                .flatMap(order -> Observable.concat(changeSLOrderObs(order, restoreSL),
                                                    changeTPOrderObs(order, restoreTP)));
    }

    private Observable<OrderEvent> mergeOrderObs(final String mergeLabel,
                                                 final Set<IOrder> filledOrders) {
        return Observable.just(mergeLabel)
                .doOnNext(label -> logger.debug("Start merge with label: " + label + " for " + instrument))
                .flatMap(order -> orderUtil.mergeOrders(mergeLabel, filledOrders))
                .retryWhen(this::shouldRetry)
                .doOnNext(this::onSubmitEvent);
    }

    private Observable<OrderEvent> changeSLOrderObs(final IOrder orderToChangeSL,
                                                    final double newSL) {
        return Observable.just(orderToChangeSL)
                .filter(order -> !isSLSetTo(newSL).test(orderToChangeSL))
                .doOnNext(order -> logger.debug("Start to change SL from " + order.getStopLossPrice() + " to "
                        + newSL + " for order " + order.getLabel() + " and position " + instrument))
                .flatMap(order -> orderUtil.setStopLossPrice(order, newSL))
                .retryWhen(this::shouldRetry);
    }

    private Observable<OrderEvent> changeTPOrderObs(final IOrder orderToChangeTP,
                                                    final double newTP) {
        return Observable.just(orderToChangeTP)
                .filter(order -> !isTPSetTo(newTP).test(orderToChangeTP))
                .doOnNext(order -> logger.debug("Start to change TP from " + order.getTakeProfitPrice() + " to "
                        + newTP + " for order " + order.getLabel() + " and position " + instrument))
                .flatMap(order -> orderUtil.setTakeProfitPrice(order, newTP))
                .retryWhen(this::shouldRetry);
    }

    private Observable<?> shouldRetry(final Observable<? extends Throwable> attempts) {
        return attempts.zipWith(Observable.range(1, pfs.MAX_NUM_RETRIES_ON_FAIL()),
                                (exc, att) -> exc)
                .doOnNext(exc -> logRetry((OrderCallRejectException) exc))
                .flatMap(exc -> concurrentUtil.timerObservable(pfs.ON_FAIL_RETRY_WAITING_TIME(),
                                                               TimeUnit.MILLISECONDS));
    }

    private void logRetry(final OrderCallRejectException rejectException) {
        final IOrder order = rejectException.orderEvent().order();
        logger.warn("Received reject type " + rejectException.orderEvent().type() + " for order " + order.getLabel()
                + "!" + " Will retry task in " + pfs.ON_FAIL_RETRY_WAITING_TIME() + " milliseconds...");
    }
}