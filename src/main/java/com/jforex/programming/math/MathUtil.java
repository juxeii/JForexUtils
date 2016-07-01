package com.jforex.programming.math;

import java.math.BigDecimal;
import java.util.Set;

import org.aeonbits.owner.ConfigFactory;
import org.paukov.combinatorics.Factory;
import org.paukov.combinatorics.Generator;

import com.dukascopy.api.Instrument;
import com.google.common.collect.Sets;
import com.jforex.programming.instrument.InstrumentUtil;
import com.jforex.programming.settings.PlatformSettings;

public final class MathUtil {

    private final static PlatformSettings platformSettings = ConfigFactory.create(PlatformSettings.class);

    private MathUtil() {
    }

    public final static <T> Set<Set<T>> kPowerSet(final Set<T> sourceSet,
                                                  final int setSize) {
        final Set<Set<T>> kPowerSet = Sets.newHashSet();
        final Generator<T> generator =
                Factory.createSimpleCombinationGenerator(Factory.createVector(sourceSet), setSize);
        generator.forEach(kSubSet -> kPowerSet.add(Sets.newHashSet(kSubSet.getVector())));
        return kPowerSet;
    }

    public final static double rateOfReturn(final double currentValue,
                                            final double previousValue) {
        return 100.0 * (currentValue - previousValue) / previousValue;
    }

    public final static double roundDouble(final double rawValue,
                                           final int digitPrecision) {
        return BigDecimal.valueOf(rawValue)
                .setScale(digitPrecision, BigDecimal.ROUND_HALF_UP)
                .doubleValue();
    }

    public final static double roundAmount(final double rawAmount) {
        return roundDouble(rawAmount, platformSettings.amountPrecision());
    }

    public final static double roundPips(final double rawPips) {
        return roundDouble(rawPips, platformSettings.pipPrecision());
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
