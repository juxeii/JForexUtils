package com.jforex.programming.order.task.params.position;

import static com.google.common.base.Preconditions.checkNotNull;

import com.dukascopy.api.Instrument;
import com.jforex.programming.order.task.CloseExecutionMode;

public class ClosePositionParams extends PositionParamsBase<Instrument> {

    private final MergePositionParams complexMergePositionParams;
    private final SimpleClosePositionParams closePositionParams;
    private final CloseExecutionMode closeExecutionMode;

    public interface BuildOption {

        public SimpleClosePositionParams build();
    }

    private ClosePositionParams(final Builder builder) {
        super(builder);

        complexMergePositionParams = builder.complexMergePositionParams;
        closePositionParams = builder.closePositionParams;
        closeExecutionMode = builder.closeExecutionMode;
        consumerForEvent = complexMergePositionParams.consumerForEvent();
        consumerForEvent.putAll(closePositionParams.consumerForEvent());
    }

    public MergePositionParams complexMergePositionParams() {
        return complexMergePositionParams;
    }

    public SimpleClosePositionParams closePositionParams() {
        return closePositionParams;
    }

    public CloseExecutionMode closeExecutionMode() {
        return closeExecutionMode;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static class Builder extends PositionParamsBuilder<Builder, Instrument> {

        private MergePositionParams complexMergePositionParams;
        private SimpleClosePositionParams closePositionParams;
        private CloseExecutionMode closeExecutionMode;

        public Builder withMergeParams(final MergePositionParams complexMergePositionParams) {
            checkNotNull(complexMergePositionParams);

            this.complexMergePositionParams = complexMergePositionParams;
            return this;
        }

        public Builder withClosePositionParams(final SimpleClosePositionParams closePositionParams) {
            checkNotNull(closePositionParams);

            this.closePositionParams = closePositionParams;
            return this;
        }

        public Builder withCloseExecutionMode(final CloseExecutionMode closeExecutionMode) {
            checkNotNull(closeExecutionMode);

            this.closeExecutionMode = closeExecutionMode;
            return this;
        }

        public ClosePositionParams build() {
            return new ClosePositionParams(this);
        }
    }
}
