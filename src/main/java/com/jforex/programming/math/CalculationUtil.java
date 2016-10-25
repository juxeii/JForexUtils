package com.jforex.programming.math;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.jforex.programming.math.MathUtil.roundAmount;

import com.dukascopy.api.ICurrency;
import com.dukascopy.api.IOrder;
import com.dukascopy.api.Instrument;
import com.dukascopy.api.OfferSide;
import com.jforex.programming.instrument.InstrumentFactory;
import com.jforex.programming.instrument.InstrumentUtil;
import com.jforex.programming.quote.TickQuoteProvider;

public class CalculationUtil {

    private final TickQuoteProvider tickQuoteProvider;

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

    public double slPriceForPips(final IOrder order,
                                 final double pips) {
        checkNotNull(order);

        return addPipsToPriceForSL(order, pips);
    }

    public double tpPriceForPips(final IOrder order,
                                 final double pips) {
        checkNotNull(order);

        return addPipsToPriceForSL(order, -pips);
    }

    private final double addPipsToPriceForSL(final IOrder order,
                                             final double pips) {
        return InstrumentUtil.addPipsToPrice(order.getInstrument(),
                                             currentQuoteForSLTP(order),
                                             order.isLong() ? -pips : pips);
    }

    private final double currentQuoteForSLTP(final IOrder order) {
        final Instrument instrument = order.getInstrument();
        return order.isLong()
                ? tickQuoteProvider.bid(instrument)
                : tickQuoteProvider.ask(instrument);
    }
}
