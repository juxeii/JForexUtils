package com.jforex.programming.order.task;

import java.util.function.Function;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.task.params.TaskParamsBase;
import com.jforex.programming.order.task.params.TaskParamsUtil;
import com.jforex.programming.order.task.params.basic.CloseParams;
import com.jforex.programming.order.task.params.position.ClosePositionParams;
import com.jforex.programming.order.task.params.position.MergePositionParams;

import io.reactivex.Observable;

public class BatchComposer {

    private final TaskParamsUtil taskParamsUtil;
    private final BasicTaskForBatch basicTaskForBatch;

    public BatchComposer(final TaskParamsUtil taskParamsUtil,
                         final BasicTaskForBatch basicTaskForBatch) {
        this.taskParamsUtil = taskParamsUtil;
        this.basicTaskForBatch = basicTaskForBatch;
    }

    public Function<IOrder, Observable<OrderEvent>> composeClose(final ClosePositionParams closePositionParams) {
        return order -> {
            final CloseParams closeParams = closePositionParams
                .closeParamsFactory()
                .apply(order);
            return taskParamsUtil.compose(basicTaskForBatch.forClose(closeParams), closeParams);
        };
    }

    public Function<IOrder, Observable<OrderEvent>> composeCancelSL(final MergePositionParams mergePositionParams) {
        return order -> {
            final TaskParamsBase cancelSLParams = mergePositionParams
                .cancelSLParamsFactory()
                .apply(order);
            return taskParamsUtil.compose(basicTaskForBatch.forCancelSL(order), cancelSLParams);
        };
    }

    public Function<IOrder, Observable<OrderEvent>> composeCancelTP(final MergePositionParams mergePositionParams) {
        return order -> {
            final TaskParamsBase cancelTPParams = mergePositionParams
                .cancelTPParamsFactory()
                .apply(order);
            return taskParamsUtil.compose(basicTaskForBatch.forCancelTP(order), cancelTPParams);
        };
    }
}
