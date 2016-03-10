package com.jforex.programming.position;

import java.util.Collection;

import com.dukascopy.api.IOrder;

public final class RestoreSLTPData {

    private final double restoreSL;
    private final double restoreTP;

    public RestoreSLTPData(final RestoreSLTPPolicy restoreSLTPPolicy,
                           final Collection<IOrder> filledOrders) {
        restoreSL = restoreSLTPPolicy.restoreSL(filledOrders);
        restoreTP = restoreSLTPPolicy.restoreTP(filledOrders);
    }

    public final double sl() {
        return restoreSL;
    }

    public final double tp() {
        return restoreTP;
    }
}
