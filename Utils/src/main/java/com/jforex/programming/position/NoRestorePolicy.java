package com.jforex.programming.position;

import static com.jforex.programming.misc.JForexUtil.pfs;

import java.util.Collection;

import com.dukascopy.api.IOrder;

public class NoRestorePolicy implements RestoreSLTPPolicy {

    @Override
    public double restoreSL(final Collection<IOrder> ordersForMerge) {
        return pfs.NO_STOP_LOSS_PRICE();
    }

    @Override
    public double restoreTP(final Collection<IOrder> ordersForMerge) {
        return pfs.NO_TAKE_PROFIT_PRICE();
    }
}
