package com.jforex.programming.math;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.jforex.programming.math.MathUtil.roundAmount;

import com.dukascopy.api.ICurrency;
import com.dukascopy.api.Instrument;
import com.dukascopy.api.OfferSide;
import com.jforex.programming.init.JForexUtil;
import com.jforex.programming.instrument.InstrumentFactory;
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

    public static final double scaleToPlatformAmount(final double amount) {
        return roundAmount(amount / platformSettings.baseAmount());
    }
}
