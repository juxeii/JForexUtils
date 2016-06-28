package com.jforex.programming.math;

import static com.jforex.programming.math.MathUtil.isValueDivisibleByX;
import static com.jforex.programming.math.MathUtil.roundAmount;
import static com.jforex.programming.math.MathUtil.roundPips;
import static com.jforex.programming.math.MathUtil.roundPrice;

import org.aeonbits.owner.ConfigFactory;

import com.dukascopy.api.ICurrency;
import com.dukascopy.api.Instrument;
import com.dukascopy.api.OfferSide;
import com.jforex.programming.instrument.InstrumentBuilder;
import com.jforex.programming.math.ConversionBuilder.FromSource;
import com.jforex.programming.math.PipDistanceBuilder.To;
import com.jforex.programming.math.PipValueBuilder.OfInstrument;
import com.jforex.programming.quote.TickQuoteHandler;
import com.jforex.programming.settings.PlatformSettings;

public final class CalculationUtil {

    private final TickQuoteHandler tickQuoteProvider;

    private final static PlatformSettings platformSettings = ConfigFactory.create(PlatformSettings.class);

    public CalculationUtil(final TickQuoteHandler tickQuoteProvider) {
        this.tickQuoteProvider = tickQuoteProvider;
    }

    public FromSource convertAmount(final double amount) {
        return new ConversionBuilder(this::convertAmount).convertAmount(amount);
    }

    private final double convertAmount(final ConversionBuilder conversionBuilder) {
        final double amount = conversionBuilder.amount();
        final ICurrency sourceCurrency = conversionBuilder.sourceCurrency();
        final ICurrency targetCurrency = conversionBuilder.targetCurrency();

        return sourceCurrency.equals(targetCurrency)
                ? amount
                : roundAmount(amount * conversionQuote(sourceCurrency,
                                                       targetCurrency,
                                                       conversionBuilder.offerSide()));
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
        return new PipValueBuilder(this::pipValueInCurrency).pipValueInCurrency(currency);
    }

    private final double pipValueInCurrency(final PipValueBuilder pipValueBuilder) {
        final double amount = pipValueBuilder.amount();
        final ICurrency targetCurrency = pipValueBuilder.targetCurrency();
        final Instrument instrument = pipValueBuilder.instrument();

        double pipValueAmount = amount * instrument.getPipValue();
        if (!targetCurrency.equals(instrument.getSecondaryJFCurrency()))
            pipValueAmount = convertAmount(pipValueAmount)
                    .fromCurrency(instrument.getSecondaryJFCurrency())
                    .toCurrency(targetCurrency)
                    .forOfferSide(pipValueBuilder.offerSide());

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

    public final static To pipDistanceFrom(final double priceFrom) {
        return new PipDistanceBuilder(CalculationUtil::pipDistance).pipDistanceFrom(priceFrom);
    }

    private final static double pipDistance(final PipDistanceBuilder pipDistanceBuilder) {
        final double pipDistance = (pipDistanceBuilder.priceFrom() - pipDistanceBuilder.priceTo())
                / pipDistanceBuilder.instrument().getPipValue();
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
