package com.jforex.programming.order.task.params.position;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.function.Function;

import com.dukascopy.api.Instrument;
import com.jforex.programming.order.task.params.TaskParamsBase;
import com.jforex.programming.order.task.params.TaskParamsType;
import com.jforex.programming.order.task.params.TaskParamsWithType;

public class CloseAllPositionsParams extends TaskParamsWithType {

    private final Function<Instrument, ClosePositionParams> paramsFactory;

    private CloseAllPositionsParams(final Builder builder) {
        super(builder);

        paramsFactory = builder.paramsFactory;
    }

    public ClosePositionParams paramsForInstrument(final Instrument instrument) {
        return paramsFactory.apply(instrument);
    }

    @Override
    public TaskParamsType type() {
        return TaskParamsType.CLOSEALLPOSITIONS;
    }

    public static Builder newBuilder(final Function<Instrument, ClosePositionParams> paramsFactory) {
        checkNotNull(paramsFactory);

        return new Builder(paramsFactory);
    }

    public static class Builder extends TaskParamsBase.Builder<Builder> {

        private final Function<Instrument, ClosePositionParams> paramsFactory;

        public Builder(final Function<Instrument, ClosePositionParams> paramsFactory) {
            this.paramsFactory = paramsFactory;
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
