package com.jforex.programming.instrument;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toSet;

import java.util.Collection;
import java.util.Currency;
import java.util.Set;
import java.util.stream.Stream;

import com.dukascopy.api.IBar;
import com.dukascopy.api.ICurrency;
import com.dukascopy.api.ITick;
import com.dukascopy.api.Instrument;
import com.jforex.programming.currency.CurrencyUtil;
import com.jforex.programming.math.CalculationUtil;
import com.jforex.programming.quote.BarParams;
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

    private static final String pairsSeparator = Instrument.getPairsSeparator();

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

    public final IBar bar(final BarParams barParams) {
        return barQuoteProvider.bar(checkNotNull(barParams));
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
        return CurrencyUtil.isInInstrument(checkNotNull(currency), instrument);
    }

    public final boolean containsCurrencyCode(final String currencyCode) {
        return CurrencyUtil.isInInstrument(checkNotNull(currencyCode), instrument);
    }

    public static final int numberOfDigits(final Instrument instrument) {
        return instrument.getPipScale() + 1;
    }

    public static final String toStringNoSeparator(final Instrument instrument) {
        return checkNotNull(instrument)
                .getPrimaryJFCurrency()
                .toString()
                .concat(instrument.getSecondaryJFCurrency().toString());
    }

    public static final Currency baseJavaCurrency(final Instrument instrument) {
        return checkNotNull(instrument)
                .getPrimaryJFCurrency()
                .getJavaCurrency();
    }

    public static final Currency quoteJavaCurrency(final Instrument instrument) {
        return checkNotNull(instrument)
                .getSecondaryJFCurrency()
                .getJavaCurrency();
    }

    public static final String nameFromCurrencies(final ICurrency baseCurrency,
                                                  final ICurrency quoteCurrency) {
        checkNotNull(baseCurrency);
        checkNotNull(quoteCurrency);

        return baseCurrency
                .toString()
                .concat(pairsSeparator)
                .concat(quoteCurrency.toString());
    }

    public static final Set<ICurrency> currencies(final Instrument instrument) {
        checkNotNull(instrument);

        return Stream.of(instrument.getPrimaryJFCurrency(),
                         instrument.getSecondaryJFCurrency())
                .collect(toSet());
    }

    public static final Set<ICurrency> currenciesFromCollection(final Collection<Instrument> instruments) {
        return checkNotNull(instruments)
                .stream()
                .map(InstrumentUtil::currencies)
                .flatMap(Set::stream)
                .collect(toSet());
    }

    public static final Set<ICurrency> currenciesFromCollection(final Instrument... instruments) {
        return InstrumentUtil.currenciesFromCollection(asList(checkNotNull(instruments)));
    }
}
