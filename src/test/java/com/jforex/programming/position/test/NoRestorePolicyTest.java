package com.jforex.programming.position.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.Set;

import org.aeonbits.owner.ConfigFactory;
import org.junit.Before;
import org.junit.Test;

import com.dukascopy.api.IOrder;
import com.google.common.collect.Sets;
import com.jforex.programming.position.NoRestorePolicy;
import com.jforex.programming.settings.PlatformSettings;
import com.jforex.programming.test.fakes.IOrderForTest;

public class NoRestorePolicyTest {

    private NoRestorePolicy noRestorePolicy;

    private final Set<IOrder> ordersForMerge = Sets.newHashSet(IOrderForTest.buyOrderEURUSD(),
                                                               IOrderForTest.sellOrderEURUSD());

    private static final PlatformSettings platformSettings =
            ConfigFactory.create(PlatformSettings.class);

    @Before
    public void setUp() {
        noRestorePolicy = new NoRestorePolicy();
    }

    @Test
    public void restoreSLReturnsNoSLPrice() {
        assertThat(noRestorePolicy.restoreSL(ordersForMerge),
                   equalTo(platformSettings.noSLPrice()));
    }

    @Test
    public void restoreTPReturnsNoTPPrice() {
        assertThat(noRestorePolicy.restoreTP(ordersForMerge),
                   equalTo(platformSettings.noTPPrice()));
    }
}
