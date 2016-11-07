package com.jforex.programming.order.task.params.position;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Map;
import java.util.function.Consumer;

import com.dukascopy.api.Instrument;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.event.OrderEventType;

public class ComplexMergePositionParams extends PositionParamsBase<Instrument> {

    private final BatchCancelSLAndTPParams batchCancelSLAndTPParams;
    private final MergePositionParams mergePositionParams;
    private final Map<OrderEventType, Consumer<OrderEvent>> consumerForEvent;

    private ComplexMergePositionParams(final Builder builder) {
        super(builder);

        batchCancelSLAndTPParams = builder.batchCancelSLAndTPParams;
        mergePositionParams = builder.mergePositionParams;
        consumerForEvent = batchCancelSLAndTPParams.consumerForEvent();
        consumerForEvent.putAll(mergePositionParams.consumerForEvent());
    }

    public BatchCancelSLAndTPParams batchCancelSLAndTPParams() {
        return batchCancelSLAndTPParams;
    }

    public MergePositionParams mergePositionParams() {
        return mergePositionParams;
    }

    public static Builder withMergeParams(final MergePositionParams mergePositionParams) {
        checkNotNull(mergePositionParams);

        return new Builder(mergePositionParams);
    }

    public static class Builder extends PositionParamsBuilder<Builder, Instrument> {

        private final BatchCancelSLAndTPParams batchCancelSLAndTPParams =
                BatchCancelSLAndTPParams.newBuilder().build();
        private MergePositionParams mergePositionParams;

        public Builder(final MergePositionParams mergePositionParams) {
            this.mergePositionParams = mergePositionParams;
        }

        public Builder withMergeParams(final MergePositionParams mergePositionParams) {
            checkNotNull(mergePositionParams);

            this.mergePositionParams = mergePositionParams;
            return this;
        }

        public ComplexMergePositionParams build() {
            return new ComplexMergePositionParams(this);
        }
    }
}
