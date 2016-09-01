package com.jforex.programming.order.process;

import static com.google.common.base.Preconditions.checkNotNull;

import com.dukascopy.api.Instrument;
import com.jforex.programming.order.process.option.CloseOption;

public class ClosePositionProcess extends CommonProcess {

    private final Instrument instrument;

    private ClosePositionProcess(final Builder builder) {
        super(builder);
        instrument = builder.instrument;
    }

    public final Instrument instrument() {
        return instrument;
    }

    public static final CloseOption forInstrument(final Instrument instrument) {
        return new Builder(checkNotNull(instrument));
    }

    private static class Builder extends CommonBuilder
                                 implements CloseOption {

        private final Instrument instrument;

        private Builder(final Instrument instrument) {
            this.instrument = instrument;
        }

        @SuppressWarnings("unchecked")
        @Override
        public ClosePositionProcess build() {
            return new ClosePositionProcess(this);
        }
    }
}
