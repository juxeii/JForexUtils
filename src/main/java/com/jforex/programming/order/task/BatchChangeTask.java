package com.jforex.programming.order.task;

import java.util.Collection;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.task.params.position.ClosePositionParams;
import com.jforex.programming.order.task.params.position.MergePositionParams;

import io.reactivex.Observable;

public class BatchChangeTask {

    private final BatchComposer batchComposer;
    private final BatchCreator batchCreator;

    public BatchChangeTask(final BatchComposer batchComposer,
                           final BatchCreator batchCreator) {
        this.batchComposer = batchComposer;
        this.batchCreator = batchCreator;
    }

    public Observable<OrderEvent> close(final Collection<IOrder> orders,
                                        final ClosePositionParams closePositionParams) {
        return batchCreator.create(orders,
                                   closePositionParams.closeBatchMode(),
                                   batchComposer.composeClose(closePositionParams));
    }

    public Observable<OrderEvent> cancelSL(final Collection<IOrder> orders,
                                           final MergePositionParams mergePositionParams) {
        return batchCreator.create(orders,
                                   mergePositionParams.batchCancelSLMode(),
                                   batchComposer.composeCancelSL(mergePositionParams));
    }

    public Observable<OrderEvent> cancelTP(final Collection<IOrder> orders,
                                           final MergePositionParams mergePositionParams) {
        return batchCreator.create(orders,
                                   mergePositionParams.batchCancelTPMode(),
                                   batchComposer.composeCancelTP(mergePositionParams));
    }
}
