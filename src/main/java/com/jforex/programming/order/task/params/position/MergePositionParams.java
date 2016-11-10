package com.jforex.programming.order.task.params.position;

import static com.google.common.base.Preconditions.checkNotNull;

import com.dukascopy.api.Instrument;
import com.jforex.programming.order.task.params.basic.BasicParamsBuilder;

public class MergePositionParams extends PositionParamsBase {

    private final BatchCancelSLTPParams batchCancelSLTPParams;
    private final SimpleMergePositionParams simpleMergePositionParams;

    private MergePositionParams(final Builder builder) {
        super(builder);

        instrument = builder.instrument;
        batchCancelSLTPParams = builder.batchCancelSLTPParams;
        simpleMergePositionParams = builder.simpleMergePositionParams;
    }

    public BatchCancelSLTPParams batchCancelSLTPParams() {
        return batchCancelSLTPParams;
    }

    public SimpleMergePositionParams simpleMergePositionParams() {
        return simpleMergePositionParams;
    }

    public static Builder newBuilder(final Instrument instrument,
                                     final SimpleMergePositionParams simpleMergePositionParams) {
        checkNotNull(instrument);
        checkNotNull(simpleMergePositionParams);

        return new Builder(instrument, simpleMergePositionParams);
    }

    public static class Builder extends BasicParamsBuilder<Builder> {

        private final Instrument instrument;
        private final SimpleMergePositionParams simpleMergePositionParams;
        private BatchCancelSLTPParams batchCancelSLTPParams = BatchCancelSLTPParams
            .newBuilder()
            .build();

        public Builder(final Instrument instrument,
                       final SimpleMergePositionParams simpleMergePositionParams) {
            this.instrument = instrument;
            this.simpleMergePositionParams = simpleMergePositionParams;
        }

        public Builder withBatchCancelSLTPParams(final BatchCancelSLTPParams batchCancelSLTPParams) {
            checkNotNull(batchCancelSLTPParams);

            this.batchCancelSLTPParams = batchCancelSLTPParams;
            return this;
        }

        public MergePositionParams build() {
            return new MergePositionParams(this);
        }
    }
}
