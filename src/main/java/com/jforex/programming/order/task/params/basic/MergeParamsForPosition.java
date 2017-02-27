package com.jforex.programming.order.task.params.basic;

import java.util.function.Consumer;

import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.event.OrderEventType;
import com.jforex.programming.order.task.params.TaskParamsBase;
import com.jforex.programming.order.task.params.TaskParamsType;
import com.jforex.programming.order.task.params.TaskParamsWithType;

public class MergeParamsForPosition extends TaskParamsWithType {

    private MergeParamsForPosition(final Builder builder) {
        super(builder);
    }

    @Override
    public TaskParamsType type() {
        return TaskParamsType.MERGE;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static class Builder extends TaskParamsBase.Builder<Builder> {

        public Builder doOnMerge(final Consumer<OrderEvent> mergeConsumer) {
            setEventConsumer(OrderEventType.MERGE_OK, mergeConsumer);
            return this;
        }

        public Builder doOnMergeClose(final Consumer<OrderEvent> mergeCloseConsumer) {
            setEventConsumer(OrderEventType.MERGE_CLOSE_OK, mergeCloseConsumer);
            return this;
        }

        public Builder doOnReject(final Consumer<OrderEvent> rejectConsumer) {
            setEventConsumer(OrderEventType.MERGE_REJECTED, rejectConsumer);
            return this;
        }

        public MergeParamsForPosition build() {
            return new MergeParamsForPosition(this);
        }
    }
}
