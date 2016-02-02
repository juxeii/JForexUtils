package com.jforex.programming.order.call;

import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import com.dukascopy.api.IOrder;
import com.dukascopy.api.JFException;
import com.jforex.programming.misc.ConcurrentUtil;

public class OrderCallExecutor {

    private final ConcurrentUtil concurrentUtil;

    private interface ExecutorOrderCall {
        abstract IOrder run() throws JFException, InterruptedException, ExecutionException;
    }

    public OrderCallExecutor(final ConcurrentUtil concurrentUtil) {
        this.concurrentUtil = concurrentUtil;
    }

    public OrderCallExecutorResult run(final OrderSupplierCall orderSupplierCall) {
        return ConcurrentUtil.isStrategyThread()
                ? execute(() -> orderSupplierCall.get())
                : execute(strategyThreadCall(() -> orderSupplierCall.get()));
    }

    private final ExecutorOrderCall strategyThreadCall(final Callable<IOrder> orderCallable) {
        return () -> concurrentUtil.executeOnStrategyThread(orderCallable).get();
    }

    private final OrderCallExecutorResult execute(final ExecutorOrderCall executorOrderCall) {
        try {
            final IOrder order = executorOrderCall.run();
            return new OrderCallExecutorResult(Optional.ofNullable(order), Optional.empty());
        } catch (JFException | InterruptedException | ExecutionException exception) {
            return new OrderCallExecutorResult(Optional.empty(), Optional.of(exception));
        }
    }
}
