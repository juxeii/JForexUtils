package com.jforex.programming.instrument;

import java.util.Currency;

import com.dukascopy.api.IBar;
import com.dukascopy.api.ICurrency;
import com.dukascopy.api.ITick;
import com.dukascopy.api.Instrument;
import com.dukascopy.api.Period;
import com.jforex.programming.currency.CurrencyUtil;
import com.jforex.programming.misc.CalculationUtil;
import com.jforex.programming.quote.BarQuoteProvider;
import com.jforex.programming.quote.TickQuoteProvider;

public final class InstrumentUtil {

    private final Instrument instrument;
    private final TickQuoteProvider tickQuoteProvider;
    private final BarQuoteProvider barQuoteProvider;
    private final Currency baseJavaCurrency;
    private final Currency quoteJavaCurrency;
    private final int numberOfDigits;
    private final String toStringNoSeparator;

    private final static String pairsSeparator = Instrument.getPairsSeparator();

    public InstrumentUtil(final Instrument instrument,
                          final TickQuoteProvider tickQuoteProvider,
                          final BarQuoteProvider barQuoteProvider) {
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

    public final double spread() {
        return CalculationUtil.pipDistance(instrument, ask(), bid());
    }

    public final IBar askBar(final Period period) {
        return barQuoteProvider.askBar(instrument, period);
    }

    public final IBar bidBar(final Period period) {
        return barQuoteProvider.bidBar(instrument, period);
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

    public final boolean containsCurrency(final String currencyName) {
        return CurrencyUtil.isInInstrument(currencyName, instrument);
    }

    public final static int numberOfDigits(final Instrument instrument) {
        return instrument.getPipScale() + 1;
    }

    public final static String toStringNoSeparator(final Instrument instrument) {
        return instrument.getPrimaryJFCurrency().toString()
                         .concat(instrument.getSecondaryJFCurrency().toString());
    }

    public final static Currency baseJavaCurrency(final Instrument instrument) {
        return instrument.getPrimaryJFCurrency().getJavaCurrency();
    }

    public final static Currency quoteJavaCurrency(final Instrument instrument) {
        return instrument.getSecondaryJFCurrency().getJavaCurrency();
    }

    public final static String nameFromCurrencies(final ICurrency baseCurrency,
                                                  final ICurrency quoteCurrency) {
        return baseCurrency.toString().concat(pairsSeparator).concat(quoteCurrency.toString());
    }
}
