package com.jforex.programming.currency;

import java.util.Collection;

import com.dukascopy.api.ICurrency;
import com.dukascopy.api.Instrument;

public final class CurrencyUtil {

    private CurrencyUtil() {
    }

    public static final boolean isNameValid(final String currencyName) {
        final ICurrency currency = CurrencyBuilder.instanceFromName(currencyName);
        return currency.getJavaCurrency() != null;
    }

    public static final boolean isInInstrument(final ICurrency currency,
                                               final Instrument instrument) {
        return equalsBaseCurrency(currency, instrument)
                || equalsQuoteCurrency(currency, instrument);
    }

    public static final boolean isInInstrument(final String currencyName,
                                               final Instrument instrument) {
        return CurrencyBuilder.fromName(currencyName)
                              .map(currency -> isInInstrument(currency, instrument))
                              .orElse(false);
    }

    public static final boolean equalsBaseCurrency(final ICurrency currency,
                                                   final Instrument instrument) {
        return instrument.getPrimaryJFCurrency().equals(currency);
    }

    public static final boolean equalsQuoteCurrency(final ICurrency currency,
                                                    final Instrument instrument) {
        return instrument.getSecondaryJFCurrency().equals(currency);
    }

    public static final boolean isInInstruments(final ICurrency currency,
                                                final Collection<Instrument> instruments) {
        return instruments.stream()
                          .anyMatch(instrument -> isInInstrument(currency, instrument));
    }
}
