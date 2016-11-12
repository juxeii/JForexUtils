package com.jforex.programming.order.task;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.task.params.position.ClosePositionParams;
import com.jforex.programming.order.task.params.position.MergePositionParams;

import io.reactivex.Observable;

public class BatchChangeTask {

    private final BatchComposer batchComposer;

    public BatchChangeTask(final BatchComposer batchComposer) {
        this.batchComposer = batchComposer;
    }

    public Observable<OrderEvent> close(final Collection<IOrder> orders,
                                        final ClosePositionParams closePositionParams) {
        return forBasicTask(orders,
                            BatchMode.MERGE,
                            batchComposer.composeClose(closePositionParams));
    }

    public Observable<OrderEvent> cancelSL(final Collection<IOrder> orders,
                                           final MergePositionParams mergePositionParams) {
        return forBasicTask(orders,
                            mergePositionParams.batchCancelSLMode(),
                            batchComposer.composeCancelSL(mergePositionParams));
    }

    public Observable<OrderEvent> cancelTP(final Collection<IOrder> orders,
                                           final MergePositionParams mergePositionParams) {
        return forBasicTask(orders,
                            mergePositionParams.batchCancelTPMode(),
                            batchComposer.composeCancelTP(mergePositionParams));
    }

    private Observable<OrderEvent> forBasicTask(final Collection<IOrder> orders,
                                                final BatchMode batchMode,
                                                final Function<IOrder, Observable<OrderEvent>> basicTask) {
        final List<Observable<OrderEvent>> observables = Observable
            .fromIterable(orders)
            .map(basicTask::apply)
            .toList()
            .blockingGet();

        return batchMode == BatchMode.MERGE
                ? Observable.merge(observables)
                : Observable.concat(observables);
    }
}
