package com.jforex.programming.currency;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Collection;

import com.dukascopy.api.ICurrency;
import com.dukascopy.api.Instrument;

public final class CurrencyUtil {

    private CurrencyUtil() {
    }

    public static final boolean isNameValid(final String currencyName) {
        return CurrencyBuilder
                .instanceFromName(checkNotNull(currencyName))
                .getJavaCurrency() != null;
    }

    public static final boolean isInInstrument(final ICurrency currency,
                                               final Instrument instrument) {
        checkNotNull(currency);
        checkNotNull(instrument);

        return equalsBaseCurrency(currency, instrument)
                || equalsQuoteCurrency(currency, instrument);
    }

    public static final boolean isInInstrument(final String currencyName,
                                               final Instrument instrument) {
        checkNotNull(currencyName);
        checkNotNull(instrument);

        return CurrencyBuilder
                .maybeFromName(currencyName)
                .map(currency -> isInInstrument(currency, instrument))
                .orElse(false);
    }

    public static final boolean equalsBaseCurrency(final ICurrency currency,
                                                   final Instrument instrument) {
        checkNotNull(currency);
        checkNotNull(instrument);

        return instrument
                .getPrimaryJFCurrency()
                .equals(currency);
    }

    public static final boolean equalsQuoteCurrency(final ICurrency currency,
                                                    final Instrument instrument) {
        checkNotNull(currency);
        checkNotNull(instrument);

        return instrument
                .getSecondaryJFCurrency()
                .equals(currency);
    }

    public static final boolean isInInstruments(final ICurrency currency,
                                                final Collection<Instrument> instruments) {
        checkNotNull(currency);
        checkNotNull(instruments);

        return instruments
                .stream()
                .anyMatch(instrument -> isInInstrument(currency, instrument));
    }
}
