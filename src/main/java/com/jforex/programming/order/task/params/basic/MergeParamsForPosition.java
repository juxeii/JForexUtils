package com.jforex.programming.order.task.params.basic;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.function.Consumer;

import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.event.OrderEventType;
import com.jforex.programming.order.task.params.TaskParamsBase;
import com.jforex.programming.order.task.params.TaskParamsType;
import com.jforex.programming.order.task.params.TaskParamsWithType;

public class MergeParamsForPosition extends TaskParamsWithType {

    private final String mergeOrderLabel;

    private MergeParamsForPosition(final Builder builder) {
        super(builder);

        this.mergeOrderLabel = builder.mergeOrderLabel;
    }

    public String mergeOrderLabel() {
        return mergeOrderLabel;
    }

    @Override
    public TaskParamsType type() {
        return TaskParamsType.MERGE;
    }

    public static Builder withLabel(final String mergeOrderLabel) {
        checkNotNull(mergeOrderLabel);

        return new Builder(mergeOrderLabel);
    }

    public static class Builder extends TaskParamsBase.Builder<Builder> {

        private final String mergeOrderLabel;

        public Builder(final String mergeOrderLabel) {
            this.mergeOrderLabel = mergeOrderLabel;
        }

        public Builder doOnMerge(final Consumer<OrderEvent> mergeConsumer) {
            setEventConsumer(OrderEventType.MERGE_OK, mergeConsumer);
            return getThis();
        }

        public Builder doOnMergeClose(final Consumer<OrderEvent> mergeCloseConsumer) {
            setEventConsumer(OrderEventType.MERGE_CLOSE_OK, mergeCloseConsumer);
            return getThis();
        }

        public Builder doOnReject(final Consumer<OrderEvent> rejectConsumer) {
            setEventConsumer(OrderEventType.MERGE_REJECTED, rejectConsumer);
            return getThis();
        }

        public MergeParamsForPosition build() {
            return new MergeParamsForPosition(this);
        }

        @Override
        protected Builder getThis() {
            return this;
        }
    }
}
