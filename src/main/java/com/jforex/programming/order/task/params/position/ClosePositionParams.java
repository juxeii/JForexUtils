package com.jforex.programming.order.task.params.position;

import static com.google.common.base.Preconditions.checkNotNull;

import com.dukascopy.api.Instrument;
import com.jforex.programming.order.task.CloseExecutionMode;

public class ClosePositionParams extends PositionParamsBase<Instrument> {

    private final MergePositionParams mergePositionParams;
    private final SimpleClosePositionParams simpleClosePositionParams;
    private final CloseExecutionMode closeExecutionMode;

    private ClosePositionParams(final Builder builder) {
        super(builder);

        mergePositionParams = builder.mergePositionParams;
        simpleClosePositionParams = builder.simpleClosePositionParams;
        closeExecutionMode = builder.closeExecutionMode;
        consumerForEvent = mergePositionParams.consumerForEvent();
        consumerForEvent.putAll(simpleClosePositionParams.consumerForEvent());
    }

    public MergePositionParams mergePositionParams() {
        return mergePositionParams;
    }

    public SimpleClosePositionParams simpleClosePositionParams() {
        return simpleClosePositionParams;
    }

    public CloseExecutionMode closeExecutionMode() {
        return closeExecutionMode;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static class Builder extends PositionParamsBuilder<Builder, Instrument> {

        private MergePositionParams mergePositionParams = MergePositionParams.newBuilder().build();
        private SimpleClosePositionParams simpleClosePositionParams = SimpleClosePositionParams.newBuilder().build();
        private CloseExecutionMode closeExecutionMode = CloseExecutionMode.CloseAll;

        public Builder withMergePositionParams(final MergePositionParams mergePositionParams) {
            checkNotNull(mergePositionParams);

            this.mergePositionParams = mergePositionParams;
            return this;
        }

        public Builder withClosePositionParams(final SimpleClosePositionParams simpleClosePositionParams) {
            checkNotNull(simpleClosePositionParams);

            this.simpleClosePositionParams = simpleClosePositionParams;
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
