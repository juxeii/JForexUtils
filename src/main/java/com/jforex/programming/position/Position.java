package com.jforex.programming.position;

import static com.jforex.programming.order.OrderStaticUtil.isClosed;
import static com.jforex.programming.order.OrderStaticUtil.isConditional;
import static com.jforex.programming.order.OrderStaticUtil.isFilled;
import static com.jforex.programming.order.OrderStaticUtil.isOpened;
import static com.jforex.programming.order.OrderStaticUtil.isSLSetTo;
import static com.jforex.programming.order.OrderStaticUtil.isTPSetTo;
import static com.jforex.programming.order.event.OrderEventTypeSets.endOfOrderEventTypes;

import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

import org.aeonbits.owner.ConfigFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.jforex.programming.misc.ConcurrentUtil;
import com.jforex.programming.misc.JFObservable;
import com.jforex.programming.order.OrderDirection;
import com.jforex.programming.order.OrderParams;
import com.jforex.programming.order.OrderUtil;
import com.jforex.programming.order.call.OrderCallRejectException;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.settings.PlatformSettings;

import com.dukascopy.api.IOrder;
import com.dukascopy.api.Instrument;

import rx.Completable;
import rx.Observable;

public class Position {

    private final Instrument instrument;
    private final OrderUtil orderUtil;
    private final RestoreSLTPPolicy restoreSLTPPolicy;
    private final ConcurrentUtil concurrentUtil;
    private final PositionOrders orderRepository = new PositionOrders();
    private final JFObservable<Long> positionEventPublisher = new JFObservable<>();

    private final static PlatformSettings platformSettings = ConfigFactory.create(PlatformSettings.class);
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
                .doOnNext(orderEvent -> logger.info("Received in repository " + orderEvent.type() + " for position "
                        + instrument + " with label " + orderEvent.order().getLabel()))
                .filter(orderEvent -> endOfOrderEventTypes.contains(orderEvent.type()))
                .doOnNext(orderEvent -> orderRepository.remove(orderEvent.order()))
                .doOnNext(orderEvent -> logger.info("Removed " + orderEvent.order().getLabel() + " from " + instrument
                        + " repositiory because of event type " + orderEvent.type()))
                .subscribe();
    }

    public Instrument instrument() {
        return instrument;
    }

    public OrderDirection direction() {
        return orderRepository.direction();
    }

    public double signedExposure() {
        return orderRepository.signedExposure();
    }

    public Set<IOrder> filter(final Predicate<IOrder> orderPredicate) {
        return orderRepository.filter(orderPredicate);
    }

    public Set<IOrder> orders() {
        return orderRepository.orders();
    }

    private Set<IOrder> filledOrders() {
        return orderRepository.filterIdle(isFilled);
    }

    public Completable submit(final OrderParams orderParams) {
        logger.debug("Start submit task with label " + orderParams.label() + " for " + instrument + " position.");
        orderUtil.submitOrder(orderParams)
                .flatMap(orderEvent -> Observable.just(orderEvent.order()))
                .doOnNext(this::onOrderAdd)
                .doOnTerminate(() -> positionEventPublisher.onNext(1L))
                .subscribe(order -> {},
                           throwable -> logger.error("Submit for position " + instrument + " failed!"));

        return Completable.fromObservable(positionEventPublisher.get().take(1));
    }

    public Completable merge(final String mergeLabel) {
        final Set<IOrder> toMergeOrders = filledOrders();
        if (toMergeOrders.size() < 2)
            return Completable.complete();

        logger.debug("Starting merge task for " + instrument + " position with label " + mergeLabel);
        orderRepository.markAllActive();
        final RestoreSLTPData restoreSLTPData = new RestoreSLTPData(restoreSLTPPolicy.restoreSL(toMergeOrders),
                                                                    restoreSLTPPolicy.restoreTP(toMergeOrders));

        removeTPSLObs(toMergeOrders)
                .doOnCompleted(() -> mergeAndRestore(mergeLabel, toMergeOrders, restoreSLTPData))
                .subscribe();

        return Completable.fromObservable(positionEventPublisher.get().take(1));
    }

    private void mergeAndRestore(final String mergeLabel,
                                 final Set<IOrder> toMergeOrders,
                                 final RestoreSLTPData restoreSLTPData) {
        mergeOrderObs(mergeLabel, toMergeOrders)
                .doOnNext(this::onOrderAdd)
                .flatMap(mergeOrder -> restoreSLTPObs(mergeOrder, restoreSLTPData).toObservable())
                .doOnTerminate(() -> positionEventPublisher.onNext(1L))
                .subscribe(order -> {},
                           throwable -> logger.error("Merging position " + instrument + " failed!"),
                           () -> logger.debug("Merging position " + instrument + " successful."));
    }

    public Completable close() {
        final Set<IOrder> ordersToClose = orderRepository.filterIdle(isFilled.or(isOpened));
        if (ordersToClose.isEmpty())
            return Completable.complete();

        logger.debug("Starting to close " + instrument + " position");
        orderRepository.markAllActive();

        Observable.from(ordersToClose)
                .flatMap(order -> orderUtil.close(order))
                .retry(this::shouldRetry)
                .doOnNext(orderEvent -> onOrderRemove(orderEvent.order()))
                .doOnTerminate(() -> positionEventPublisher.onNext(1L))
                .subscribe(orderEvent -> {},
                           throwable -> logger.error("Closing position " + instrument + " failed!"),
                           () -> logger.debug("Closing position " + instrument + " successful."));

        return Completable.fromObservable(positionEventPublisher.get().take(1));
    }

    private void onOrderAdd(final IOrder order) {
        if (isFilled.test(order) || (isConditional.test(order) && isOpened.test(order))) {
            orderRepository.add(order);
            logger.debug("Added order " + order.getLabel() + " to position " + instrument + " Orderstate: "
                    + order.getState() + " repo size " + orderRepository.size());
        }
    }

    private void onOrderRemove(final IOrder order) {
        if (isClosed.test(order)) {
            orderRepository.remove(order);
            logger.debug("Removed order " + order.getLabel() + " of position " + instrument + " Orderstate: "
                    + order.getState() + " repo size " + orderRepository.size());
        }
    }

    private Completable removeTPSLObs(final Set<IOrder> filledOrders) {
        logger.debug("Called removeTPSLObs for " + instrument + " with filledOrders size " + filledOrders.size());
        return Completable.fromObservable(Observable.from(filledOrders)
                .flatMap(order -> Completable.concat(changeTPOrderObs(order, platformSettings.noTPPrice()),
                                                     changeSLOrderObs(order, platformSettings.noSLPrice()))
                        .toObservable()));
    }

    private Completable restoreSLTPObs(final IOrder mergedOrder,
                                       final RestoreSLTPData restoreSLTPData) {
        return Completable.concat(changeSLOrderObs(mergedOrder, restoreSLTPData.sl()),
                                  changeTPOrderObs(mergedOrder, restoreSLTPData.tp()));
    }

    private Observable<IOrder> mergeOrderObs(final String mergeLabel,
                                             final Set<IOrder> filledOrders) {
        logger.debug("Start merge with label " + mergeLabel + " for " + instrument);
        return Observable.defer(() -> orderUtil.mergeOrders(mergeLabel, filledOrders))
                .doOnError(t -> logger.info("MERGE RETRY for " + mergeLabel))
                .retry(this::shouldRetry)
                .flatMap(orderEvent -> Observable.just(orderEvent.order()));
    }

    private Completable changeSLOrderObs(final IOrder orderToChangeSL,
                                         final double newSL) {
        logger.debug("Called changeSLOrderObs for " + orderToChangeSL.getLabel() + " with new SL " + newSL);
        return Completable.fromObservable(Observable.just(orderToChangeSL)
                .filter(order -> !isSLSetTo(newSL).test(orderToChangeSL))
                .doOnNext(order -> logger.debug("Start to change SL from " + order.getStopLossPrice() + " to "
                        + newSL + " for order " + order.getLabel() + " and position " + instrument))
                .flatMap(order -> orderUtil.setStopLossPrice(order, newSL))
                .retry(this::shouldRetry));
    }

    private Completable changeTPOrderObs(final IOrder orderToChangeTP,
                                         final double newTP) {
        logger.debug("Called changeTPOrderObs for " + orderToChangeTP.getLabel() + " with new SL " + newTP);
        return Completable.fromObservable(Observable.just(orderToChangeTP)
                .filter(order -> !isTPSetTo(newTP).test(orderToChangeTP))
                .doOnNext(order -> logger.debug("Start to change TP from " + order.getTakeProfitPrice() + " to "
                        + newTP + " for order " + order.getLabel() + " and position " + instrument))
                .flatMap(order -> orderUtil.setTakeProfitPrice(order, newTP))
                .retry(this::shouldRetry));
    }

    public boolean shouldRetry(final int retryCount,
                               final Throwable throwable) {
        if (throwable instanceof OrderCallRejectException &&
                retryCount <= platformSettings.maxRetriesOnOrderFail()) {
            logRetry((OrderCallRejectException) throwable);
            concurrentUtil.timerObservable(platformSettings.delayOnOrderFailRetry(), TimeUnit.MILLISECONDS)
                    .toBlocking()
                    .subscribe(i -> {});
            return true;
        }
        return false;
    }

    private void logRetry(final OrderCallRejectException rejectException) {
        final IOrder order = rejectException.orderEvent().order();
        logger.warn("Received reject type " + rejectException.orderEvent().type() + " for order " + order.getLabel()
                + "!" + " Will retry task in " + platformSettings.delayOnOrderFailRetry() + " milliseconds...");
    }
}