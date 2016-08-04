package com.jforex.programming.math;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.function.Function;

import com.dukascopy.api.ICurrency;
import com.dukascopy.api.Instrument;
import com.dukascopy.api.OfferSide;

public class PipValueBuilder {

    private Builder builder;
    protected final Function<PipValueBuilder, Double> consumer;

    public interface OfInstrument {
        public WithAmount ofInstrument(Instrument instrument);
    }

    public interface WithAmount {
        public AndOfferSide withAmount(double amount);
    }

    public interface AndOfferSide {
        public double andOfferSide(OfferSide offerSide);
    }

    public PipValueBuilder(final Function<PipValueBuilder, Double> consumer) {
        this.consumer = consumer;
    }

    public final OfInstrument pipValueInCurrency(final ICurrency currency) {
        builder = new Builder(checkNotNull(currency));
        return builder;
    }

    public final double amount() {
        return this.builder.amount;
    }

    public final ICurrency targetCurrency() {
        return this.builder.targetCurrency;
    }

    public final Instrument instrument() {
        return this.builder.instrument;
    }

    public final OfferSide offerSide() {
        return this.builder.offerSide;
    }

    private final class Builder implements
                                OfInstrument,
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
        public final AndOfferSide withAmount(final double amount) {
            this.amount = amount;
            return this;
        }

        @Override
        public final WithAmount ofInstrument(final Instrument instrument) {
            this.instrument = checkNotNull(instrument);
            return this;
        }

        @Override
        public final double andOfferSide(final OfferSide offerSide) {
            this.offerSide = checkNotNull(offerSide);
            return consumer.apply(PipValueBuilder.this);
        }
    }
}
