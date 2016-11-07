package com.jforex.programming.order.task.params.position;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.function.Consumer;

import com.dukascopy.api.Instrument;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.event.OrderEventType;

public class MergePositionParams extends PositionParamsBase<Instrument> {

    private final String mergeOrderLabel;

    private MergePositionParams(final Builder builder) {
        super(builder);

        mergeOrderLabel = builder.mergeOrderLabel;
    }

    public String mergeOrderLabel() {
        return mergeOrderLabel;
    }

    public static Builder mergeWith(final String mergeOrderLabel) {
        checkNotNull(mergeOrderLabel);

        return new Builder(mergeOrderLabel);
    }

    public static class Builder extends PositionParamsBuilder<Builder, Instrument> {

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

        public MergePositionParams build() {
            return new MergePositionParams(this);
        }
    }
}
