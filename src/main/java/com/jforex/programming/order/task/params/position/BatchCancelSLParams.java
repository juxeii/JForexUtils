package com.jforex.programming.order.task.params.position;

import static com.google.common.base.Preconditions.checkNotNull;

import com.dukascopy.api.Instrument;
import com.jforex.programming.order.task.BatchMode;

public class BatchCancelSLParams extends PositionParamsBase<Instrument> {

    private final CancelSLParams cancelSLParams;
    private final BatchMode batchMode;

    private BatchCancelSLParams(final Builder builder) {
        super(builder);

        cancelSLParams = builder.cancelSLParams;
        batchMode = builder.batchMode;
        consumerForEvent = cancelSLParams.consumerForEvent();
    }

    public final CancelSLParams cancelSLParams() {
        return cancelSLParams;
    }

    public final BatchMode batchMode() {
        return batchMode;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static class Builder extends PositionParamsBuilder<Builder, Instrument> {

        private CancelSLParams cancelSLParams = CancelSLParams.newBuilder().build();
        private BatchMode batchMode = BatchMode.MERGE;

        public Builder withCancelSLParams(final CancelSLParams cancelSLParams) {
            checkNotNull(cancelSLParams);

            this.cancelSLParams = cancelSLParams;
            return this;
        }

        public Builder withBatchMode(final BatchMode batchMode) {
            checkNotNull(batchMode);

            this.batchMode = batchMode;
            return this;
        }

        public BatchCancelSLParams build() {
            return new BatchCancelSLParams(this);
        }
    }
}
