package com.jforex.programming.position;

import java.util.Set;

import org.aeonbits.owner.ConfigFactory;

import com.dukascopy.api.IOrder;
import com.jforex.programming.settings.PlatformSettings;

public final class NoRestorePolicy implements RestoreSLTPPolicy {

    private final static PlatformSettings platformSettings = ConfigFactory.create(PlatformSettings.class);

    @Override
    public final double restoreSL(final Set<IOrder> ordersForMerge) {
        return platformSettings.noSLPrice();
    }

    @Override
    public final double restoreTP(final Set<IOrder> ordersForMerge) {
        return platformSettings.noTPPrice();
    }
}
