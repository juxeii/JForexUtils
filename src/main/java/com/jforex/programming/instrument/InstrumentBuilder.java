package com.jforex.programming.instrument;

import static java.util.stream.Collectors.toSet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;

import com.jforex.programming.misc.MathUtil;

import com.dukascopy.api.ICurrency;
import com.dukascopy.api.Instrument;

public final class InstrumentBuilder {

    private InstrumentBuilder() {
    }

    public final static Optional<Instrument> fromName(final String instrumentName) {
        return fromString(instrumentName.toUpperCase());
    }

    private final static Optional<Instrument> fromString(final String instrumentName) {
        return Instrument.isInverted(instrumentName)
                ? Optional.of(Instrument.fromInvertedString(instrumentName))
                : Optional.ofNullable(Instrument.fromString(instrumentName));
    }

    public final static Optional<Instrument> fromCurrencies(final ICurrency firstCurrency,
                                                            final ICurrency secondCurrency) {
        return fromString(InstrumentUtil.nameFromCurrencies(firstCurrency, secondCurrency));
    }

    public final static Set<Instrument> combineAllFromCurrencySet(final Set<ICurrency> currencies) {
        return fromCurrencyPairs(MathUtil.kPowerSet(currencies, 2)
                .stream()
                .map(InstrumentBuilder::currencyPairFromSet)
                .collect(toSet()));
    }

    private final static Pair<ICurrency, ICurrency> currencyPairFromSet(final Set<ICurrency> currencySet) {
        final List<ICurrency> currencyList = new ArrayList<>(currencySet);
        return Pair.of(currencyList.get(0), currencyList.get(1));
    }

    private final static Set<Instrument> fromCurrencyPairs(final Set<Pair<ICurrency, ICurrency>> currencyPairs) {
        return currencyPairs
                .stream()
                .map(currencyTuple -> fromCurrencyTuple(currencyTuple).get())
                .collect(toSet());
    }

    private final static Optional<Instrument> fromCurrencyTuple(final Pair<ICurrency, ICurrency> currencyTuple) {
        return fromCurrencies(currencyTuple.getLeft(), currencyTuple.getRight());
    }

    public final static Set<Instrument> combineAllWithAnchorCurrency(final ICurrency anchorCurrency,
                                                                     final Collection<ICurrency> partnerCurrencies) {
        return partnerCurrencies
                .stream()
                .map(partnerCurrency -> fromCurrencies(anchorCurrency, partnerCurrency))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(toSet());
    }
}
