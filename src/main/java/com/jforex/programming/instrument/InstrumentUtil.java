package com.jforex.programming.instrument;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.jforex.programming.math.MathUtil.isValueDivisibleByX;
import static com.jforex.programming.math.MathUtil.roundPips;
import static com.jforex.programming.math.MathUtil.roundPrice;

import java.util.Currency;
import java.util.Set;

import com.dukascopy.api.IBar;
import com.dukascopy.api.ICurrency;
import com.dukascopy.api.ITick;
import com.dukascopy.api.Instrument;
import com.dukascopy.api.OfferSide;
import com.jforex.programming.currency.CurrencyCode;
import com.jforex.programming.currency.CurrencyFactory;
import com.jforex.programming.currency.CurrencyUtil;
import com.jforex.programming.math.CalculationUtil;
import com.jforex.programming.math.MathUtil;
import com.jforex.programming.quote.BarParams;
import com.jforex.programming.quote.BarQuoteProvider;
import com.jforex.programming.quote.TickQuoteProvider;

public class InstrumentUtil {

    private final Instrument instrument;
    private final TickQuoteProvider tickQuoteProvider;
    private final BarQuoteProvider barQuoteProvider;
    private final CalculationUtil calculationUtil;
    private final ICurrency baseCurrency;
    private final ICurrency quoteCurrency;
    private final Currency baseJavaCurrency;
    private final Currency quoteJavaCurrency;
    private final int numberOfDigits;
    private final String toStringNoSeparator;
    private final String toString;
    private final Set<ICurrency> currencies;

    private static final String pairsSeparator = Instrument.getPairsSeparator();

    public InstrumentUtil(final Instrument instrument,
                          final TickQuoteProvider tickQuoteProvider,
                          final BarQuoteProvider barQuoteProvider,
                          final CalculationUtil calculationUtil) {
        this.instrument = instrument;
        this.tickQuoteProvider = tickQuoteProvider;
        this.barQuoteProvider = barQuoteProvider;
        this.calculationUtil = calculationUtil;

        baseCurrency = instrument.getPrimaryJFCurrency();
        quoteCurrency = instrument.getSecondaryJFCurrency();
        baseJavaCurrency = baseJavaCurrency(instrument);
        quoteJavaCurrency = quoteJavaCurrency(instrument);
        numberOfDigits = numberOfDigits(instrument);
        toStringNoSeparator = toStringNoSeparator(instrument);
        toString = nameFromCurrencies(baseCurrency, quoteCurrency);
        currencies = CurrencyFactory.fromInstrument(instrument);
    }

    public ITick tickQuote() {
        return tickQuoteProvider.tick(instrument);
    }

    public double askQuote() {
        return tickQuoteProvider.ask(instrument);
    }

    public double bidQuote() {
        return tickQuoteProvider.bid(instrument);
    }

    public IBar barQuote(final BarParams barParams) {
        checkNotNull(barParams);

        return barQuoteProvider.bar(barParams);
    }

    public double spreadInPips() {
        return pipDistanceOfPrices(askQuote(), bidQuote());
    }

    public double spread() {
        return roundPrice(askQuote() - bidQuote(), instrument);
    }

    public Currency baseJavaCurrency() {
        return baseJavaCurrency;
    }

    public Currency quoteJavaCurrency() {
        return quoteJavaCurrency;
    }

    public int numberOfDigits() {
        return numberOfDigits;
    }

    public String toStringNoSeparator() {
        return toStringNoSeparator;
    }

    @Override
    public String toString() {
        return toString;
    }

    public Set<ICurrency> currencies() {
        return currencies;
    }

    public double scalePipsToPrice(final double pips) {
        return scalePipsToPrice(instrument, pips);
    }

    public double addPipsToPrice(final double price,
                                 final double pipsToAdd) {
        return addPipsToPrice(instrument,
                              price,
                              pipsToAdd);
    }

    public double pipDistanceOfPrices(final double priceA,
                                      final double priceB) {
        return pipDistanceOfPrices(instrument,
                                   priceA,
                                   priceB);
    }

    public boolean isPricePipDivisible(final double price) {
        return isPricePipDivisible(instrument, price);
    }

    public double convertAmount(final double amount,
                                final Instrument targetInstrument,
                                final OfferSide offerSide) {
        checkNotNull(targetInstrument);
        checkNotNull(offerSide);

        return calculationUtil.convertAmount(amount,
                                             baseCurrency,
                                             targetInstrument.getPrimaryJFCurrency(),
                                             offerSide);
    }

    public double pipValueInCurrency(final double amount,
                                     final ICurrency targetCurrency,
                                     final OfferSide offerSide) {
        checkNotNull(targetCurrency);
        checkNotNull(offerSide);

        return calculationUtil.pipValueInCurrency(amount,
                                                  instrument,
                                                  targetCurrency,
                                                  offerSide);
    }

    public boolean containsCurrency(final ICurrency currency) {
        checkNotNull(currency);

        return CurrencyUtil.isInInstrument(currency, instrument);
    }

    public boolean containsCurrencyCode(final CurrencyCode currencyCode) {
        checkNotNull(currencyCode);

        return CurrencyUtil.isInInstrument(currencyCode.toString(), instrument);
    }

    public static final int numberOfDigits(final Instrument instrument) {
        checkNotNull(instrument);

        return instrument.getPipScale() + 1;
    }

    public static final double pipDistanceOfPrices(final Instrument instrument,
                                                   final double priceA,
                                                   final double priceB) {
        final double pipDistance = (priceA - priceB) / instrument.getPipValue();
        return roundPips(pipDistance);
    }

    public static final double addPipsToPrice(final Instrument instrument,
                                              final double price,
                                              final double pipsToAdd) {
        checkNotNull(instrument);

        final double scaledPips = scalePipsToPrice(instrument, pipsToAdd);
        return roundPrice(price + scaledPips, instrument);
    }

    public static final double scalePipsToPrice(final Instrument instrument,
                                                final double pips) {
        checkNotNull(instrument);

        return roundPrice(instrument.getPipValue() * pips, instrument);
    }

    public static final double scalePriceToPips(final Instrument instrument,
                                                final double price) {
        checkNotNull(instrument);

        final double pips = price * (1 / instrument.getPipValue());
        return MathUtil.roundPips(pips);
    }

    public static final boolean isPricePipDivisible(final Instrument instrument,
                                                    final double price) {
        checkNotNull(instrument);

        return isValueDivisibleByX(price, instrument.getPipValue() / 10);
    }

    public static final String toStringNoSeparator(final Instrument instrument) {
        checkNotNull(instrument);

        return instrument
            .getPrimaryJFCurrency()
            .toString()
            .concat(instrument.getSecondaryJFCurrency().toString());
    }

    public static final Currency baseJavaCurrency(final Instrument instrument) {
        checkNotNull(instrument);

        return instrument
            .getPrimaryJFCurrency()
            .getJavaCurrency();
    }

    public static final Currency quoteJavaCurrency(final Instrument instrument) {
        checkNotNull(instrument);

        return instrument
            .getSecondaryJFCurrency()
            .getJavaCurrency();
    }

    public static final String baseCurrencyName(final Instrument instrument) {
        checkNotNull(instrument);

        return instrument
            .getPrimaryJFCurrency()
            .getCurrencyCode();
    }

    public static final String quoteCurrencyName(final Instrument instrument) {
        checkNotNull(instrument);

        return instrument
            .getSecondaryJFCurrency()
            .getCurrencyCode();
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
}
