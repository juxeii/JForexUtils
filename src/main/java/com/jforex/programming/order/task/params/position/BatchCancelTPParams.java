package com.jforex.programming.order.task.params.position;

import static com.google.common.base.Preconditions.checkNotNull;

import com.dukascopy.api.Instrument;
import com.jforex.programming.order.task.BatchMode;

public class BatchCancelTPParams extends PositionParamsBase<Instrument> {

    private final CancelTPParams cancelTPParams;
    private final BatchMode batchMode;

    private BatchCancelTPParams(final Builder builder) {
        super(builder);

        cancelTPParams = builder.cancelTPParams;
        batchMode = builder.batchMode;
        consumerForEvent = cancelTPParams.consumerForEvent();
    }

    public final CancelTPParams cancelTPParams() {
        return cancelTPParams;
    }

    public final BatchMode batchMode() {
        return batchMode;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static class Builder extends PositionParamsBuilder<Builder, Instrument> {

        private CancelTPParams cancelTPParams = CancelTPParams
            .newBuilder()
            .build();
        private BatchMode batchMode = BatchMode.MERGE;

        public Builder withCancelTPParams(final CancelTPParams cancelTPParams) {
            checkNotNull(cancelTPParams);

            this.cancelTPParams = cancelTPParams;
            return this;
        }

        public Builder withBatchMode(final BatchMode batchMode) {
            checkNotNull(batchMode);

            this.batchMode = batchMode;
            return this;
        }

        public BatchCancelTPParams build() {
            return new BatchCancelTPParams(this);
        }
    }
}
