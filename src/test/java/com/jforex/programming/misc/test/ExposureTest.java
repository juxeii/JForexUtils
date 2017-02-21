package com.jforex.programming.misc.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.dukascopy.api.IOrder;
import com.dukascopy.api.JFException;
import com.jforex.programming.math.MathUtil;
import com.jforex.programming.misc.Exposure;
import com.jforex.programming.test.common.InstrumentUtilForTest;

import de.bechte.junit.runners.context.HierarchicalContextRunner;

@RunWith(HierarchicalContextRunner.class)
public class ExposureTest extends InstrumentUtilForTest {

    private Exposure exposure;
    private final List<IOrder> orderList = new ArrayList<>();

    @Before
    public void setUp() throws JFException {
        orderList.add(buyOrderEURUSD);
        orderList.add(sellOrderEURUSD);

        when(engineMock.getOrders(instrumentEURUSD))
            .thenReturn(orderList);

        exposure = new Exposure(engineMock);
    }

    @Test
    public void getReturnsCorrectExposure() throws JFException {
        final double returnedExposure = exposure.get(instrumentEURUSD);
        final double expectedExposure =
                MathUtil.roundAmount(buyOrderEURUSD.getAmount() - sellOrderEURUSD.getAmount());

        assertThat(expectedExposure, equalTo(returnedExposure));

        verify(engineMock).getOrders(instrumentEURUSD);
    }

    public class WhenEngineException {

        @Before
        public void setUp() throws JFException {
            when(engineMock.getOrders(instrumentEURUSD))
                .thenThrow(jfException);

            orderUtilForTest.setAmount(buyOrderEURUSD, 25.0);
        }

        @Test
        public void getReturnsZero() {
            assertThat(exposure.get(instrumentEURUSD), equalTo(0.0));
        }

        @Test
        public void wouldExceedIsFalse() {
            assertFalse(exposure.wouldExceed(instrumentEURUSD, 0.1));
        }
    }

    public class ExposureExceededForBuyOrder {

        @Before
        public void setUp() {
            orderList.remove(sellOrderEURUSD);

            orderUtilForTest.setAmount(buyOrderEURUSD, 25.0);
        }

        @Test
        public void wouldExceedForPositiveAmount() {
            assertTrue(exposure.wouldExceed(instrumentEURUSD, 0.1));
        }

        @Test
        public void wouldNotExceedForNegativeAmount() {
            assertFalse(exposure.wouldExceed(instrumentEURUSD, -0.001));
        }
    }

    public class ExposureExceededForSellOrder {

        @Before
        public void setUp() {
            orderList.remove(buyOrderEURUSD);

            orderUtilForTest.setAmount(sellOrderEURUSD, 25.0);
        }

        @Test
        public void wouldNotExceedForPositiveAmount() {
            assertFalse(exposure.wouldExceed(instrumentEURUSD, 0.1));
        }

        @Test
        public void wouldExceedForNegativeAmount() {
            assertTrue(exposure.wouldExceed(instrumentEURUSD, -0.001));
        }
    }
}
