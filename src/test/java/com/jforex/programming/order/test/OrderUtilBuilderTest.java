package com.jforex.programming.order.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.Set;
import java.util.concurrent.Callable;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import com.dukascopy.api.IOrder;
import com.google.common.collect.Sets;
import com.jforex.programming.misc.IEngineUtil;
import com.jforex.programming.order.OrderUtilBuilder;
import com.jforex.programming.order.command.MergeCommand;
import com.jforex.programming.order.command.SubmitCommand;
import com.jforex.programming.test.common.InstrumentUtilForTest;

import de.bechte.junit.runners.context.HierarchicalContextRunner;

@RunWith(HierarchicalContextRunner.class)
public class OrderUtilBuilderTest extends InstrumentUtilForTest {

    private OrderUtilBuilder orerUtilBuilder;

    @Mock
    private IEngineUtil engineUtilMock;
    @Mock
    private Callable<IOrder> callableMock;

    @Before
    public void setUp() {
        orerUtilBuilder = new OrderUtilBuilder(engineUtilMock);
    }

    public class SubmitBuilderTests {

        private SubmitCommand submitCommand;

        @Before
        public void setUp() {
            when(engineUtilMock.submitCallable(buyParamsEURUSD))
                .thenReturn(callableMock);

            submitCommand = orerUtilBuilder
                .submitBuilder(buyParamsEURUSD)
                .build();
        }

        @Test
        public void callableIsSet() {
            assertThat(submitCommand.callable(), equalTo(callableMock));
        }

        @Test
        public void noInteractionsHappensAtCreation() {
            verifyZeroInteractions(callableMock);
        }
    }

    public class MergeBuilderTests {

        private MergeCommand mergeCommand;
        private final String mergeOrderLabel = "mergeOrderLabel";
        private final Set<IOrder> toMergeOrders = Sets.newHashSet(buyOrderEURUSD, sellOrderEURUSD);

        @Before
        public void setUp() {
            when(engineUtilMock.mergeCallable(mergeOrderLabel, toMergeOrders))
                .thenReturn(callableMock);

            mergeCommand = orerUtilBuilder
                .mergeBuilder(mergeOrderLabel, toMergeOrders)
                .build();
        }

        @Test
        public void callableIsSet() {
            assertThat(mergeCommand.callable(), equalTo(callableMock));
        }

        @Test
        public void noInteractionsHappensAtCreation() {
            verifyZeroInteractions(callableMock);
        }
    }

    @Test
    public void noInteractionsHappensAtCloseCreation() {
        orerUtilBuilder
            .closeBuilder(buyOrderEURUSD)
            .build();

        verifyZeroInteractions(callableMock);
    }

    @Test
    public void noInteractionsHappensAtSetLabelCreation() {
        orerUtilBuilder
            .setLabelBuilder(buyOrderEURUSD, "newLabel")
            .build();

        verifyZeroInteractions(callableMock);
    }

    @Test
    public void noInteractionsHappensAtSetAmountCreation() {
        orerUtilBuilder
            .setAmountBuilder(buyOrderEURUSD, 0.12)
            .build();

        verifyZeroInteractions(callableMock);
    }

    @Test
    public void noInteractionsHappensAtSetOpenPriceCreation() {
        orerUtilBuilder
            .setOpenPriceBuilder(buyOrderEURUSD, 1.1234)
            .build();

        verifyZeroInteractions(callableMock);
    }

    @Test
    public void noInteractionsHappensAtSetSLCreation() {
        orerUtilBuilder
            .setSLBuilder(buyOrderEURUSD, 1.1234)
            .build();

        verifyZeroInteractions(callableMock);
    }

    @Test
    public void noInteractionsHappensAtSetTPCreation() {
        orerUtilBuilder
            .setTPBuilder(buyOrderEURUSD, 1L)
            .build();

        verifyZeroInteractions(callableMock);
    }
}
