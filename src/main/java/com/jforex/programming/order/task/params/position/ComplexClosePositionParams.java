package com.jforex.programming.order.task.params.position;

import static com.google.common.base.Preconditions.checkNotNull;

import com.dukascopy.api.Instrument;
import com.jforex.programming.order.task.CloseExecutionMode;

public class ComplexClosePositionParams extends PositionParamsBase<Instrument> {

    private final ComplexMergePositionParams complexMergePositionParams;
    private final ClosePositionParams closePositionParams;
    private final CloseExecutionMode closeExecutionMode;

    public interface BuildOption {

        public ClosePositionParams build();
    }

    private ComplexClosePositionParams(final Builder builder) {
        super(builder);

        complexMergePositionParams = builder.complexMergePositionParams;
        closePositionParams = builder.closePositionParams;
        closeExecutionMode = builder.closeExecutionMode;
        consumerForEvent = complexMergePositionParams.consumerForEvent();
        consumerForEvent.putAll(closePositionParams.consumerForEvent());
    }

    public ComplexMergePositionParams complexMergePositionParams() {
        return complexMergePositionParams;
    }

    public ClosePositionParams closePositionParams() {
        return closePositionParams;
    }

    public CloseExecutionMode closeExecutionMode() {
        return closeExecutionMode;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static class Builder extends PositionParamsBuilder<Builder, Instrument> {

        private ComplexMergePositionParams complexMergePositionParams;
        private ClosePositionParams closePositionParams;
        private CloseExecutionMode closeExecutionMode;

        public Builder withMergeParams(final ComplexMergePositionParams complexMergePositionParams) {
            checkNotNull(complexMergePositionParams);

            this.complexMergePositionParams = complexMergePositionParams;
            return this;
        }

        public Builder withClosePositionParams(final ClosePositionParams closePositionParams) {
            checkNotNull(closePositionParams);

            this.closePositionParams = closePositionParams;
            return this;
        }

        public Builder withCloseExecutionMode(final CloseExecutionMode closeExecutionMode) {
            checkNotNull(closeExecutionMode);

            this.closeExecutionMode = closeExecutionMode;
            return this;
        }

        public ComplexClosePositionParams build() {
            return new ComplexClosePositionParams(this);
        }
    }
}
