package com.jforex.programming.order.task.params.position;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.function.Function;

import com.dukascopy.api.Instrument;
import com.jforex.programming.order.task.params.basic.BasicParamsBase;
import com.jforex.programming.order.task.params.basic.BasicParamsBuilder;

public class MergeAllPositionsParams extends BasicParamsBase {

    private final Function<Instrument, MergePositionParams> paramsFactory;

    private MergeAllPositionsParams(final Builder builder) {
        super(builder);

        paramsFactory = builder.paramsFactory;
    }

    public MergePositionParams mergePositionParams(final Instrument instrument) {
        return paramsFactory.apply(instrument);
    }

    public static Builder withMergePositionParams(final Function<Instrument, MergePositionParams> paramsFactory) {
        checkNotNull(paramsFactory);

        return new Builder(paramsFactory);
    }

    public static class Builder extends BasicParamsBuilder<Builder> {

        private final Function<Instrument, MergePositionParams> paramsFactory;

        public Builder(final Function<Instrument, MergePositionParams> paramsFactory) {
            this.paramsFactory = paramsFactory;
        }

        public MergeAllPositionsParams build() {
            return new MergeAllPositionsParams(this);
        }
    }
}
