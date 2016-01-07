package com.jforex.programming.misc;

import static com.jforex.programming.misc.JForexUtil.pfs;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.paukov.combinatorics.Factory;
import org.paukov.combinatorics.Generator;

import com.jforex.programming.instrument.InstrumentUtil;

import com.dukascopy.api.Instrument;

public final class MathUtil {

    private MathUtil() {
    }

    public final static <T> Set<Set<T>> kPowerSet(final Set<T> sourceSet,
                                                  final int setSize) {
        return setSize >= 0 ? kPowerSetForKNotZero(sourceSet, setSize) : Collections.emptySet();
    }

    private final static <T> Set<Set<T>> kPowerSetForKNotZero(final Set<T> sourceSet,
                                                              final int setSize) {
        final Set<Set<T>> kPowerSet = new HashSet<>();
        final Generator<T> generator =
                Factory.createSimpleCombinationGenerator(Factory.createVector(sourceSet), setSize);
        generator.forEach(kSubSet -> kPowerSet.add(new HashSet<>(kSubSet.getVector())));
        return kPowerSet;
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
        return roundDouble(rawAmount, pfs.AMOUNT_PRECISION());
    }

    public final static double roundPips(final double pips) {
        return roundDouble(pips, pfs.PIP_PRECISION());
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
