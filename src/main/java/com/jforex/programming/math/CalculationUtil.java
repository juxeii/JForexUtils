package com.jforex.programming.math;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.jforex.programming.math.MathUtil.isValueDivisibleByX;
import static com.jforex.programming.math.MathUtil.roundAmount;
import static com.jforex.programming.math.MathUtil.roundPips;
import static com.jforex.programming.math.MathUtil.roundPrice;

import org.aeonbits.owner.ConfigFactory;

import com.dukascopy.api.ICurrency;
import com.dukascopy.api.Instrument;
import com.dukascopy.api.OfferSide;
import com.jforex.programming.instrument.InstrumentFactory;
import com.jforex.programming.math.ConversionBuilder.FromSource;
import com.jforex.programming.math.PipDistanceBuilder.To;
import com.jforex.programming.math.PipValueBuilder.OfInstrument;
import com.jforex.programming.quote.TickQuoteHandler;
import com.jforex.programming.settings.PlatformSettings;

public final class CalculationUtil {

    private final TickQuoteHandler tickQuoteProvider;

    private static final PlatformSettings platformSettings = ConfigFactory.create(PlatformSettings.class);

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
        final Instrument conversionInstrument = InstrumentFactory
                .maybeFromCurrencies(sourceCurrency, targetCurrency)
                .get();
        final double conversionQuote =
                tickQuoteProvider.forOfferSide(conversionInstrument, offerSide);
        return targetCurrency.equals(conversionInstrument.getPrimaryJFCurrency())
                ? 1 / conversionQuote
                : conversionQuote;
    }

    public OfInstrument pipValueInCurrency(final ICurrency currency) {
        checkNotNull(currency);

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

    public static final double scalePipsToInstrument(final double pips,
                                                     final Instrument instrument) {
        return roundPrice(checkNotNull(instrument).getPipValue() * pips, instrument);
    }

    public static final double addPips(final Instrument instrument,
                                       final double price,
                                       final double pipsToAdd) {
        final double scaledPips = scalePipsToInstrument(pipsToAdd, checkNotNull(instrument));
        return roundPrice(price + scaledPips, instrument);
    }

    public static final To pipDistanceFrom(final double priceFrom) {
        return new PipDistanceBuilder(CalculationUtil::pipDistance).pipDistanceFrom(priceFrom);
    }

    private static final double pipDistance(final PipDistanceBuilder pipDistanceBuilder) {
        final double pipDistance = (pipDistanceBuilder.priceFrom() - pipDistanceBuilder.priceTo())
                / pipDistanceBuilder.instrument().getPipValue();
        return roundPips(pipDistance);
    }

    public static final boolean isPricePipDivisible(final Instrument instrument,
                                                    final double price) {
        return isValueDivisibleByX(price, checkNotNull(instrument).getPipValue() / 10);
    }

    public static final double scaleToPlatformAmount(final double amount) {
        return roundAmount(amount / platformSettings.baseAmount());
    }
}
