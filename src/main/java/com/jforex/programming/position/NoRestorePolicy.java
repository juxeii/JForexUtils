package com.jforex.programming.position;

import static com.jforex.programming.misc.JForexUtil.pfs;

import java.util.Collection;

import com.dukascopy.api.IOrder;

public final class NoRestorePolicy implements RestoreSLTPPolicy {

    @Override
    public final double restoreSL(final Collection<IOrder> ordersForMerge) {
        return pfs.NO_STOP_LOSS_PRICE();
    }

    @Override
    public final double restoreTP(final Collection<IOrder> ordersForMerge) {
        return pfs.NO_TAKE_PROFIT_PRICE();
    }
}
