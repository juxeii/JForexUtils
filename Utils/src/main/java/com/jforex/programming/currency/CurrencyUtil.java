package com.jforex.programming.currency;

import java.util.Collection;
import java.util.Optional;

import com.dukascopy.api.ICurrency;
import com.dukascopy.api.Instrument;

public final class CurrencyUtil {

    private CurrencyUtil() {
    }

    public final static boolean isNameValid(final String currencyName) {
        final ICurrency currency = CurrencyBuilder.instanceFromName(currencyName);
        return currency.getJavaCurrency() != null;
    }

    public final static boolean isInInstrument(final ICurrency currency,
                                               final Instrument instrument) {
        return equalsBaseCurrency(currency, instrument) || equalsQuoteCurrency(currency, instrument);
    }

    public final static boolean isInInstrument(final String currencyName,
                                               final Instrument instrument) {
        final Optional<ICurrency> currencyOpt = CurrencyBuilder.fromName(currencyName);
        return currencyOpt.isPresent()
                ? isInInstrument(currencyOpt.get(), instrument)
                : false;
    }

    public final static boolean equalsBaseCurrency(final ICurrency currency,
                                                   final Instrument instrument) {
        return instrument.getPrimaryJFCurrency().equals(currency);
    }

    public final static boolean equalsQuoteCurrency(final ICurrency currency,
                                                    final Instrument instrument) {
        return instrument.getSecondaryJFCurrency().equals(currency);
    }

    public final static boolean isInInstruments(final ICurrency currency,
                                                final Collection<Instrument> instruments) {
        return instruments.stream()
                          .anyMatch(instrument -> isInInstrument(currency, instrument));
    }
}
