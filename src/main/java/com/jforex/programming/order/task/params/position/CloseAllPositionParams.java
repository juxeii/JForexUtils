package com.jforex.programming.order.task.params.position;

import static com.google.common.base.Preconditions.checkNotNull;

import com.dukascopy.api.Instrument;

public class CloseAllPositionParams extends PositionParamsBase<Instrument> {

    private final ComplexClosePositionParams complexClosePositionParams;

    private CloseAllPositionParams(final Builder builder) {
        super(builder);

        complexClosePositionParams = builder.complexClosePositionParams;
        consumerForEvent = complexClosePositionParams.consumerForEvent();
    }

    public ComplexClosePositionParams complexClosePositionParams() {
        return complexClosePositionParams;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static class Builder extends PositionParamsBuilder<Builder, Instrument> {

        private ComplexClosePositionParams complexClosePositionParams;

        public Builder withClosePositionParams(final ComplexClosePositionParams complexClosePositionParams) {
            checkNotNull(complexClosePositionParams);

            this.complexClosePositionParams = complexClosePositionParams;
            return this;
        }

        public CloseAllPositionParams build() {
            return new CloseAllPositionParams(this);
        }
    }
}
