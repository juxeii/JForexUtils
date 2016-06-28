package com.jforex.programming.math;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

import org.aeonbits.owner.ConfigFactory;
import org.paukov.combinatorics.Factory;
import org.paukov.combinatorics.Generator;

import com.dukascopy.api.Instrument;
import com.jforex.programming.instrument.InstrumentUtil;
import com.jforex.programming.settings.PlatformSettings;

public final class MathUtil {

    private final static PlatformSettings platformSettings = ConfigFactory.create(PlatformSettings.class);

    private MathUtil() {
    }

    public final static <T> Set<Set<T>> kPowerSet(final Set<T> sourceSet,
                                                  final int setSize) {
        final Set<Set<T>> kPowerSet = new HashSet<>();
        final Generator<T> generator =
                Factory.createSimpleCombinationGenerator(Factory.createVector(sourceSet), setSize);
        generator.forEach(kSubSet -> kPowerSet.add(new HashSet<>(kSubSet.getVector())));
        return kPowerSet;
    }

    public final static double rateOfReturn(final double currentValue,
                                            final double previousValue) {
        return 100.0 * (currentValue - previousValue) / previousValue;
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
