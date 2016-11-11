package com.jforex.programming.order.task.params;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import com.dukascopy.api.IOrder;

import io.reactivex.functions.Action;

public class ComposeParamsForOrder {

    private Function<IOrder, Action> startAction = o -> () -> {};
    private Function<IOrder, Action> completeAction = o -> () -> {};
    private BiConsumer<Throwable, IOrder> errorConsumer = (t, o) -> {};
    private RetryParams retryParams = new RetryParams(0, 0L);

    public Action startAction(final IOrder order) {
        return startAction.apply(order);
    }

    public Action completeAction(final IOrder order) {
        return completeAction.apply(order);
    }

    public Consumer<Throwable> errorConsumer(final IOrder order) {
        return err -> errorConsumer.accept(err, order);
    }

    public RetryParams retryParams() {
        return retryParams;
    }

    public void setStartAction(final Function<IOrder, Action> startAction) {
        this.startAction = startAction;
    }

    public void setCompleteAction(final Function<IOrder, Action> completeAction) {
        this.completeAction = completeAction;
    }

    public void setErrorConsumer(final BiConsumer<Throwable, IOrder> errorConsumer) {
        this.errorConsumer = errorConsumer;
    }

    public void setRetryParams(final RetryParams retryParams) {
        this.retryParams = retryParams;
    }

    public ComposeParams convertWithOrder(final IOrder order) {
        final ComposeParams composeParams = new ComposeParams();
        composeParams.setStartAction(startAction(order));
        composeParams.setCompleteAction(completeAction(order));
        composeParams.setErrorConsumer(errorConsumer(order));

        return composeParams;
    }
}
