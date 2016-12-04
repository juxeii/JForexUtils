package com.jforex.programming.math;

import static com.google.common.base.Preconditions.checkNotNull;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

import org.paukov.combinatorics3.Generator;

import com.dukascopy.api.Instrument;
import com.google.common.collect.Sets;
import com.jforex.programming.instrument.InstrumentUtil;
import com.jforex.programming.settings.PlatformSettings;
import com.jforex.programming.strategy.StrategyUtil;

public final class MathUtil {

    private MathUtil() {
    }

    private static final PlatformSettings platformSettings = StrategyUtil.platformSettings;

    public static final <T> Set<Set<T>> kPowerSet(final Collection<T> sourceSet,
                                                  final int setSize) {
        checkNotNull(sourceSet);

        return Generator
            .combination(sourceSet)
            .simple(setSize)
            .stream()
            .map(Sets::newHashSet)
            .collect(Collectors.toSet());
    }

    public static final double rateOfReturn(final double currentValue,
                                            final double previousValue) {
        return 100.0 * (currentValue - previousValue) / previousValue;
    }

    public static final double roundDouble(final double rawValue,
                                           final int digitPrecision) {
        return BigDecimal
            .valueOf(rawValue)
            .setScale(digitPrecision, BigDecimal.ROUND_HALF_UP)
            .doubleValue();
    }

    public static final double roundAmount(final double rawAmount) {
        return roundDouble(rawAmount, platformSettings.amountPrecision());
    }

    public static final double roundPips(final double rawPips) {
        return roundDouble(rawPips, platformSettings.pipPrecision());
    }

    public static final double roundPrice(final double rawPrice,
                                          final Instrument instrument) {
        checkNotNull(instrument);

        return roundDouble(rawPrice, InstrumentUtil.numberOfDigits(instrument));
    }

    public static final boolean isValueDivisibleByX(final double value,
                                                    final double divisor) {
        return BigDecimal
            .valueOf(value)
            .remainder(BigDecimal.valueOf(divisor))
            .doubleValue() == 0;
    }

    public static final double scaleAmountForPlatform(final double amount) {
        return roundAmount(amount / platformSettings.baseAmount());
    }
}
