package com.jforex.programming.order.task.params;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.event.OrderEventType;

import io.reactivex.functions.Action;

public abstract class TaskParamsBase {

    private final ComposeData composeData;
    private final Map<OrderEventType, Consumer<OrderEvent>> consumerForEvent;

    @SuppressWarnings("unchecked")
    protected TaskParamsBase(final Builder builder) {
        composeData = builder.composeParams;
        consumerForEvent = builder.consumerForEvent;
    }

    public ComposeData composeData() {
        return composeData;
    }

    public Map<OrderEventType, Consumer<OrderEvent>> consumerForEvent() {
        return consumerForEvent;
    }

    public abstract static class Builder<T extends Builder<T>> {

        public ComposeParams composeParams = new ComposeParams();
        public Map<OrderEventType, Consumer<OrderEvent>> consumerForEvent = new HashMap<>();

        public T doOnStart(final Action startAction) {
            checkNotNull(startAction);

            composeParams.setStartAction(startAction);
            return getThis();
        }

        public T doOnComplete(final Action completeAction) {
            checkNotNull(completeAction);

            composeParams.setCompleteAction(completeAction);
            return getThis();
        }

        public T doOnError(final Consumer<Throwable> errorConsumer) {
            checkNotNull(errorConsumer);

            composeParams.setErrorConsumer(errorConsumer);
            return getThis();
        }

        public T retryOnReject(final RetryParams retryParams) {
            composeParams.setRetryParams(retryParams);
            return getThis();
        }

        protected T setEventConsumer(final OrderEventType orderEventType,
                                     final Consumer<OrderEvent> consumer) {
            checkNotNull(consumer);

            consumerForEvent.put(orderEventType, consumer);
            return getThis();
        }

        public abstract T getThis();
    }
}
