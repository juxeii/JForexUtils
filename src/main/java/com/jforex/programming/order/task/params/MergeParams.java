package com.jforex.programming.order.task.params;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Collection;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.event.OrderEventType;
import com.jforex.programming.order.task.BasicTask;

public class MergeParams extends BasicTaskParams {

    private final String mergeOrderLabel;
    private final Collection<IOrder> toMergeOrders;

    private MergeParams(final Builder builder) {
        super(builder);

        this.mergeOrderLabel = builder.mergeOrderLabel;
        this.toMergeOrders = builder.toMergeOrders;
    }

    public void subscribe(final BasicTask basicTask) {
        subscribe(basicTask.mergeOrders(mergeOrderLabel, toMergeOrders));
    }

    public static Builder mergeWith(final String mergeOrderLabel,
                                    final Collection<IOrder> toMergeOrders) {
        checkNotNull(mergeOrderLabel);
        checkNotNull(toMergeOrders);

        return new Builder(mergeOrderLabel, toMergeOrders);
    }

    public static class Builder extends ParamsBuilderBase<Builder> {

        private final String mergeOrderLabel;
        private final Collection<IOrder> toMergeOrders;

        public Builder(final String mergeOrderLabel,
                       final Collection<IOrder> toMergeOrders) {
            this.mergeOrderLabel = mergeOrderLabel;
            this.toMergeOrders = toMergeOrders;
        }

        public Builder doOnMerge(final OrderEventConsumer mergeConsumer) {
            return setEventConsumer(OrderEventType.MERGE_OK, mergeConsumer);
        }

        public Builder doOnMergeClose(final OrderEventConsumer mergeCloseConsumer) {
            return setEventConsumer(OrderEventType.MERGE_CLOSE_OK, mergeCloseConsumer);
        }

        public Builder doOnReject(final OrderEventConsumer rejectConsumer) {
            return setEventConsumer(OrderEventType.MERGE_REJECTED, rejectConsumer);
        }

        public MergeParams build() {
            return new MergeParams(this);
        }
    }
}
