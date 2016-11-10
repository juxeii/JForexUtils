package com.jforex.programming.order.task.params.position;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.function.Function;

import com.dukascopy.api.Instrument;
import com.jforex.programming.order.task.params.basic.BasicParamsBase;
import com.jforex.programming.order.task.params.basic.BasicParamsBuilder;

public class CloseAllPositionsParams extends BasicParamsBase {

    private final Function<Instrument, ClosePositionParams> paramsFactory;

    private CloseAllPositionsParams(final Builder builder) {
        super(builder);

        paramsFactory = builder.paramsFactory;
    }

    public ClosePositionParams closePositionParams(final Instrument instrument) {
        return paramsFactory.apply(instrument);
    }

    public static Builder withClosePositionParams(final Function<Instrument, ClosePositionParams> paramsFactory) {
        checkNotNull(paramsFactory);

        return new Builder(paramsFactory);
    }

    public static class Builder extends BasicParamsBuilder<Builder> {

        private final Function<Instrument, ClosePositionParams> paramsFactory;

        public Builder(final Function<Instrument, ClosePositionParams> paramsFactory) {
            this.paramsFactory = paramsFactory;
        }

        public CloseAllPositionsParams build() {
            return new CloseAllPositionsParams(this);
        }
    }
}
