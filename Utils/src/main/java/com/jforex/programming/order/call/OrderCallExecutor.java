package com.jforex.programming.order.call;

import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import com.jforex.programming.misc.ConcurrentUtil;

import com.dukascopy.api.IOrder;
import com.dukascopy.api.JFException;

public class OrderCallExecutor {

    private final ConcurrentUtil concurrentUtil;

    private interface ExecutorOrderCall {
        abstract IOrder run() throws JFException, InterruptedException, ExecutionException;
    }

    public OrderCallExecutor(final ConcurrentUtil concurrentUtil) {
        this.concurrentUtil = concurrentUtil;
    }

    public OrderExecutorResult run(final OrderCall orderCall) {
        return ConcurrentUtil.isStrategyThread()
                ? execute(() -> orderCall.run())
                : execute(strategyThreadCall(orderCall));
    }

    private final ExecutorOrderCall strategyThreadCall(final OrderCall orderCall) {
        final Callable<IOrder> orderCallable = () -> orderCall.run();
        return () -> concurrentUtil.executeOnStrategyThread(orderCallable).get();
    }

    private final OrderExecutorResult execute(final ExecutorOrderCall executorOrderCall) {
        try {
            final IOrder order = executorOrderCall.run();
            return new OrderExecutorResult(Optional.ofNullable(order), Optional.empty());
        } catch (JFException | InterruptedException | ExecutionException exception) {
            return new OrderExecutorResult(Optional.empty(), Optional.of(exception));
        }
    }
}
