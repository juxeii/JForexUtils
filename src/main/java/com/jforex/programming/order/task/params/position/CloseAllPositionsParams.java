package com.jforex.programming.order.task.params.position;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.function.Function;

import com.dukascopy.api.Instrument;
import com.jforex.programming.order.task.params.TaskParamsBase;
import com.jforex.programming.order.task.params.TaskParamsType;
import com.jforex.programming.order.task.params.TaskParamsWithType;

public class CloseAllPositionsParams extends TaskParamsWithType {

    private final Function<Instrument, ClosePositionParams> closePositonParamsFactory;

    private CloseAllPositionsParams(final Builder builder) {
        super(builder);

        closePositonParamsFactory = builder.closePositonParamsFactory;
    }

    public Function<Instrument, ClosePositionParams> closePositonParamsFactory() {
        return closePositonParamsFactory;
    }

    @Override
    public TaskParamsType type() {
        return TaskParamsType.CLOSEALLPOSITIONS;
    }

    public static Builder newBuilder(final Function<Instrument, ClosePositionParams> closePositonParamsFactory) {
        checkNotNull(closePositonParamsFactory);

        return new Builder(closePositonParamsFactory);
    }

    public static class Builder extends TaskParamsBase.Builder<Builder> {

        private final Function<Instrument, ClosePositionParams> closePositonParamsFactory;

        public Builder(final Function<Instrument, ClosePositionParams> closePositonParamsFactory) {
            this.closePositonParamsFactory = closePositonParamsFactory;
        }

        @Override
        public CloseAllPositionsParams build() {
            return new CloseAllPositionsParams(this);
        }
    }
}
