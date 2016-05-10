package com.jforex.programming.position;

import java.util.Collection;

import com.dukascopy.api.IOrder;

public interface RestoreSLTPPolicy {

    public double restoreSL(Collection<IOrder> ordersForMerge);

    public double restoreTP(Collection<IOrder> ordersForMerge);
}
