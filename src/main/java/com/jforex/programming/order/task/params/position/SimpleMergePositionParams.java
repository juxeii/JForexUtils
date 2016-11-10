package com.jforex.programming.order.task.params.position;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.function.Consumer;

import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.event.OrderEventType;
import com.jforex.programming.order.task.params.basic.BasicParamsBuilder;

public class SimpleMergePositionParams extends PositionParamsBase {

    private final String mergeOrderLabel;

    private SimpleMergePositionParams(final Builder builder) {
        super(builder);

        mergeOrderLabel = builder.mergeOrderLabel;
    }

    public String mergeOrderLabel() {
        return mergeOrderLabel;
    }

    public static Builder mergeWithLabel(final String mergeOrderLabel) {
        checkNotNull(mergeOrderLabel);

        return new Builder(mergeOrderLabel);
    }

    public static class Builder extends BasicParamsBuilder<Builder> {

        private final String mergeOrderLabel;

        public Builder(final String mergeOrderLabel) {
            this.mergeOrderLabel = mergeOrderLabel;
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

        public SimpleMergePositionParams build() {
            return new SimpleMergePositionParams(this);
        }
    }
}
