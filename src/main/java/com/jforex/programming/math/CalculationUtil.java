package com.jforex.programming.math;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.jforex.programming.math.MathUtil.isValueDivisibleByX;
import static com.jforex.programming.math.MathUtil.roundAmount;
import static com.jforex.programming.math.MathUtil.roundPips;
import static com.jforex.programming.math.MathUtil.roundPrice;

import com.dukascopy.api.ICurrency;
import com.dukascopy.api.Instrument;
import com.dukascopy.api.OfferSide;
import com.jforex.programming.instrument.InstrumentFactory;
import com.jforex.programming.misc.JForexUtil;
import com.jforex.programming.quote.TickQuoteProvider;
import com.jforex.programming.settings.PlatformSettings;

public class CalculationUtil {

    private final TickQuoteProvider tickQuoteProvider;

    private static final PlatformSettings platformSettings = JForexUtil.platformSettings;

    public CalculationUtil(final TickQuoteProvider tickQuoteProvider) {
        this.tickQuoteProvider = tickQuoteProvider;
    }

    public double convertAmount(final double amount,
                                final ICurrency sourceCurrency,
                                final ICurrency targetCurrency,
                                final OfferSide offerSide) {
        checkNotNull(sourceCurrency);
        checkNotNull(targetCurrency);
        checkNotNull(offerSide);

        return sourceCurrency.equals(targetCurrency)
                ? amount
                : roundAmount(amount * conversionQuote(sourceCurrency,
                                                       targetCurrency,
                                                       offerSide));
    }

    private final double conversionQuote(final ICurrency sourceCurrency,
                                         final ICurrency targetCurrency,
                                         final OfferSide offerSide) {
        final Instrument conversionInstrument = InstrumentFactory
            .maybeFromCurrencies(sourceCurrency, targetCurrency)
            .get();
        final double conversionQuote = tickQuoteProvider.forOfferSide(conversionInstrument, offerSide);
        return targetCurrency.equals(conversionInstrument.getPrimaryJFCurrency())
                ? 1 / conversionQuote
                : conversionQuote;
    }

    public double pipValueInCurrency(final double amount,
                                     final Instrument instrument,
                                     final ICurrency targetCurrency,
                                     final OfferSide offerSide) {
        checkNotNull(instrument);
        checkNotNull(targetCurrency);
        checkNotNull(targetCurrency);
        checkNotNull(offerSide);

        double pipValueAmount = amount * instrument.getPipValue();
        if (!targetCurrency.equals(instrument.getSecondaryJFCurrency()))
            pipValueAmount = convertAmount(pipValueAmount,
                                           instrument.getSecondaryJFCurrency(),
                                           targetCurrency,
                                           offerSide);

        return roundAmount(pipValueAmount);
    }

    public static final double scalePipsToInstrument(final double pips,
                                                     final Instrument instrument) {
        checkNotNull(instrument);

        return roundPrice(instrument.getPipValue() * pips, instrument);
    }

    public static final double addPipsToPrice(final Instrument instrument,
                                              final double price,
                                              final double pipsToAdd) {
        checkNotNull(instrument);

        final double scaledPips = scalePipsToInstrument(pipsToAdd, instrument);
        return roundPrice(price + scaledPips, instrument);
    }

    public static final double pipDistance(final Instrument instrument,
                                           final double priceA,
                                           final double priceB) {
        checkNotNull(instrument);

        final double pipDistance = (priceA - priceB) / instrument.getPipValue();
        return roundPips(pipDistance);
    }

    public static final boolean isPricePipDivisible(final Instrument instrument,
                                                    final double price) {
        checkNotNull(instrument);

        return isValueDivisibleByX(price, instrument.getPipValue() / 10);
    }

    public static final double scaleToPlatformAmount(final double amount) {
        return roundAmount(amount / platformSettings.baseAmount());
    }
}
