package com.jforex.programming.order;

import static com.jforex.programming.order.OrderStaticUtil.runnableToCallable;

import java.util.Collection;
import java.util.concurrent.Callable;

import com.dukascopy.api.IOrder;
import com.jforex.programming.misc.IEngineUtil;
import com.jforex.programming.misc.JFRunnable;
import com.jforex.programming.misc.TaskExecutor;

import io.reactivex.Completable;
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
        final Callable<IOrder> submitCallable = engineUtil.submitCallable(orderParams);
        return single(submitCallable);
    }

    public Single<IOrder> mergeOrders(final String mergeOrderLabel,
                                      final Collection<IOrder> toMergeOrders) {
        final Callable<IOrder> mergeCallable = engineUtil.mergeCallable(mergeOrderLabel, toMergeOrders);
        return single(mergeCallable);
    }

    public Completable close(final IOrder order) {
        return completable(() -> order.close());
    }

    public Completable setLabel(final IOrder order,
                                final String label) {
        return completable(() -> order.setLabel(label));
    }

    public Completable setGoodTillTime(final IOrder order,
                                       final long newGTT) {
        return completable(() -> order.setGoodTillTime(newGTT));
    }

    public Completable setRequestedAmount(final IOrder order,
                                          final double newRequestedAmount) {
        return completable(() -> order.setRequestedAmount(newRequestedAmount));
    }

    public Completable setOpenPrice(final IOrder order,
                                    final double newOpenPrice) {
        return completable(() -> order.setOpenPrice(newOpenPrice));
    }

    public Completable setStopLossPrice(final IOrder order,
                                        final double newSL) {
        return completable(() -> order.setStopLossPrice(newSL));
    }

    public Completable setTakeProfitPrice(final IOrder order,
                                          final double newTP) {
        return completable(() -> order.setTakeProfitPrice(newTP));
    }

    private Single<IOrder> single(final Callable<IOrder> callable) {
        return taskExecutor.onStrategyThread(callable);
    }

    private Completable completable(final JFRunnable jfRunnable) {
        return taskExecutor
            .onStrategyThread(runnableToCallable(jfRunnable))
            .toCompletable();
    }
}
