package com.jforex.programming.order.task.params;

import java.util.function.Consumer;

import com.dukascopy.api.IOrder;

import io.reactivex.functions.Action;

public interface ComposeDataForOrder {

    public Action startAction(IOrder order);

    public Action completeAction(IOrder order);

    public Consumer<Throwable> errorConsumer(IOrder order);

    public RetryParams retryParams();

    public ComposeParams convertWithOrder(IOrder order);
}
