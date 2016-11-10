package com.jforex.programming.order.task.params.position;

import static com.google.common.base.Preconditions.checkNotNull;

import com.dukascopy.api.Instrument;
import com.jforex.programming.order.task.CloseExecutionMode;
import com.jforex.programming.order.task.params.basic.BasicParamsBuilder;

public class ClosePositionParams extends PositionParamsBase {

    private final MergePositionParams mergePositionParams;
    private final SimpleClosePositionParams simpleClosePositionParams;
    private final CloseExecutionMode closeExecutionMode;

    private ClosePositionParams(final Builder builder) {
        super(builder);

        instrument = builder.instrument;
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

    public static Builder newBuilder(final Instrument instrument,
                                     final MergePositionParams mergePositionParams) {
        checkNotNull(instrument);
        checkNotNull(mergePositionParams);

        return new Builder(instrument, mergePositionParams);
    }

    public static class Builder extends BasicParamsBuilder<Builder> {

        private final Instrument instrument;
        private SimpleClosePositionParams simpleClosePositionParams = SimpleClosePositionParams
            .newBuilder()
            .build();
        private final MergePositionParams mergePositionParams;
        private CloseExecutionMode closeExecutionMode = CloseExecutionMode.CloseAll;

        public Builder(final Instrument instrument,
                       final MergePositionParams mergePositionParams) {
            this.instrument = instrument;
            this.mergePositionParams = mergePositionParams;
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
