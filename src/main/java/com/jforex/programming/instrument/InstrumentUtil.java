package com.jforex.programming.instrument;

import java.util.Currency;

import com.dukascopy.api.IBar;
import com.dukascopy.api.ICurrency;
import com.dukascopy.api.ITick;
import com.dukascopy.api.Instrument;
import com.jforex.programming.builder.BarQuoteFilter;
import com.jforex.programming.currency.CurrencyUtil;
import com.jforex.programming.misc.CalculationUtil;
import com.jforex.programming.quote.BarQuoteHandler;
import com.jforex.programming.quote.TickQuoteHandler;

public final class InstrumentUtil {

    private final Instrument instrument;
    private final TickQuoteHandler tickQuoteProvider;
    private final BarQuoteHandler barQuoteProvider;
    private final Currency baseJavaCurrency;
    private final Currency quoteJavaCurrency;
    private final int numberOfDigits;
    private final String toStringNoSeparator;

    private final static String pairsSeparator = Instrument.getPairsSeparator();

    public InstrumentUtil(final Instrument instrument,
                          final TickQuoteHandler tickQuoteProvider,
                          final BarQuoteHandler barQuoteProvider) {
        this.instrument = instrument;
        this.tickQuoteProvider = tickQuoteProvider;
        this.barQuoteProvider = barQuoteProvider;

        baseJavaCurrency = baseJavaCurrency(instrument);
        quoteJavaCurrency = quoteJavaCurrency(instrument);
        numberOfDigits = numberOfDigits(instrument);
        toStringNoSeparator = toStringNoSeparator(instrument);
    }

    public final ITick tick() {
        return tickQuoteProvider.tick(instrument);
    }

    public final double ask() {
        return tickQuoteProvider.ask(instrument);
    }

    public final double bid() {
        return tickQuoteProvider.bid(instrument);
    }

    public final IBar barQuote(final BarQuoteFilter barQuoteFilter) {
        return barQuoteProvider.quote(barQuoteFilter);
    }

    public final double spread() {
        return CalculationUtil
                .pipDistanceFrom(ask())
                .to(bid())
                .forInstrument(instrument);
    }

    public final Currency baseJavaCurrency() {
        return baseJavaCurrency;
    }

    public final Currency quoteJavaCurrency() {
        return quoteJavaCurrency;
    }

    public final int numberOfDigits() {
        return numberOfDigits;
    }

    public final String toStringNoSeparator() {
        return toStringNoSeparator;
    }

    public final boolean containsCurrency(final ICurrency currency) {
        return CurrencyUtil.isInInstrument(currency, instrument);
    }

    public final boolean containsCurrencyCode(final String currencyCode) {
        return CurrencyUtil.isInInstrument(currencyCode, instrument);
    }

    public final static int numberOfDigits(final Instrument instrument) {
        return instrument.getPipScale() + 1;
    }

    public final static String toStringNoSeparator(final Instrument instrument) {
        return instrument
                .getPrimaryJFCurrency()
                .toString()
                .concat(instrument.getSecondaryJFCurrency().toString());
    }

    public final static Currency baseJavaCurrency(final Instrument instrument) {
        return instrument
                .getPrimaryJFCurrency()
                .getJavaCurrency();
    }

    public final static Currency quoteJavaCurrency(final Instrument instrument) {
        return instrument
                .getSecondaryJFCurrency()
                .getJavaCurrency();
    }

    public final static String nameFromCurrencies(final ICurrency baseCurrency,
                                                  final ICurrency quoteCurrency) {
        return baseCurrency
                .toString()
                .concat(pairsSeparator)
                .concat(quoteCurrency.toString());
    }
}
