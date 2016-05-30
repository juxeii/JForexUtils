package com.jforex.programming.builder;

import java.util.function.Function;

import com.dukascopy.api.ICurrency;
import com.dukascopy.api.Instrument;
import com.dukascopy.api.OfferSide;

public class PipValueBuilder {

    private Builder builder;
    protected final Function<PipValueBuilder, Double> consumer;

    public PipValueBuilder(final Function<PipValueBuilder, Double> consumer) {
        this.consumer = consumer;
    }

    public interface OfInstrument {
        public WithAmount ofInstrument(Instrument instrument);
    }

    public interface WithAmount {
        public AndOfferSide withAmount(double amount);
    }

    public interface AndOfferSide {
        public double andOfferSide(OfferSide offerSide);
    }

    public OfInstrument pipValueInCurrency(final ICurrency currency) {
        builder = new Builder(currency);
        return builder;
    }

    public double amount() {
        return this.builder.amount;
    }

    public ICurrency targetCurrency() {
        return this.builder.targetCurrency;
    }

    public Instrument instrument() {
        return this.builder.instrument;
    }

    public OfferSide offerSide() {
        return this.builder.offerSide;
    }

    private class Builder implements OfInstrument,
                          WithAmount,
                          AndOfferSide {

        private final ICurrency targetCurrency;
        private double amount;
        private Instrument instrument;
        private OfferSide offerSide;

        private Builder(final ICurrency targetCurrency) {
            this.targetCurrency = targetCurrency;
        }

        @Override
        public AndOfferSide withAmount(final double amount) {
            this.amount = amount;
            return this;
        }

        @Override
        public WithAmount ofInstrument(final Instrument instrument) {
            this.instrument = instrument;
            return this;
        }

        @Override
        public double andOfferSide(final OfferSide offerSide) {
            this.offerSide = offerSide;
            return consumer.apply(PipValueBuilder.this);
        }
    }
}
