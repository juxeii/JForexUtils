package com.jforex.programming.position;

import java.util.Collection;

import com.dukascopy.api.IOrder;

public interface RestoreSLTPPolicy {

    abstract double restoreSL(Collection<IOrder> ordersForMerge);

    abstract double restoreTP(Collection<IOrder> ordersForMerge);
}
