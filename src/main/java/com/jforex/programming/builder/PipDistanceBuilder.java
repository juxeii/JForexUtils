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
        public ForInstrument to(double amount);
    }

    public interface ForInstrument {
        public double forInstrument(Instrument instrument);
    }

    public To pipDistanceFrom(final double fromPrice) {
        builder = new Builder(fromPrice);
        return builder;
    }

    public double priceFrom() {
        return this.builder.priceFrom;
    }

    public double priceTo() {
        return this.builder.priceTo;
    }

    public Instrument instrument() {
        return this.builder.instrument;
    }

    private class Builder implements To,
            ForInstrument {

        private final double priceFrom;
        private double priceTo;
        private Instrument instrument;

        private Builder(final double priceFrom) {
            this.priceFrom = priceFrom;
        }

        @Override
        public ForInstrument to(final double priceTo) {
            this.priceTo = priceTo;
            return this;
        }

        @Override
        public double forInstrument(final Instrument instrument) {
            this.instrument = instrument;
            return consumer.apply(PipDistanceBuilder.this);
        }
    }
}
