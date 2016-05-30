package com.jforex.programming.misc;

import static com.jforex.programming.misc.MathUtil.isValueDivisibleByX;
import static com.jforex.programming.misc.MathUtil.roundAmount;
import static com.jforex.programming.misc.MathUtil.roundPips;
import static com.jforex.programming.misc.MathUtil.roundPrice;

import org.aeonbits.owner.ConfigFactory;

import com.dukascopy.api.ICurrency;
import com.dukascopy.api.Instrument;
import com.dukascopy.api.OfferSide;
import com.jforex.programming.instrument.InstrumentBuilder;
import com.jforex.programming.quote.TickQuoteProvider;
import com.jforex.programming.settings.PlatformSettings;

public final class CalculationUtil {

    private final TickQuoteProvider tickQuoteProvider;

    private final static PlatformSettings platformSettings = ConfigFactory.create(PlatformSettings.class);

    public CalculationUtil(final TickQuoteProvider tickQuoteProvider) {
        this.tickQuoteProvider = tickQuoteProvider;
    }

    public FromSource convertAmount(final double amount) {
        return new ConversionBuilder(amount);
    }

    public interface FromSource {
        public ToInstrument fromInstrument(Instrument instrument);

        public ToCurrency fromCurrency(ICurrency currency);
    }

    public interface ToInstrument {
        public ForOfferSide toInstrument(Instrument instrument);
    }

    public interface ForOfferSide {
        public double forOfferSide(OfferSide offerSide);
    }

    public interface ToCurrency {
        public ForOfferSide toCurrency(ICurrency currency);
    }

    private class ConversionBuilder implements FromSource,
            ToInstrument,
            ToCurrency,
            ForOfferSide {

        private final double sourceAmount;
        private ICurrency sourceCurrency;
        private ICurrency targetCurrency;

        private ConversionBuilder(final double sourceAmount) {
            this.sourceAmount = sourceAmount;
        }

        @Override
        public ToInstrument fromInstrument(final Instrument sourceInstrument) {
            this.sourceCurrency = sourceInstrument.getPrimaryJFCurrency();
            return this;
        }

        @Override
        public ForOfferSide toInstrument(final Instrument targetInstrument) {
            this.targetCurrency = targetInstrument.getPrimaryJFCurrency();
            return this;
        }

        @Override
        public double forOfferSide(final OfferSide offerSide) {
            return convertAmount(sourceAmount,
                                 sourceCurrency,
                                 targetCurrency,
                                 offerSide);
        }

        @Override
        public ForOfferSide toCurrency(final ICurrency currency) {
            this.targetCurrency = currency;
            return this;
        }

        @Override
        public ToCurrency fromCurrency(final ICurrency currency) {
            this.sourceCurrency = currency;
            return this;
        }
    }

    private final double convertAmount(final double sourceAmount,
                                       final ICurrency sourceCurrency,
                                       final ICurrency targetCurrency,
                                       final OfferSide offerSide) {
        return sourceCurrency.equals(targetCurrency)
                ? sourceAmount
                : roundAmount(sourceAmount * conversionQuote(sourceCurrency, targetCurrency, offerSide));
    }

    private final double conversionQuote(final ICurrency sourceCurrency,
                                         final ICurrency targetCurrency,
                                         final OfferSide offerSide) {
        final Instrument conversionInstrument =
                InstrumentBuilder.fromCurrencies(sourceCurrency, targetCurrency).get();
        final double conversionQuote = tickQuoteProvider.forOfferSide(conversionInstrument, offerSide);
        return targetCurrency.equals(conversionInstrument.getPrimaryJFCurrency())
                ? 1 / conversionQuote
                : conversionQuote;
    }

    public OfInstrument pipValueInCurrency(final ICurrency currency) {
        return new PipValueBuilder(currency);
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

    private class PipValueBuilder implements OfInstrument,
            WithAmount,
            AndOfferSide {

        private final ICurrency currency;
        private Instrument instrument;
        private double amount;

        private PipValueBuilder(final ICurrency currency) {
            this.currency = currency;
        }

        @Override
        public WithAmount ofInstrument(final Instrument instrument) {
            this.instrument = instrument;
            return this;
        }

        @Override
        public AndOfferSide withAmount(final double amount) {
            this.amount = amount;
            return this;
        }

        @Override
        public double andOfferSide(final OfferSide offerSide) {
            return pipValueInCurrency(instrument,
                                      amount,
                                      currency,
                                      offerSide);
        }
    }

    private final double pipValueInCurrency(final Instrument instrument,
                                            final double amount,
                                            final ICurrency targetCurrency,
                                            final OfferSide offerSide) {
        double pipValueAmount = amount * instrument.getPipValue();
        if (!targetCurrency.equals(instrument.getSecondaryJFCurrency()))
            pipValueAmount = convertAmount(pipValueAmount,
                                           instrument.getSecondaryJFCurrency(),
                                           targetCurrency,
                                           offerSide);
        return roundAmount(pipValueAmount);
    }

    public final static double scalePipsToInstrument(final double pips,
                                                     final Instrument instrument) {
        return roundPrice(instrument.getPipValue() * pips, instrument);
    }

    public final static double addPips(final Instrument instrument,
                                       final double price,
                                       final double pipsToAdd) {
        return roundPrice(price + scalePipsToInstrument(pipsToAdd, instrument), instrument);
    }

    public final static double pipDistance(final Instrument instrument,
                                           final double priceA,
                                           final double priceB) {
        final double pipDistance = (priceA - priceB) / instrument.getPipValue();
        return roundPips(pipDistance);
    }

    public final static boolean isPricePipDivisible(final Instrument instrument,
                                                    final double price) {
        return isValueDivisibleByX(price, instrument.getPipValue() / 10);
    }

    public final static double scaleToPlatformAmount(final double amount) {
        return roundAmount(amount / platformSettings.baseAmount());
    }
}
