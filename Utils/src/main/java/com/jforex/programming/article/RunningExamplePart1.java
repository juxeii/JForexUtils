package com.jforex.programming.article;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import com.dukascopy.api.IAccount;
import com.dukascopy.api.IBar;
import com.dukascopy.api.IContext;
import com.dukascopy.api.ICurrency;
import com.dukascopy.api.IMessage;
import com.dukascopy.api.IStrategy;
import com.dukascopy.api.ITick;
import com.dukascopy.api.Instrument;
import com.dukascopy.api.JFException;
import com.dukascopy.api.Period;
import com.jforex.programming.currency.CurrencyBuilder;
import com.jforex.programming.currency.CurrencyUtil;
import com.jforex.programming.instrument.InstrumentBuilder;
import com.jforex.programming.misc.JForexUtil;

public class RunningExamplePart1 implements IStrategy {

    private JForexUtil jForexUtil;

    @Override
    public void onStart(final IContext context) throws JFException {
        jForexUtil = new JForexUtil(context);

        // Create ICurrency instances from currency name/string
        final ICurrency eurCurrency = CurrencyBuilder.fromName("EUR").get();
        final ICurrency eurCurrencyMixedCase = CurrencyBuilder.fromName("eUr").get();
        final Optional<ICurrency> invalidCurrency = CurrencyBuilder.fromName("ouch");

        // Create ICurrency instances from multiple currency names/strings
        final Set<String> currencyNames =
                new HashSet<String>(Arrays.asList("EUR", "CHF", "JPY", "ouch"));
        final Set<ICurrency> currencySetA = CurrencyBuilder.fromNames(currencyNames);
        final Set<ICurrency> currencySetB = CurrencyBuilder.fromNames("EUR", "CHF", "JPY", "ouch");

        // Create ICurrency instances from instrument(s)
        final Set<ICurrency> currencySetFromSingleInstrument =
                CurrencyBuilder.fromInstrument(Instrument.EURUSD);
        final Set<Instrument> instruments =
                new HashSet<Instrument>(Arrays.asList(Instrument.EURUSD, Instrument.CHFJPY));
        final Set<ICurrency> currencySetFromInstrumentsA =
                CurrencyBuilder.fromInstruments(instruments);
        final Set<ICurrency> currencySetFromInstrumentsB =
                CurrencyBuilder.fromInstruments(Instrument.EURUSD, Instrument.CHFJPY);

        // Check if a currency name is valid
        final boolean isValidEURCurrency = CurrencyUtil.isNameValid("EUR");
        final boolean isValidEURCurrencyLowerCase = CurrencyUtil.isNameValid("eur");
        final boolean isValidForInvalidName = CurrencyUtil.isNameValid("ouch");

        System.out.println("isValidEURCurrency: " + isValidEURCurrency
                + " isValidEURCurrencyLowerCase: " + isValidEURCurrencyLowerCase
                + " isValidForInvalidName: " + isValidForInvalidName);

        // Checking if an instrument contains a currency
        final boolean isEURinEURUSD = CurrencyUtil.isInInstrument("EUR", Instrument.EURUSD);
        final boolean isJPYinEURUSD = CurrencyUtil.isInInstrument("JPY", Instrument.EURUSD);
        final ICurrency jpyCurrency = CurrencyBuilder.fromName("JPY").get();
        final ICurrency audCurrency = CurrencyBuilder.fromName("AUD").get();

        final boolean isEURinInstrumentSet = CurrencyUtil.isInInstruments(eurCurrency, instruments);
        final boolean isJPYinInstrumentSet = CurrencyUtil.isInInstruments(jpyCurrency, instruments);
        final boolean isAUDinInstrumentSet = CurrencyUtil.isInInstruments(audCurrency, instruments);

        System.out.println("isEURinEURUSD: " + isEURinEURUSD
                + " isJPYinEURUSD: " + isJPYinEURUSD
                + " isEURinInstrumentSet: " + isEURinInstrumentSet
                + " isJPYinInstrumentSet: " + isJPYinInstrumentSet
                + " isAUDinInstrumentSet: " + isAUDinInstrumentSet);

        // Creating instrument from name/string or from currencies
        final Instrument instrumentEURUSD = InstrumentBuilder.fromName("EUR/USD").get();
        final Instrument instrumentEURUSDInverted = InstrumentBuilder.fromName("USD/EUR").get();
        final Optional<Instrument> invalidEURInstrument = InstrumentBuilder.fromName("EURUSD");
        final Instrument instrumentGBPAUD = InstrumentBuilder.fromCurrencies(CurrencyBuilder.fromName("GBP").get(),
                                                                             CurrencyBuilder.fromName("AUD").get())
                                                             .get();

        // Combining one anchor currency with partner currencies
        final ICurrency anchorCurrency = CurrencyBuilder.fromName("EUR").get();
        final Set<ICurrency> partnerCurrencies = CurrencyBuilder.fromNames("EUR", "USD", "AUD", "JPY");
        final Set<Instrument> anchorInstruments =
                InstrumentBuilder.combineAllWithAnchorCurrency(anchorCurrency, partnerCurrencies);
        System.out.println("anchorInstruments are: " + Arrays.toString(anchorInstruments.toArray()));

        // Combining several currencies to all possible instruments
        final Set<ICurrency> currencies = CurrencyBuilder.fromNames("EUR", "USD", "AUD", "JPY");
        final Set<Instrument> combinedInstruments =
                InstrumentBuilder.combineAllFromCurrencySet(currencies);
        System.out.println("combinedInstruments are: " + Arrays.toString(combinedInstruments.toArray()));
    }

    @Override
    public void onStop() throws JFException {
        jForexUtil.onStop();
    }

    @Override
    public void onMessage(final IMessage message) throws JFException {
        jForexUtil.onMessage(message);
    }

    @Override
    public void onTick(final Instrument instrument,
                       final ITick tick) throws JFException {
        jForexUtil.onTick(instrument, tick);
    }

    @Override
    public void onBar(final Instrument instrument,
                      final Period period,
                      final IBar askBar,
                      final IBar bidBar) throws JFException {
        jForexUtil.onBar(instrument, period, askBar, bidBar);
    }

    @Override
    public void onAccount(final IAccount account) throws JFException {

    }
}
