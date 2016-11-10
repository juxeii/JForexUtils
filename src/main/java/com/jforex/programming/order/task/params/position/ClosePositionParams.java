package com.jforex.programming.order.task.params.position;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.function.Function;

import com.dukascopy.api.IOrder;
import com.dukascopy.api.Instrument;
import com.jforex.programming.order.task.CloseExecutionMode;
import com.jforex.programming.order.task.params.basic.BasicParamsBuilder;
import com.jforex.programming.order.task.params.basic.CloseParams;

public class ClosePositionParams extends PositionParamsBase {

    private final MergePositionParams mergePositionParams;
    private final Function<IOrder, CloseParams> closeParamsFactory;
    private final CloseExecutionMode closeExecutionMode;

    private ClosePositionParams(final Builder builder) {
        super(builder);

        instrument = builder.instrument;
        mergePositionParams = builder.mergePositionParams;
        closeParamsFactory = builder.closeParamsFactory;
        closeExecutionMode = builder.closeExecutionMode;
    }

    public MergePositionParams mergePositionParams() {
        return mergePositionParams;
    }

    public Function<IOrder, CloseParams> closeParamsFactory() {
        return closeParamsFactory;
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
        private Function<IOrder, CloseParams> closeParamsFactory =
                order -> CloseParams.withOrder(order).build();
        private final MergePositionParams mergePositionParams;
        private CloseExecutionMode closeExecutionMode = CloseExecutionMode.CloseAll;

        public Builder(final Instrument instrument,
                       final MergePositionParams mergePositionParams) {
            this.instrument = instrument;
            this.mergePositionParams = mergePositionParams;
        }

        public Builder withCloseParams(final Function<IOrder, CloseParams> closeParamsFactory) {
            checkNotNull(closeParamsFactory);

            this.closeParamsFactory = closeParamsFactory;
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
