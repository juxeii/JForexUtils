package com.jforex.programming.order.task.params.position;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.function.Function;

import com.dukascopy.api.Instrument;
import com.jforex.programming.order.task.params.TaskParamsBase;
import com.jforex.programming.order.task.params.TaskParamsType;
import com.jforex.programming.order.task.params.TaskParamsWithType;

public class CloseAllPositionsParams extends TaskParamsWithType {

    private final Function<Instrument, ClosePositionParams> closeParamsFactory;

    private CloseAllPositionsParams(final Builder builder) {
        super(builder);

        closeParamsFactory = builder.closeParamsFactory;
    }

    public ClosePositionParams createClosePositionParams(final Instrument instrument) {
        return closeParamsFactory.apply(instrument);
    }

    @Override
    public TaskParamsType type() {
        return TaskParamsType.CLOSEALLPOSITIONS;
    }

    public static Builder newBuilder(final Function<Instrument, ClosePositionParams> closeParamsFactory) {
        checkNotNull(closeParamsFactory);

        return new Builder(closeParamsFactory);
    }

    public static class Builder extends TaskParamsBase.Builder<Builder> {

        private final Function<Instrument, ClosePositionParams> closeParamsFactory;

        public Builder(final Function<Instrument, ClosePositionParams> closeParamsFactory) {
            this.closeParamsFactory = closeParamsFactory;
        }

        public CloseAllPositionsParams build() {
            return new CloseAllPositionsParams(this);
        }

        @Override
        public Builder getThis() {
            return this;
        }
    }
}
