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
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.base.Supplier;
import com.google.common.collect.MapMaker;
import com.jforex.programming.misc.JFEventPublisherForRx;
import com.jforex.programming.order.OrderChangeResult;
import com.jforex.programming.order.OrderCreateResult;
import com.jforex.programming.order.OrderDirection;
import com.jforex.programming.order.OrderParams;
import com.jforex.programming.order.OrderStaticUtil;
import com.jforex.programming.order.OrderUtil;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.event.OrderEventType;

import com.dukascopy.api.IOrder;
import com.dukascopy.api.Instrument;

import rx.Observable;
import rx.Subscriber;

public class Position {

    private final Instrument instrument;
    private final OrderUtil orderUtil;
    private final Queue<Runnable> taskQueue = new ConcurrentLinkedQueue<>();
    private final Set<IOrder> orderRepository =
            Collections.newSetFromMap(new MapMaker().weakKeys().<IOrder, Boolean> makeMap());
    private final RestoreSLTPPolicy restoreSLTPPolicy;
    private final ConcurrentMap<IOrder, OrderProgressData> progressDataByOrder = new MapMaker().weakKeys().makeMap();
    private final JFEventPublisherForRx<PositionEventType> positionEventPublisher = new JFEventPublisherForRx<>();
    private PositionEventType positionEventType;

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

    public Observable<PositionEventType> positionEventTypeObs() {
        return positionEventPublisher.observable();
    }

    private void processPositionEvent(final OrderEvent orderEvent) {
        final IOrder order = orderEvent.order();
        if (progressDataByOrder.containsKey(order))
            handleOrderInProgress(orderEvent);
        if (endOfOrderEventTypes.contains(orderEvent.type())) {
            orderRepository.remove(order);
            logger.info("Removed " + order.getLabel() + " from " + instrument
                    + " repositiory because of event type " + orderEvent.type());
        }
    }

    private void handleOrderInProgress(final OrderEvent orderEvent) {
        final IOrder order = orderEvent.order();
        final OrderProgressData progressData = progressDataByOrder.get(order);
        final Subscriber<? super IOrder> subscriber = progressData.subscriber();
        final TaskEventData taskEventData = progressData.taskEventData();
        final OrderEventType orderEventType = orderEvent.type();

        if (taskEventData.forReject().contains(orderEventType)) {
            progressDataByOrder.remove(order);
            subscriber.onError(new PositionTaskRejectException("", orderEvent));
        } else if (taskEventData.forDone().contains(orderEventType)) {
            if (wasOrderCreated(orderEventType)) {
                orderRepository.add(order);
                logger.info("Added " + order.getLabel() + " to " + instrument
                        + " repositiory because of event type " + orderEventType);
            }
            progressDataByOrder.remove(order);
            subscriber.onNext(order);
            subscriber.onCompleted();
        }
    }

    private boolean wasOrderCreated(final OrderEventType orderEventType) {
        return orderEventType == OrderEventType.FULL_FILL_OK
                || orderEventType == OrderEventType.MERGE_OK;
    }

    public boolean isBusy() {
        return !taskQueue.isEmpty();
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
        return orderRepository;
    }

    private Collection<IOrder> filledOrders() {
        return filter(isFilled);
    }

    public synchronized void submit(final OrderParams orderParams) {
        positionEventType = PositionEventType.SUBMITTED;
        startTaskObs(submitOrderObs(orderParams));
    }

    public synchronized void merge(final String mergeLabel) {
        positionEventType = null;
        final RestoreSLTPData restoreSLTPData = new RestoreSLTPData(restoreSLTPPolicy, filledOrders());
        startTaskObs(mergeSequenceObs(mergeLabel, restoreSLTPData));
    }

    public synchronized void close() {
        positionEventType = null;
        final Observable<IOrder> observable =
                Observable.from(filter(isFilled.or(isOpened)))
                          .doOnSubscribe(() -> logger.info("Starting to close " + instrument + " position"))
                          .flatMap(order -> orderObsForChange(() -> orderUtil.close(order),
                                                              TaskEventData.closeEvents).retryWhen(this::shouldRetry));
        startTaskObs(observable);
    }

    private void startTaskObs(final Observable<IOrder> observable) {
        final Runnable task = () -> observable.subscribe(item -> {},
                                                         exc -> taskFinish(),
                                                         () -> taskFinish());
        taskQueue.add(task);
        if (taskQueue.size() == 1)
            taskQueue.peek().run();
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

    private Observable<IOrder> orderObsForCreate(final Supplier<OrderCreateResult> orderCreateResultSupplier,
                                                 final TaskEventData taskEventData) {
        return Observable.create(subscriber -> {
            final OrderCreateResult orderCreateResult = orderCreateResultSupplier.get();
            if (orderCreateResult.exceptionOpt().isPresent())
                subscriber.onError(orderCreateResult.exceptionOpt().get());
            else
                progressDataByOrder.put(orderCreateResult.orderOpt().get(),
                                        new OrderProgressData(taskEventData, subscriber));
        });
    }

    private Observable<IOrder> orderObsForChange(final Supplier<OrderChangeResult> orderChangeResultSupplier,
                                                 final TaskEventData taskEventData) {
        return Observable.create(subscriber -> {
            final OrderChangeResult orderChangeResult = orderChangeResultSupplier.get();
            if (orderChangeResult.exceptionOpt().isPresent())
                subscriber.onError(orderChangeResult.exceptionOpt().get());
            else
                progressDataByOrder.put(orderChangeResult.order(),
                                        new OrderProgressData(taskEventData, subscriber));
        });
    }

    private Observable<IOrder> submitOrderObs(final OrderParams orderParams) {
        return orderObsForCreate(() -> orderUtil.submit(orderParams),
                                 TaskEventData.submitEvents);
    }

    private Observable<IOrder> mergeOrderObs(final String mergeLabel) {
        return Observable.just(mergeLabel)
                         .doOnNext(label -> logger.info("Start merge with label: " + label + " for " + instrument))
                         .flatMap(label -> orderObsForCreate(() -> orderUtil.merge(mergeLabel, filledOrders()),
                                                             TaskEventData.mergeEvents))
                         .retryWhen(this::shouldRetry);
    }

    private Observable<IOrder> changeSLOrderObs(final IOrder orderToChangeSL,
                                                final double newSL) {
        return Observable.just(orderToChangeSL)
                         .filter(order -> !isSLSetTo(newSL).test(order))
                         .doOnNext(order -> logger.info("Start to change SL from " + order.getStopLossPrice() + " to "
                                 + newSL + " for order " + order.getLabel() + " and position " + instrument))
                         .flatMap(order -> orderObsForChange(() -> orderUtil.setSL(order, newSL),
                                                             TaskEventData.changeSLEvents))
                         .retryWhen(this::shouldRetry);
    }

    private Observable<IOrder> changeTPOrderObs(final IOrder orderToChangeTP,
                                                final double newTP) {
        return Observable.just(orderToChangeTP)
                         .filter(order -> !isTPSetTo(newTP).test(order))
                         .doOnNext(order -> logger.info("Start to change TP from " + order.getTakeProfitPrice() + " to "
                                 + newTP + " for order " + order.getLabel() + " and position " + instrument))
                         .flatMap(order -> orderObsForChange(() -> orderUtil.setTP(order, newTP),
                                                             TaskEventData.changeTPEvents))
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

    private void taskFinish() {
        if (!taskQueue.isEmpty())
            taskQueue.remove();
        if (!taskQueue.isEmpty())
            taskQueue.peek().run();
        if (positionEventType != null)
            positionEventPublisher.onJFEvent(positionEventType);
    }
}