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

    public final double convertAmountForInstrument(final double sourceAmount,
                                                   final Instrument sourceInstrument,
                                                   final Instrument targetInstrument,
                                                   final OfferSide offerSide) {
        return convertAmount(sourceAmount,
                             sourceInstrument.getPrimaryJFCurrency(),
                             targetInstrument.getPrimaryJFCurrency(),
                             offerSide);
    }

    public final double convertAmount(final double sourceAmount,
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
        final Instrument conversionInstrument = InstrumentBuilder.fromCurrencies(sourceCurrency, targetCurrency).get();
        final double conversionQuote = tickQuoteProvider.forOfferSide(conversionInstrument, offerSide);
        return targetCurrency.equals(conversionInstrument.getPrimaryJFCurrency())
                ? 1 / conversionQuote
                : conversionQuote;
    }

    public final double pipValueInCurrency(final Instrument instrument,
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
