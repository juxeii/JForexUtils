package com.jforex.programming.position;

import static com.jforex.programming.misc.JForexUtil.pfs;
import static com.jforex.programming.order.OrderStaticUtil.isFilled;
import static com.jforex.programming.order.OrderStaticUtil.isOpened;
import static com.jforex.programming.order.OrderStaticUtil.isSLSetTo;
import static com.jforex.programming.order.OrderStaticUtil.isTPSetTo;
import static com.jforex.programming.order.event.OrderEventTypeSets.endOfOrderEventTypes;
import static java.util.stream.Collectors.toList;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Predicate;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dukascopy.api.IOrder;
import com.dukascopy.api.Instrument;
import com.google.common.base.Supplier;
import com.google.common.collect.MapMaker;
import com.google.common.collect.Sets;
import com.jforex.programming.order.OrderDirection;
import com.jforex.programming.order.OrderParams;
import com.jforex.programming.order.OrderStaticUtil;
import com.jforex.programming.order.OrderUtil;
import com.jforex.programming.order.call.OrderCallResult;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.event.OrderEventType;

import rx.Observable;
import rx.Subscriber;

public class Position {

    private final Instrument instrument;
    private final OrderUtil orderUtil;
    private final Set<IOrder> orderRepository = Sets.newIdentityHashSet();
    private final RestoreSLTPPolicy restoreSLTPPolicy;
    private boolean isBusy = false;
    private final ConcurrentMap<IOrder, OrderProgressData> progressDataByOrder = new MapMaker().weakKeys().makeMap();
    private final Lock lock = new ReentrantLock();

    private static final Logger logger = LogManager.getLogger(Position.class);

    public Position(final Instrument instrument,
                    final OrderUtil orderUtil,
                    final Observable<OrderEvent> orderEventObservable,
                    final RestoreSLTPPolicy restoreSLTPPolicy) {
        this.instrument = instrument;
        this.orderUtil = orderUtil;
        this.restoreSLTPPolicy = restoreSLTPPolicy;

        orderEventObservable.filter(orderEvent -> orderEvent.order().getInstrument() == instrument)
                            .doOnNext(orderEvent -> logger.info("Received " + orderEvent.type() + " for position "
                                    + instrument + " with label " + orderEvent.order().getLabel()))
                            .subscribe(this::processPositionEvent);
    }

    private void processPositionEvent(final OrderEvent orderEvent) {
        if (progressDataByOrder.containsKey(orderEvent.order()))
            handleOrderInProgress(orderEvent);
        orderRepository.removeIf(order -> endOfOrderEventTypes.contains(orderEvent.type()));
    }

    private void handleOrderInProgress(final OrderEvent orderEvent) {
        final IOrder order = orderEvent.order();
        final OrderProgressData orderProgressData = progressDataByOrder.get(order);
        final Subscriber<? super IOrder> subscriber = orderProgressData.subscriber();
        final TaskEventData taskEventData = orderProgressData.taskEventData();
        final OrderEventType orderEventType = orderEvent.type();

        if (taskEventData.forReject().contains(orderEventType)) {
            progressDataByOrder.remove(order);
            subscriber.onError(new PositionTaskRejectException("", orderEvent));
        } else if (taskEventData.forDone().contains(orderEventType)) {
            if (orderEventType == OrderEventType.FULL_FILL_OK
                    || orderEventType == OrderEventType.MERGE_OK)
                orderRepository.add(order);
            progressDataByOrder.remove(order);
            subscriber.onNext(order);
            subscriber.onCompleted();
        }
    }

    public boolean isBusy() {
        return isBusy;
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

    public void submit(final OrderParams orderParams) {
        startTask(submitOrderObs(orderParams));
    }

    public void submitAndMerge(final OrderParams orderParams,
                               final String mergeLabel) {
        final RestoreSLTPData restoreSLTPData = new RestoreSLTPData(restoreSLTPPolicy, filledOrders());
        startTask(submitOrderObs(orderParams).concatMap(op -> mergeSequenceObs(mergeLabel, restoreSLTPData)));
    }

    public void merge(final String mergeLabel) {
        final RestoreSLTPData restoreSLTPData = new RestoreSLTPData(restoreSLTPPolicy, filledOrders());
        startTask(mergeSequenceObs(mergeLabel, restoreSLTPData));
    }

    public void close() {
        startTask(Observable.from(filter(isFilled.or(isOpened)))
                            .doOnSubscribe(() -> logger.info("Starting to close " + instrument + " position"))
                            .flatMap(order -> orderObs(() -> orderUtil.close(order),
                                                       TaskEventData.closeEvents).retryWhen(this::shouldRetry)));
    }

    private void startTask(final Observable<IOrder> observable) {
        lock.lock();
        isBusy = true;
        observable.subscribe(item -> {} , exc -> taskFinish(), () -> taskFinish());
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

    private Observable<IOrder> orderObs(final Supplier<OrderCallResult> orderCallSupplier,
                                        final TaskEventData taskEventData) {
        return Observable.create(subscriber -> {
            final OrderCallResult orderCallResult = orderCallSupplier.get();
            if (orderCallResult.exceptionOpt().isPresent())
                subscriber.onError(orderCallResult.exceptionOpt().get());
            else
                progressDataByOrder.put(orderCallResult.orderOpt().get(),
                                        new OrderProgressData(taskEventData, subscriber));
        });
    }

    private Observable<IOrder> submitOrderObs(final OrderParams orderParams) {
        return orderObs(() -> orderUtil.submit(orderParams),
                        TaskEventData.submitEvents);
    }

    private Observable<IOrder> mergeOrderObs(final String mergeLabel) {
        return Observable.just(mergeLabel)
                         .doOnNext(label -> logger.info("Start merge with label: " + label + " for " + instrument))
                         .flatMap(label -> orderObs(() -> orderUtil.merge(mergeLabel, filledOrders()),
                                                    TaskEventData.mergeEvents))
                         .retryWhen(this::shouldRetry);
    }

    private Observable<IOrder> changeSLOrderObs(final IOrder orderToChangeSL,
                                                final double newSL) {
        return Observable.just(orderToChangeSL)
                         .filter(order -> !isSLSetTo(newSL).test(order))
                         .doOnNext(order -> logger.info("Start to change SL from " + order.getStopLossPrice() + " to "
                                 + newSL + " for order " + order.getLabel() + " and position " + instrument))
                         .flatMap(order -> orderObs(() -> orderUtil.changeSL(order, newSL),
                                                    TaskEventData.changeSLEvents))
                         .retryWhen(this::shouldRetry);
    }

    private Observable<IOrder> changeTPOrderObs(final IOrder orderToChangeTP,
                                                final double newTP) {
        return Observable.just(orderToChangeTP)
                         .filter(order -> !isTPSetTo(newTP).test(order))
                         .doOnNext(order -> logger.info("Start to change TP from " + order.getTakeProfitPrice() + " to "
                                 + newTP + " for order " + order.getLabel() + " and position " + instrument))
                         .flatMap(order -> orderObs(() -> orderUtil.changeTP(order, newTP),
                                                    TaskEventData.changeTPEvents))
                         .retryWhen(this::shouldRetry);
    }

    private Observable<?> shouldRetry(final Observable<? extends Throwable> attempts) {
        return attempts.zipWith(Observable.range(1, pfs.MAX_NUM_RETRIES_ON_FAIL()),
                                (exc, att) -> exc)
                       .doOnNext(exc -> logRetry((PositionTaskRejectException) exc))
                       .flatMap(exc -> Observable.timer(pfs.ON_FAIL_RETRY_WAITING_TIME(),
                                                        TimeUnit.MILLISECONDS));
    }

    private void logRetry(final PositionTaskRejectException rejectException) {
        final IOrder order = rejectException.orderEvent().order();
        logger.warn("Received reject type " + rejectException.orderEvent().type() + " for order " + order.getLabel()
                + "!" + " Will retry task in " + pfs.ON_FAIL_RETRY_WAITING_TIME() + " milliseconds...");
    }

    private Collection<IOrder> filledOrders() {
        return filter(isFilled);
    }

    private void taskFinish() {
        isBusy = false;
        lock.unlock();
    }
}