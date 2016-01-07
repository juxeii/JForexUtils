package com.jforex.programming.test.common;

import java.util.Collection;

import com.dukascopy.api.IOrder;
import com.jforex.programming.misc.CalculationUtil;
import com.jforex.programming.order.OrderStaticUtil;
import com.jforex.programming.position.RestoreSLTPPolicy;

public class RestoreSLTPPolicyAdd20Pips implements RestoreSLTPPolicy {

    private final double tickQuote;
    private final double pipsToAdd = 20;

    public RestoreSLTPPolicyAdd20Pips(final double tickQuote) {
        this.tickQuote = tickQuote;
    }

    @Override
    public double restoreSL(final Collection<IOrder> mergedOrders) {
        return getValue(mergedOrders, 1);
    }

    @Override
    public double restoreTP(final Collection<IOrder> mergedOrders) {
        return getValue(mergedOrders, -1);
    }

    private double getValue(final Collection<IOrder> mergedOrders,
                            final double factor) {
        final IOrder someOrder = mergedOrders.iterator().next();
        final double signedPositionAmount = OrderStaticUtil.combinedSignedAmount(mergedOrders);
        if (signedPositionAmount > 0)
            return CalculationUtil.addPips(someOrder.getInstrument(), tickQuote, pipsToAdd);
        else
            return CalculationUtil.addPips(someOrder.getInstrument(), tickQuote, -pipsToAdd);
    }
}
