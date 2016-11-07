package com.jforex.programming.order.task.params.position;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Map;
import java.util.function.Consumer;

import com.dukascopy.api.Instrument;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.event.OrderEventType;

public class MergePositionParams extends PositionParamsBase<Instrument> {

    private final BatchCancelSLTPParams batchCancelSLAndTPParams;
    private final SimpleMergePositionParams mergePositionParams;
    private final Map<OrderEventType, Consumer<OrderEvent>> consumerForEvent;

    private MergePositionParams(final Builder builder) {
        super(builder);

        batchCancelSLAndTPParams = builder.batchCancelSLAndTPParams;
        mergePositionParams = builder.mergePositionParams;
        consumerForEvent = batchCancelSLAndTPParams.consumerForEvent();
        consumerForEvent.putAll(mergePositionParams.consumerForEvent());
    }

    public BatchCancelSLTPParams batchCancelSLAndTPParams() {
        return batchCancelSLAndTPParams;
    }

    public SimpleMergePositionParams mergePositionParams() {
        return mergePositionParams;
    }

    public static Builder withMergeParams(final SimpleMergePositionParams mergePositionParams) {
        checkNotNull(mergePositionParams);

        return new Builder(mergePositionParams);
    }

    public static class Builder extends PositionParamsBuilder<Builder, Instrument> {

        private final BatchCancelSLTPParams batchCancelSLAndTPParams =
                BatchCancelSLTPParams.newBuilder().build();
        private SimpleMergePositionParams mergePositionParams;

        public Builder(final SimpleMergePositionParams mergePositionParams) {
            this.mergePositionParams = mergePositionParams;
        }

        public Builder withMergeParams(final SimpleMergePositionParams mergePositionParams) {
            checkNotNull(mergePositionParams);

            this.mergePositionParams = mergePositionParams;
            return this;
        }

        public MergePositionParams build() {
            return new MergePositionParams(this);
        }
    }
}
