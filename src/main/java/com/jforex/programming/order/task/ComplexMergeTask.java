package com.jforex.programming.order.task;

import java.util.Collection;

import com.dukascopy.api.IOrder;
import com.dukascopy.api.Instrument;
import com.google.common.base.Supplier;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.task.params.position.ComplexMergePositionParams;
import com.jforex.programming.position.PositionUtil;

import io.reactivex.Observable;
import io.reactivex.functions.Function;

public class ComplexMergeTask {

    private final CancelSLTPAndMergeTask cancelSLTPAndMergeTask;
    private final PositionUtil positionUtil;

    public ComplexMergeTask(final CancelSLTPAndMergeTask cancelSLTPAndMergeTask,
                            final PositionUtil positionUtil) {
        this.cancelSLTPAndMergeTask = cancelSLTPAndMergeTask;
        this.positionUtil = positionUtil;
    }

    public Observable<OrderEvent> merge(final Collection<IOrder> toMergeOrders,
                                        final ComplexMergePositionParams complexMergeParams) {
        return observeSplit(() -> toMergeOrders, complexMergeParams);
    }

    public Observable<OrderEvent> mergePosition(final Instrument instrument,
                                                final ComplexMergePositionParams mergeParams) {
        return observeSplit(() -> positionUtil.filledOrders(instrument), mergeParams);
    }

    private final Observable<OrderEvent> observeSplit(final Supplier<Collection<IOrder>> toMergeOrders,
                                                      final ComplexMergePositionParams mergeParams) {
        return Observable.defer(() -> cancelSLTPAndMergeTask.observe(toMergeOrders.get(), mergeParams));
    }

    public Observable<OrderEvent> mergeAll(final ComplexMergePositionParams complexMergePositionParams) {
        return Observable.defer(() -> {
            final Function<Instrument, Observable<OrderEvent>> observablesFromFactory =
                    instrument -> mergePosition(instrument, complexMergePositionParams);
            return Observable.merge(positionUtil.observablesFromFactory(observablesFromFactory));
        });
    }
}
