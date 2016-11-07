package com.jforex.programming.order.task.params.position;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.function.Consumer;
import java.util.function.Function;

import com.dukascopy.api.Instrument;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.event.OrderEventType;

public class SimpleMergePositionParams extends PositionParamsBase<Instrument> {

    private final Function<Instrument, String> mergeOrderLabelSupplier;

    private SimpleMergePositionParams(final Builder builder) {
        super(builder);

        mergeOrderLabelSupplier = builder.mergeOrderLabelSupplier;
    }

    public String mergeOrderLabel(final Instrument instrument) {
        return mergeOrderLabelSupplier.apply(instrument);
    }

    public static Builder mergeWithLabel(final Function<Instrument, String> mergeOrderLabelSupplier) {
        checkNotNull(mergeOrderLabelSupplier);

        return new Builder(mergeOrderLabelSupplier);
    }

    public static class Builder extends PositionParamsBuilder<Builder, Instrument> {

        private final Function<Instrument, String> mergeOrderLabelSupplier;

        public Builder(final Function<Instrument, String> mergeOrderLabelSupplier) {
            this.mergeOrderLabelSupplier = mergeOrderLabelSupplier;
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
