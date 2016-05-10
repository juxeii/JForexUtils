package com.jforex.programming.position;

import java.util.Set;

import com.dukascopy.api.IOrder;

public interface RestoreSLTPPolicy {

    public double restoreSL(Set<IOrder> ordersForMerge);

    public double restoreTP(Set<IOrder> ordersForMerge);
}
