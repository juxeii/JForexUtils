package com.jforex.programming.order.task.params.position;

import static com.google.common.base.Preconditions.checkNotNull;

import com.dukascopy.api.Instrument;

public class MergeAllPositionParams extends PositionParamsBase<Instrument> {

    private final ComplexMergePositionParams complexMergePositionParams;

    private MergeAllPositionParams(final Builder builder) {
        super(builder);

        complexMergePositionParams = builder.complexMergePositionParams;
        consumerForEvent = complexMergePositionParams.consumerForEvent();
    }

    public ComplexMergePositionParams complexMergePositionParams() {
        return complexMergePositionParams;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static class Builder extends PositionParamsBuilder<Builder, Instrument> {

        private ComplexMergePositionParams complexMergePositionParams;

        public Builder withClosePositionParams(final ComplexMergePositionParams complexMergePositionParams) {
            checkNotNull(complexMergePositionParams);

            this.complexMergePositionParams = complexMergePositionParams;
            return this;
        }

        public MergeAllPositionParams build() {
            return new MergeAllPositionParams(this);
        }
    }
}
