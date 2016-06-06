package com.jforex.programming.builder;

import java.util.function.Function;

import com.dukascopy.api.Instrument;

public class PipDistanceBuilder {

    private Builder builder;
    protected final Function<PipDistanceBuilder, Double> consumer;

    public PipDistanceBuilder(final Function<PipDistanceBuilder, Double> consumer) {
        this.consumer = consumer;
    }

    public interface To {
        public ForInstrument to(double toPrice);
    }

    public interface ForInstrument {
        public double forInstrument(Instrument instrument);
    }

    public To pipDistanceFrom(final double fromPrice) {
        builder = new Builder(fromPrice);
        return builder;
    }

    public double priceFrom() {
        return this.builder.fromPrice;
    }

    public double priceTo() {
        return this.builder.toPrice;
    }

    public Instrument instrument() {
        return this.builder.instrument;
    }

    private class Builder implements To,
            ForInstrument {

        private final double fromPrice;
        private double toPrice;
        private Instrument instrument;

        private Builder(final double fromPrice) {
            this.fromPrice = fromPrice;
        }

        @Override
        public ForInstrument to(final double toPrice) {
            this.toPrice = toPrice;
            return this;
        }

        @Override
        public double forInstrument(final Instrument instrument) {
            this.instrument = instrument;
            return consumer.apply(PipDistanceBuilder.this);
        }
    }
}
