package com.jforex.programming.instrument;

import static java.util.stream.Collectors.toSet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;

import com.jforex.programming.math.MathUtil;
import com.jforex.programming.misc.StreamUtil;

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
        return MathUtil
                .kPowerSet(currencies, 2)
                .stream()
                .map(ArrayList<ICurrency>::new)
                .map(pair -> fromCurrencies(pair.get(0), pair.get(1)))
                .flatMap(StreamUtil::streamOptional)
                .collect(toSet());
    }

    public final static Set<Instrument> combineAllWithAnchorCurrency(final ICurrency anchorCurrency,
                                                                     final Collection<ICurrency> partnerCurrencies) {
        return partnerCurrencies
                .stream()
                .map(partnerCurrency -> fromCurrencies(anchorCurrency, partnerCurrency))
                .flatMap(StreamUtil::streamOptional)
                .collect(toSet());
    }
}
