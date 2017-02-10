package com.jforex.programming.order.task;

import java.util.Collection;
import java.util.function.Function;

import com.dukascopy.api.IOrder;
import com.dukascopy.api.Instrument;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.task.params.position.MergeAllPositionsParams;
import com.jforex.programming.order.task.params.position.MergePositionParams;
import com.jforex.programming.position.PositionUtil;

import io.reactivex.Observable;

public class MergePositionTask {

    private final CancelSLTPAndMergeTask cancelSLTPAndMergeTask;
    private final PositionUtil positionUtil;

    public MergePositionTask(final CancelSLTPAndMergeTask cancelSLTPAndMergeTask,
                             final PositionUtil positionUtil) {
        this.cancelSLTPAndMergeTask = cancelSLTPAndMergeTask;
        this.positionUtil = positionUtil;
    }

    public Observable<OrderEvent> merge(final Collection<IOrder> toMergeOrders,
                                        final MergePositionParams mergePositionParams) {
        return observeSplit(toMergeOrders, mergePositionParams);
    }

    public Observable<OrderEvent> merge(final MergePositionParams mergePositionParams) {
        return Observable.defer(() -> observeSplit(positionUtil.filledOrders(mergePositionParams.instrument()),
                                                   mergePositionParams));
    }

    private final Observable<OrderEvent> observeSplit(final Collection<IOrder> toMergeOrders,
                                                      final MergePositionParams mergePositionParams) {
        return Observable.defer(() -> toMergeOrders.size() > 1
                ? cancelSLTPAndMergeTask.observe(toMergeOrders, mergePositionParams)
                : Observable.empty());
    }

    public Observable<OrderEvent> mergeAll(final MergeAllPositionsParams mergeAllPositionParams) {
        return Observable.defer(() -> {
            final Function<Instrument, Observable<OrderEvent>> observablesFromFactory =
                    instrument -> merge(mergeAllPositionParams.mergePositionParamsFactory().apply(instrument));
            return Observable.merge(positionUtil.observablesFromFactory(observablesFromFactory));
        });
    }
}
