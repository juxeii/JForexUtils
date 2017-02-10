package com.jforex.programming.order.task.params.basic;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Collection;
import java.util.function.Consumer;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.event.OrderEventType;
import com.jforex.programming.order.task.params.TaskParamsBase;
import com.jforex.programming.order.task.params.TaskParamsType;
import com.jforex.programming.order.task.params.TaskParamsWithType;

public class MergeParams extends TaskParamsWithType {

    private final String mergeOrderLabel;
    private final Collection<IOrder> toMergeOrders;

    private MergeParams(final Builder builder) {
        super(builder);

        this.mergeOrderLabel = builder.mergeOrderLabel;
        this.toMergeOrders = builder.toMergeOrders;
    }

    public String mergeOrderLabel() {
        return mergeOrderLabel;
    }

    public Collection<IOrder> toMergeOrders() {
        return toMergeOrders;
    }

    @Override
    public TaskParamsType type() {
        return TaskParamsType.MERGE;
    }

    public static Builder mergeWith(final String mergeOrderLabel,
                                    final Collection<IOrder> toMergeOrders) {
        checkNotNull(mergeOrderLabel);
        checkNotNull(toMergeOrders);

        return new Builder(mergeOrderLabel, toMergeOrders);
    }

    public static class Builder extends TaskParamsBase.Builder<Builder> {

        private final String mergeOrderLabel;
        private final Collection<IOrder> toMergeOrders;

        public Builder(final String mergeOrderLabel,
                       final Collection<IOrder> toMergeOrders) {
            this.mergeOrderLabel = mergeOrderLabel;
            this.toMergeOrders = toMergeOrders;
        }

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

        public MergeParams build() {
            return new MergeParams(this);
        }
    }
}
