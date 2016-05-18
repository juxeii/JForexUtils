package com.jforex.programming.position;

import static com.jforex.programming.order.OrderStaticUtil.isFilled;
import static com.jforex.programming.order.OrderStaticUtil.isOpened;
import static com.jforex.programming.order.event.OrderEventTypeSets.endOfOrderEventTypes;

import java.util.Set;
import java.util.function.Predicate;

import org.aeonbits.owner.ConfigFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dukascopy.api.IOrder;
import com.dukascopy.api.Instrument;
import com.jforex.programming.order.OrderDirection;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.settings.PlatformSettings;

import rx.Completable;
import rx.Observable;

public class Position {

    private final Instrument instrument;
    private final PositionTask positionTask;
    private final RestoreSLTPPolicy restoreSLTPPolicy;
    private final PositionOrders orderRepository = new PositionOrders();

    private final static PlatformSettings platformSettings = ConfigFactory.create(PlatformSettings.class);
    private static final Logger logger = LogManager.getLogger(Position.class);

    public Position(final Instrument instrument,
                    final PositionTask positionTask,
                    final Observable<OrderEvent> orderEventObservable,
                    final RestoreSLTPPolicy restoreSLTPPolicy) {
        this.instrument = instrument;
        this.positionTask = positionTask;
        this.restoreSLTPPolicy = restoreSLTPPolicy;

        orderEventObservable
                .filter(orderEvent -> orderEvent.order().getInstrument() == instrument)
                .filter(orderEvent -> orderRepository.contains(orderEvent.order()))
                .doOnNext(orderEvent -> logger.info("Received event " + orderEvent.type() + " for order "
                        + orderEvent.order().getLabel() + "in repository for " + instrument))
                .filter(orderEvent -> endOfOrderEventTypes.contains(orderEvent.type()))
                .doOnNext(orderEvent -> removeOrder(orderEvent.order()))
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

    public Set<IOrder> filledOrders() {
        return orderRepository.filterIdle(isFilled);
    }

    public Completable merge(final String mergeLabel) {
        final Set<IOrder> ordersToMerge = filledOrders();
        if (ordersToMerge.size() < 2)
            return Completable.complete();

        logger.debug("Starting merge task for " + instrument + " position with label " + mergeLabel);
        orderRepository.markAllActive();
        final RestoreSLTPData restoreSLTPData = new RestoreSLTPData(restoreSLTPPolicy.restoreSL(ordersToMerge),
                                                                    restoreSLTPPolicy.restoreTP(ordersToMerge));

        return removeTPSLObs(ordersToMerge)
                .concatWith(Observable.defer(() -> positionTask.mergeObservable(mergeLabel, ordersToMerge))
                        .doOnNext(this::addOrder)
                        .flatMap(mergeOrder -> restoreSLTPObs(mergeOrder, restoreSLTPData).toObservable())
                        .toCompletable());
    }

    private Completable removeTPSLObs(final Set<IOrder> filledOrders) {
        final Completable removeTPObs = Observable.from(filledOrders)
                .doOnNext(order -> logger.debug("Remove TP from " + order.getLabel()))
                .flatMap(order -> positionTask.setTPCompletable(order, platformSettings.noTPPrice()).toObservable())
                .toCompletable();
        final Completable removeSLObs = Observable.from(filledOrders)
                .doOnNext(order -> logger.debug("Remove SL from " + order.getLabel()))
                .flatMap(order -> positionTask.setSLCompletable(order, platformSettings.noSLPrice()).toObservable())
                .toCompletable();
        return removeTPObs.concatWith(removeSLObs);
    }

    private Completable restoreSLTPObs(final IOrder mergedOrder,
                                       final RestoreSLTPData restoreSLTPData) {
        final Completable restoreSLObs = Observable.just(mergedOrder)
                .doOnNext(order -> logger.debug("Restore SL from " + order.getLabel()))
                .flatMap(order -> positionTask.setSLCompletable(order, restoreSLTPData.sl()).toObservable())
                .toCompletable();
        final Completable restoreTPObs = Observable.just(mergedOrder)
                .doOnNext(order -> logger.debug("Restore TP from " + order.getLabel()))
                .flatMap(order -> positionTask.setTPCompletable(order, restoreSLTPData.tp()).toObservable())
                .toCompletable();
        return restoreSLObs.concatWith(restoreTPObs);
    }

    public Set<IOrder> filterIdle() {
        return orderRepository.filterIdle(isFilled.or(isOpened));
    }

    public void markOrdersActive() {
        orderRepository.markAllActive();
    }

    public void addOrder(final IOrder order) {
        orderRepository.add(order);
        logger.debug("Added order " + order.getLabel() + " to position " + instrument + " Orderstate: "
                + order.getState() + " repo size " + orderRepository.size());
    }

    public void removeOrder(final IOrder order) {
        orderRepository.remove(order);
        logger.debug("Removed order " + order.getLabel() + " from position " + instrument + " Orderstate: "
                + order.getState() + " repo size " + orderRepository.size());
    }
}