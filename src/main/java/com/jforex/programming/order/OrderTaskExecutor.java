package com.jforex.programming.order;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.concurrent.Callable;

import com.dukascopy.api.IOrder;
import com.jforex.programming.misc.IEngineUtil;
import com.jforex.programming.misc.TaskExecutor;

import io.reactivex.Single;

public class OrderTaskExecutor {

    private final TaskExecutor taskExecutor;
    private final IEngineUtil engineUtil;

    public OrderTaskExecutor(final TaskExecutor taskExecutor,
                             final IEngineUtil engineUtil) {
        this.taskExecutor = taskExecutor;
        this.engineUtil = engineUtil;
    }

    public Single<IOrder> submitOrder(final OrderParams orderParams) {
        final Callable<IOrder> submitCallable = engineUtil.submitCallable(checkNotNull(orderParams));
        return taskExecutor.onStrategyThread(submitCallable);
    }
}
