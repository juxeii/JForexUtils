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

    public final double convertAmount(final double amount,
                                      final ICurrency sourceCurrency,
                                      final ICurrency targetCurrency,
                                      final OfferSide offerSide) {
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
        double pipValueAmount = amount * instrument.getPipValue();
        if (!targetCurrency.equals(instrument.getSecondaryJFCurrency()))
            pipValueAmount = convertAmount(pipValueAmount,
                                           instrument.getSecondaryJFCurrency(),
                                           targetCurrency,
                                           offerSide);

        return roundAmount(pipValueAmount);
    }

    public static double scalePipsToInstrument(final double pips,
                                               final Instrument instrument) {
        return roundPrice(checkNotNull(instrument).getPipValue() * pips, instrument);
    }

    public static double addPips(final Instrument instrument,
                                 final double price,
                                 final double pipsToAdd) {
        final double scaledPips = scalePipsToInstrument(pipsToAdd, checkNotNull(instrument));
        return roundPrice(price + scaledPips, instrument);
    }

    public double pipDistance(final Instrument instrument,
                              final double priceA,
                              final double priceB) {
        final double pipDistance = (priceA - priceB) / instrument.getPipValue();
        return roundPips(pipDistance);
    }

    public boolean isPricePipDivisible(final Instrument instrument,
                                       final double price) {
        return isValueDivisibleByX(price, checkNotNull(instrument).getPipValue() / 10);
    }

    public static double scaleToPlatformAmount(final double amount) {
        return roundAmount(amount / platformSettings.baseAmount());
    }
}
