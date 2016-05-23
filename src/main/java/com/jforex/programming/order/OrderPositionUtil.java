package com.jforex.programming.order;

import java.util.Collection;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dukascopy.api.IOrder;
import com.dukascopy.api.Instrument;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.event.OrderEventTypeData;
import com.jforex.programming.position.Position;
import com.jforex.programming.position.PositionFactory;
import com.jforex.programming.position.RestoreSLTPData;
import com.jforex.programming.position.RestoreSLTPPolicy;
import com.jforex.programming.position.task.PositionBatchTask;
import com.jforex.programming.position.task.PositionMultiTask;
import com.jforex.programming.position.task.PositionSingleTask;
import com.jforex.programming.position.task.PositionTaskUtil;

import rx.Completable;
import rx.Observable;

public class OrderPositionUtil {

    private final OrderCreateUtil orderCreateUtil;
    private final PositionSingleTask positionSingleTask;
    private final PositionMultiTask positionMultiTask;
    private final PositionBatchTask positionBatchTask;
    private final PositionFactory positionFactory;

    private static final Logger logger = LogManager.getLogger(OrderPositionUtil.class);

    public OrderPositionUtil(final OrderCreateUtil orderCreateUtil,
                             final PositionSingleTask positionSingleTask,
                             final PositionMultiTask positionMultiTask,
                             final PositionBatchTask positionBatchTask,
                             final PositionFactory positionFactory) {
        this.orderCreateUtil = orderCreateUtil;
        this.positionSingleTask = positionSingleTask;
        this.positionMultiTask = positionMultiTask;
        this.positionBatchTask = positionBatchTask;
        this.positionFactory = positionFactory;
    }

    public Position position(final Instrument instrument) {
        return positionFactory.forInstrument(instrument);
    }

    public Observable<OrderEvent> submitOrder(final OrderParams orderParams) {
        final Instrument instrument = orderParams.instrument();
        logger.debug("Start submit task with label " + orderParams.label() + " for " + instrument + " position.");

        final Position position = positionFactory.forInstrument(instrument);
        final Observable<OrderEvent> submitObs = orderCreateUtil.submitOrder(orderParams);
        submitObs.subscribe(submitEvent -> onSubmitEvent(position, submitEvent),
                            e -> logger.error("Submit " + orderParams.label() + " for position "
                                    + instrument + " failed! Exception: " + e.getMessage()));
        return PositionTaskUtil.connectObservable(submitObs);
    }

    private void onSubmitEvent(final Position position,
                               final OrderEvent submitEvent) {
        if (OrderEventTypeData.submitData.isDoneType(submitEvent.type()))
            position.addOrder(submitEvent.order());
    }

    public Observable<OrderEvent> mergeOrders(final String mergeOrderLabel,
                                              final Collection<IOrder> toMergeOrders) {
        final Instrument instrument = toMergeOrders.iterator().next().getInstrument();
        final Position position = positionFactory.forInstrument(instrument);
        final Observable<OrderEvent> mergeObs = orderCreateUtil.mergeOrders(mergeOrderLabel, toMergeOrders);
        mergeObs.subscribe(orderEvent -> position.addOrder(orderEvent.order()),
                           e -> logger.error("Merge with label " + mergeOrderLabel
                                   + " failed! Exception: " + e.getMessage()));
        return PositionTaskUtil.connectObservable(mergeObs);
    }

    public Observable<OrderEvent> mergePositionOrders(final String mergeOrderLabel,
                                                      final Instrument instrument,
                                                      final RestoreSLTPPolicy restoreSLTPPolicy) {
        final Position position = positionFactory.forInstrument(instrument);
        final Set<IOrder> filledOrders = position.filledOrders();
        if (filledOrders.size() < 2)
            return Observable.empty();

        final RestoreSLTPData restoreSLTPData = new RestoreSLTPData(restoreSLTPPolicy.restoreSL(filledOrders),
                                                                    restoreSLTPPolicy.restoreTP(filledOrders));
        final Observable<OrderEvent> mergeAndRestoreObs =
                Observable.defer(() -> positionSingleTask.mergeObservable(mergeOrderLabel, filledOrders))
                        .doOnNext(orderEvent -> position.addOrder(orderEvent.order()))
                        .flatMap(orderEvent -> positionMultiTask.restoreSLTPObservable(orderEvent.order(),
                                                                                       restoreSLTPData))
                        .doOnCompleted(() -> logger.debug("Merge task for " + instrument + " position with label "
                                + mergeOrderLabel + " was successful."));

        final Observable<OrderEvent> mergeSequenceObs =
                Observable.just(filledOrders)
                        .doOnNext(toMergeOrders -> logger.debug("Starting merge task for " + instrument
                                + " position with label " + mergeOrderLabel))
                        .doOnNext(toMergeOrders -> position.markAllOrdersActive())
                        .flatMap(toMergeOrders -> positionBatchTask.removeTPSLObservable(toMergeOrders)
                                .toObservable())
                        .concatWith(mergeAndRestoreObs)
                        .cast(OrderEvent.class);

        return PositionTaskUtil.connectObservable(mergeSequenceObs);
    }

    public Completable closePosition(final Instrument instrument) {
        final Position position = positionFactory.forInstrument(instrument);
        final Set<IOrder> ordersToClose = position.filledOrOpenedOrders();

        if (ordersToClose.isEmpty())
            return Completable.complete();
        position.markAllOrdersActive();
        return PositionTaskUtil.connectCompletable(positionBatchTask.closeCompletable(ordersToClose));
    }
}
