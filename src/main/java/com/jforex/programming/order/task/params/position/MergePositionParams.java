package com.jforex.programming.order.task.params.position;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Map;
import java.util.function.Consumer;

import com.dukascopy.api.Instrument;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.event.OrderEventType;

public class MergePositionParams extends PositionParamsBase<Instrument> {

    private final BatchCancelSLTPParams batchCancelSLTPParams;
    private final SimpleMergePositionParams simpleMergePositionParams;
    private final Map<OrderEventType, Consumer<OrderEvent>> consumerForEvent;

    private MergePositionParams(final Builder builder) {
        super(builder);

        batchCancelSLTPParams = builder.batchCancelSLTPParams;
        simpleMergePositionParams = builder.simpleMergePositionParams;
        consumerForEvent = batchCancelSLTPParams.consumerForEvent();
        consumerForEvent.putAll(simpleMergePositionParams.consumerForEvent());
    }

    public BatchCancelSLTPParams batchCancelSLTPParams() {
        return batchCancelSLTPParams;
    }

    public SimpleMergePositionParams simpleMergePositionParams() {
        return simpleMergePositionParams;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static class Builder extends PositionParamsBuilder<Builder, Instrument> {

        private BatchCancelSLTPParams batchCancelSLTPParams = BatchCancelSLTPParams
            .newBuilder()
            .build();
        private SimpleMergePositionParams simpleMergePositionParams =
                SimpleMergePositionParams.mergeWithLabel(inst -> "").build();

        public Builder withBatchCancelSLTPParams(final BatchCancelSLTPParams batchCancelSLTPParams) {
            checkNotNull(batchCancelSLTPParams);

            this.batchCancelSLTPParams = batchCancelSLTPParams;
            return this;
        }

        public Builder withSimpleMergePositionParams(final SimpleMergePositionParams simpleMergePositionParams) {
            checkNotNull(simpleMergePositionParams);

            this.simpleMergePositionParams = simpleMergePositionParams;
            return this;
        }

        public MergePositionParams build() {
            return new MergePositionParams(this);
        }
    }
}
