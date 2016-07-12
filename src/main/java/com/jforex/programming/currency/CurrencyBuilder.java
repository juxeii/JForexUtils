package com.jforex.programming.currency;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toSet;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import com.dukascopy.api.ICurrency;
import com.dukascopy.api.Instrument;
import com.dukascopy.api.JFCurrency;

public final class CurrencyBuilder {

    private CurrencyBuilder() {
    }

    public static final Optional<ICurrency> fromName(final String currencyName) {
        return CurrencyUtil.isNameValid(currencyName)
                ? Optional.of(instanceFromName(currencyName))
                : Optional.empty();
    }

    public static final ICurrency instanceFromName(final String currencyName) {
        return JFCurrency.getInstance(currencyName.toUpperCase());
    }

    public static final Set<ICurrency> fromNames(final Collection<String> currencyNames) {
        return currencyNames
                .stream()
                .map(CurrencyBuilder::fromName)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(toSet());
    }

    public static final Set<ICurrency> fromNames(final String... currencyNames) {
        return fromNames(asList(currencyNames));
    }

    public static final Set<ICurrency> fromInstrument(final Instrument instrument) {
        return Stream.of(instrument.getPrimaryJFCurrency(),
                         instrument.getSecondaryJFCurrency())
                .collect(toSet());
    }

    public static final Set<ICurrency> fromInstruments(final Collection<Instrument> instruments) {
        return instruments
                .stream()
                .map(CurrencyBuilder::fromInstrument)
                .flatMap(Set::stream)
                .collect(toSet());
    }

    public static final Set<ICurrency> fromInstruments(final Instrument... instruments) {
        return fromInstruments(asList(instruments));
    }
}
