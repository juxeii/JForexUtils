package com.jforex.programming.order.task.params.basic;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Collection;
import java.util.function.Consumer;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.event.OrderEventType;
import com.jforex.programming.order.task.params.CommonParamsBase;
import com.jforex.programming.order.task.params.TaskParamsType;

public class MergeParams extends CommonParamsBase {

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

    public static Builder mergeWith(final String mergeOrderLabel,
                                    final Collection<IOrder> toMergeOrders) {
        checkNotNull(mergeOrderLabel);
        checkNotNull(toMergeOrders);

        return new Builder(mergeOrderLabel, toMergeOrders);
    }

    public static class Builder extends BasicParamsBuilder<Builder> {

        private final String mergeOrderLabel;
        private final Collection<IOrder> toMergeOrders;

        public Builder(final String mergeOrderLabel,
                       final Collection<IOrder> toMergeOrders) {
            this.mergeOrderLabel = mergeOrderLabel;
            this.toMergeOrders = toMergeOrders;
        }

        public Builder doOnMerge(final Consumer<OrderEvent> mergeConsumer) {
            return setEventConsumer(OrderEventType.MERGE_OK, mergeConsumer);
        }

        public Builder doOnMergeClose(final Consumer<OrderEvent> mergeCloseConsumer) {
            return setEventConsumer(OrderEventType.MERGE_CLOSE_OK, mergeCloseConsumer);
        }

        public Builder doOnReject(final Consumer<OrderEvent> rejectConsumer) {
            return setEventConsumer(OrderEventType.MERGE_REJECTED, rejectConsumer);
        }

        public MergeParams build() {
            return new MergeParams(this);
        }
    }

    @Override
    public TaskParamsType type() {
        return TaskParamsType.MERGE;
    }
}
