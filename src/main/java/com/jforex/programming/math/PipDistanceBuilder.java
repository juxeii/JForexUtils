package com.jforex.programming.math;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.function.Function;

import com.dukascopy.api.Instrument;

public class PipDistanceBuilder {

    private Builder builder;
    protected final Function<PipDistanceBuilder, Double> consumer;

    public interface To {
        public ForInstrument to(double toPrice);
    }

    public interface ForInstrument {
        public double forInstrument(Instrument instrument);
    }

    public PipDistanceBuilder(final Function<PipDistanceBuilder, Double> consumer) {
        this.consumer = consumer;
    }

    public final To pipDistanceFrom(final double fromPrice) {
        builder = new Builder(fromPrice);
        return builder;
    }

    public final double priceFrom() {
        return this.builder.fromPrice;
    }

    public final double priceTo() {
        return this.builder.toPrice;
    }

    public final Instrument instrument() {
        return this.builder.instrument;
    }

    private final class Builder implements
                                To,
                                ForInstrument {

        private final double fromPrice;
        private double toPrice;
        private Instrument instrument;

        private Builder(final double fromPrice) {
            this.fromPrice = fromPrice;
        }

        @Override
        public final ForInstrument to(final double toPrice) {
            this.toPrice = toPrice;
            return this;
        }

        @Override
        public final double forInstrument(final Instrument instrument) {
            this.instrument = checkNotNull(instrument);
            return consumer.apply(PipDistanceBuilder.this);
        }
    }
}
