package com.jforex.programming.misc;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.aeonbits.owner.ConfigFactory;
import org.paukov.combinatorics3.Generator;

import com.dukascopy.api.Instrument;
import com.jforex.programming.instrument.InstrumentUtil;
import com.jforex.programming.settings.PlatformSettings;

public final class MathUtil {

    private final static PlatformSettings platformSettings = ConfigFactory.create(PlatformSettings.class);

    private MathUtil() {
    }

    public final static <T> Set<Set<T>> kPowerSet(final Set<T> sourceSet,
                                                  final int k) {
        return Generator
                .combination(sourceSet)
                .simple(k)
                .stream()
                .map(list -> new HashSet<T>(list))
                .collect(Collectors.<Set<T>> toSet());
    }

    public final static double rateOfReturn(final double currentValue,
                                            final double previousValue) {
        return 100 * (currentValue - previousValue) / previousValue;
    }

    public final static double roundDouble(final double unroundedValue,
                                           final int digitPrecision) {
        return BigDecimal.valueOf(unroundedValue)
                .setScale(digitPrecision, BigDecimal.ROUND_HALF_UP)
                .doubleValue();
    }

    public final static double roundAmount(final double rawAmount) {
        return roundDouble(rawAmount, platformSettings.amountPrecision());
    }

    public final static double roundPips(final double pips) {
        return roundDouble(pips, platformSettings.pipPrecision());
    }

    public final static double roundPrice(final double rawPrice,
                                          final Instrument instrument) {
        return roundDouble(rawPrice, InstrumentUtil.numberOfDigits(instrument));
    }

    public final static boolean isValueDivisibleByX(final double value,
                                                    final double divisor) {
        return BigDecimal.valueOf(value)
                .remainder(BigDecimal.valueOf(divisor))
                .doubleValue() == 0;
    }
}
