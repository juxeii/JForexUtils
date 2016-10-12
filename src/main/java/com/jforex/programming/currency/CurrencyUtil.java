package com.jforex.programming.currency;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Collection;

import com.dukascopy.api.ICurrency;
import com.dukascopy.api.Instrument;
import com.dukascopy.api.JFCurrency;

public final class CurrencyUtil {

    private CurrencyUtil() {
    }

    public static final boolean isNameValid(final String currencyName) {
        checkNotNull(currencyName);

        return JFCurrency
            .getInstance(currencyName.toUpperCase())
            .getJavaCurrency() != null;
    }

    public static final boolean isInInstrument(final ICurrency currency,
                                               final Instrument instrument) {
        checkNotNull(currency);
        checkNotNull(instrument);

        return instrument.getPrimaryJFCurrency().equals(currency)
                || instrument.getSecondaryJFCurrency().equals(currency);
    }

    public static final boolean isInInstrument(final String currencyName,
                                               final Instrument instrument) {
        checkNotNull(currencyName);
        checkNotNull(instrument);

        return CurrencyFactory
            .maybeFromName(currencyName)
            .map(currency -> isInInstrument(currency, instrument))
            .orElse(false);
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
