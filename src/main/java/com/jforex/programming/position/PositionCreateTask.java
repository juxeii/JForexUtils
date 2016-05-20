package com.jforex.programming.position;

import java.util.Collection;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dukascopy.api.IOrder;
import com.dukascopy.api.Instrument;
import com.jforex.programming.order.OrderCreateUtil;
import com.jforex.programming.order.OrderParams;
import com.jforex.programming.order.OrderUtil;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.event.OrderEventTypeData;

import rx.Completable;
import rx.Observable;
import rx.observables.ConnectableObservable;

public class PositionCreateTask {

    private final PositionFactory positionFactory;
    private final PositionChangeTask positionChangeTask;
    private final OrderCreateUtil orderCreateUtil;
    private final PositionRetryLogic positionRetryLogic;

    private static final Logger logger = LogManager.getLogger(OrderUtil.class);

    public PositionCreateTask(final PositionFactory positionFactory,
                              final PositionChangeTask positionChangeTask,
                              final OrderCreateUtil orderCreateUtil,
                              final PositionRetryLogic positionRetryLogic) {
        this.positionFactory = positionFactory;
        this.positionChangeTask = positionChangeTask;
        this.orderCreateUtil = orderCreateUtil;
        this.positionRetryLogic = positionRetryLogic;
    }

    public Observable<OrderEvent> submitOrder(final OrderParams orderParams) {
        final Instrument instrument = orderParams.instrument();
        logger.debug("Start submit task with label " + orderParams.label() + " for " + instrument + " position.");

        final Position position = positionFactory.forInstrument(instrument);
        final Observable<OrderEvent> submitObs = orderCreateUtil.submitOrder(orderParams);
        submitObs.doOnCompleted(() -> logger.debug("Submit " + orderParams.label() + " for position "
                + instrument + " was successful."))
                .subscribe(orderEvent -> {
                    if (OrderEventTypeData.submitData.isDoneType(orderEvent.type()))
                        position.addOrder(orderEvent.order());
                },
                           e -> logger.error("Submit " + orderParams.label() + " for position "
                                   + instrument + " failed!"));
        return submitObs;
    }

    public Observable<OrderEvent> mergeOrders(final String mergeOrderLabel,
                                              final Collection<IOrder> toMergeOrders) {
        final Observable<OrderEvent> mergeObs = orderCreateUtil.mergeOrders(mergeOrderLabel, toMergeOrders);
        final Position position = positionFactory.forInstrument(toMergeOrders.iterator().next().getInstrument());
        mergeObs.subscribe(orderEvent -> position.addOrder(orderEvent.order()),
                           e -> logger.error("Merge for " + mergeOrderLabel + " failed!"),
                           () -> logger.debug("Merge for " + mergeOrderLabel + " was successful."));

        return mergeObs;
    }

    public Observable<OrderEvent> mergePositionOrders(final String mergeOrderLabel,
                                                      final Instrument instrument,
                                                      final RestoreSLTPPolicy restoreSLTPPolicy) {
        final Position position = positionFactory.forInstrument(instrument);
        final Set<IOrder> ordersToMerge = position.filledOrders();
        if (ordersToMerge.size() < 2) {
            logger.warn("Cannot merge " + instrument + " position with only " + ordersToMerge.size() + " orders!");
            return Observable.empty();
        }

        logger.debug("Starting merge task for " + instrument + " position with label " + mergeOrderLabel);
        position.markAllOrdersActive();
        final RestoreSLTPData restoreSLTPData = new RestoreSLTPData(restoreSLTPPolicy.restoreSL(ordersToMerge),
                                                                    restoreSLTPPolicy.restoreTP(ordersToMerge));

        final Completable mergeSequence =
                positionChangeTask.removeTPSLObs(ordersToMerge)
                        .concatWith(Observable.defer(() -> {
                            logger.debug("Start merge with label " + mergeOrderLabel);
                            return mergeOrders(mergeOrderLabel, ordersToMerge)
                                    .retryWhen(positionRetryLogic::shouldRetry)
                                    .flatMap(orderEvent -> Observable.just(orderEvent.order()));
                        })
                                .flatMap(mergeOrder -> positionChangeTask.restoreSLTPObs(mergeOrder, restoreSLTPData)
                                        .toObservable())
                                .toCompletable());
        final Observable<OrderEvent> mergeSequenceObs = mergeSequence.toObservable();
        final ConnectableObservable<OrderEvent> mergeObs = mergeSequenceObs.replay();
        mergeObs.connect();

        return mergeObs;
    }
}
