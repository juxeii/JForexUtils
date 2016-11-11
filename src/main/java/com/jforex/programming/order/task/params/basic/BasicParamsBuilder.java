package com.jforex.programming.order.task.params.basic;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.function.Consumer;

import com.jforex.programming.order.task.params.CommonParamsBuilder;
import com.jforex.programming.order.task.params.ComposeParams;
import com.jforex.programming.order.task.params.RetryParams;

import io.reactivex.functions.Action;

@SuppressWarnings("unchecked")
public abstract class BasicParamsBuilder<T> extends CommonParamsBuilder<T> {

    public ComposeParams composeParams = new ComposeParams();

    public T retryOnReject(final int noOfRetries,
                           final long delayInMillis) {
        composeParams.setRetryParams(new RetryParams(noOfRetries, delayInMillis));
        return (T) this;
    }

    public T doOnStart(final Action startAction) {
        checkNotNull(startAction);

        composeParams.setStartAction(startAction);
        return (T) this;
    }

    public T doOnComplete(final Action completeAction) {
        checkNotNull(completeAction);

        composeParams.setCompleteAction(completeAction);
        return (T) this;
    }

    public T doOnError(final Consumer<Throwable> errorConsumer) {
        checkNotNull(errorConsumer);

        composeParams.setErrorConsumer(errorConsumer);
        return (T) this;
    }
}
