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
import com.jforex.programming.order.OrderUtilCompletable;
import com.jforex.programming.order.command.CloseCommand;
import com.jforex.programming.order.command.MergeCommand;
import com.jforex.programming.order.command.SetAmountCommand;
import com.jforex.programming.order.command.SetGTTCommand;
import com.jforex.programming.order.command.SetLabelCommand;
import com.jforex.programming.order.command.SetOpenPriceCommand;
import com.jforex.programming.order.command.SetSLCommand;
import com.jforex.programming.order.command.SetTPCommand;
import com.jforex.programming.order.command.SubmitCommand;
import com.jforex.programming.test.common.InstrumentUtilForTest;

import de.bechte.junit.runners.context.HierarchicalContextRunner;

@RunWith(HierarchicalContextRunner.class)
public class OrderUtilBuilderTest extends InstrumentUtilForTest {

    private OrderUtilBuilder orerUtilBuilder;

    @Mock
    private IEngineUtil engineUtilMock;
    @Mock
    private OrderUtilCompletable orderUtilCompletableMock;
    @Mock
    private Callable<IOrder> callableMock;

    @Before
    public void setUp() {
        orerUtilBuilder = new OrderUtilBuilder(engineUtilMock, orderUtilCompletableMock);
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
        public void orderParamsIsSet() {
            assertThat(submitCommand.orderParams(), equalTo(buyParamsEURUSD));
        }

        @Test
        public void callableIsSet() {
            assertThat(submitCommand.callable(), equalTo(callableMock));
        }

        @Test
        public void noInteractionsHappensAtCreation() {
            verifyZeroInteractions(callableMock);
            verifyZeroInteractions(orderUtilCompletableMock);
        }

        @Test
        public void onSubscribeSubmitOrderIsInvokedOnOrderUtilCompletable() {
            when(orderUtilCompletableMock.submitOrder(submitCommand))
                .thenReturn(emptyCompletable());

            submitCommand
                .completable()
                .subscribe();

            verify(orderUtilCompletableMock).submitOrder(submitCommand);
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
        public void mergeOrderLabelIsSet() {
            assertThat(mergeCommand.mergeOrderLabel(), equalTo(mergeOrderLabel));
        }

        @Test
        public void toMergeOrdersIsSet() {
            assertThat(mergeCommand.toMergeOrders(), equalTo(toMergeOrders));
        }

        @Test
        public void callableIsSet() {
            assertThat(mergeCommand.callable(), equalTo(callableMock));
        }

        @Test
        public void noInteractionsHappensAtCreation() {
            verifyZeroInteractions(callableMock);
            verifyZeroInteractions(orderUtilCompletableMock);
        }

        @Test
        public void onSubscribeMergeOrdersIsInvokedOnOrderUtilCompletable() {
            when(orderUtilCompletableMock.mergeOrders(mergeCommand))
                .thenReturn(emptyCompletable());

            mergeCommand
                .completable()
                .subscribe();

            verify(orderUtilCompletableMock).mergeOrders(mergeCommand);
        }
    }

    public class CloseBuilderTests {

        private CloseCommand closeCommand;

        @Before
        public void setUp() {
            closeCommand = orerUtilBuilder
                .closeBuilder(buyOrderEURUSD)
                .build();
        }

        @Test
        public void orderIsSet() {
            assertThat(closeCommand.order(), equalTo(buyOrderEURUSD));
        }

        @Test
        public void noInteractionsHappensAtCreation() {
            verifyZeroInteractions(callableMock);
            verifyZeroInteractions(orderUtilCompletableMock);
        }

        @Test
        public void onSubscribeCloseIsInvokedOnOrderUtilCompletable() {
            when(orderUtilCompletableMock.close(closeCommand))
                .thenReturn(emptyCompletable());

            closeCommand
                .completable()
                .subscribe();

            verify(orderUtilCompletableMock).close(closeCommand);
        }
    }

    public class LabelBuilderTests {

        private SetLabelCommand setLabelCommand;
        private final String newLabel = "newLabel";

        @Before
        public void setUp() {
            setLabelCommand = orerUtilBuilder
                .setLabelBuilder(buyOrderEURUSD, newLabel)
                .build();
        }

        @Test
        public void orderIsSet() {
            assertThat(setLabelCommand.order(), equalTo(buyOrderEURUSD));
        }

        @Test
        public void noInteractionsHappensAtCreation() {
            verifyZeroInteractions(callableMock);
            verifyZeroInteractions(orderUtilCompletableMock);
        }

        @Test
        public void onSubscribeCloseIsInvokedOnOrderUtilCompletable() {
            when(orderUtilCompletableMock.setLabel(setLabelCommand))
                .thenReturn(emptyCompletable());

            setLabelCommand
                .completable()
                .subscribe();

            verify(orderUtilCompletableMock).setLabel(setLabelCommand);
        }
    }

    public class GTTBuilderTests {

        private SetGTTCommand setGTTCommand;
        private final long newGTT = 1L;

        @Before
        public void setUp() {
            setGTTCommand = orerUtilBuilder
                .setGTTBuilder(buyOrderEURUSD, newGTT)
                .build();
        }

        @Test
        public void orderIsSet() {
            assertThat(setGTTCommand.order(), equalTo(buyOrderEURUSD));
        }

        @Test
        public void noInteractionsHappensAtCreation() {
            verifyZeroInteractions(callableMock);
            verifyZeroInteractions(orderUtilCompletableMock);
        }

        @Test
        public void onSubscribeCloseIsInvokedOnOrderUtilCompletable() {
            when(orderUtilCompletableMock.setGTT(setGTTCommand))
                .thenReturn(emptyCompletable());

            setGTTCommand
                .completable()
                .subscribe();

            verify(orderUtilCompletableMock).setGTT(setGTTCommand);
        }
    }

    public class AmountBuilderTests {

        private SetAmountCommand setAmountCommand;
        private final double newAmount = 1L;

        @Before
        public void setUp() {
            setAmountCommand = orerUtilBuilder
                .setAmountBuilder(buyOrderEURUSD, newAmount)
                .build();
        }

        @Test
        public void orderIsSet() {
            assertThat(setAmountCommand.order(), equalTo(buyOrderEURUSD));
        }

        @Test
        public void noInteractionsHappensAtCreation() {
            verifyZeroInteractions(callableMock);
            verifyZeroInteractions(orderUtilCompletableMock);
        }

        @Test
        public void onSubscribeCloseIsInvokedOnOrderUtilCompletable() {
            when(orderUtilCompletableMock.setAmount(setAmountCommand))
                .thenReturn(emptyCompletable());

            setAmountCommand
                .completable()
                .subscribe();

            verify(orderUtilCompletableMock).setAmount(setAmountCommand);
        }
    }

    public class OpenPriceBuilderTests {

        private SetOpenPriceCommand setOpenPriceCommand;
        private final double newOpenPrice = 1L;

        @Before
        public void setUp() {
            setOpenPriceCommand = orerUtilBuilder
                .setOpenPriceBuilder(buyOrderEURUSD, newOpenPrice)
                .build();
        }

        @Test
        public void orderIsSet() {
            assertThat(setOpenPriceCommand.order(), equalTo(buyOrderEURUSD));
        }

        @Test
        public void noInteractionsHappensAtCreation() {
            verifyZeroInteractions(callableMock);
            verifyZeroInteractions(orderUtilCompletableMock);
        }

        @Test
        public void onSubscribeCloseIsInvokedOnOrderUtilCompletable() {
            when(orderUtilCompletableMock.setOpenPrice(setOpenPriceCommand))
                .thenReturn(emptyCompletable());

            setOpenPriceCommand
                .completable()
                .subscribe();

            verify(orderUtilCompletableMock).setOpenPrice(setOpenPriceCommand);
        }
    }

    public class SLBuilderTests {

        private SetSLCommand setSLCommand;
        private final double newSL = 1L;

        @Before
        public void setUp() {
            setSLCommand = orerUtilBuilder
                .setSLBuilder(buyOrderEURUSD, newSL)
                .build();
        }

        @Test
        public void orderIsSet() {
            assertThat(setSLCommand.order(), equalTo(buyOrderEURUSD));
        }

        @Test
        public void noInteractionsHappensAtCreation() {
            verifyZeroInteractions(callableMock);
            verifyZeroInteractions(orderUtilCompletableMock);
        }

        @Test
        public void onSubscribeCloseIsInvokedOnOrderUtilCompletable() {
            when(orderUtilCompletableMock.setSL(setSLCommand))
                .thenReturn(emptyCompletable());

            setSLCommand
                .completable()
                .subscribe();

            verify(orderUtilCompletableMock).setSL(setSLCommand);
        }
    }

    public class TPBuilderTests {

        private SetTPCommand setTPCommand;
        private final double newTP = 1L;

        @Before
        public void setUp() {
            setTPCommand = orerUtilBuilder
                .setTPBuilder(buyOrderEURUSD, newTP)
                .build();
        }

        @Test
        public void orderIsSet() {
            assertThat(setTPCommand.order(), equalTo(buyOrderEURUSD));
        }

        @Test
        public void noInteractionsHappensAtCreation() {
            verifyZeroInteractions(callableMock);
            verifyZeroInteractions(orderUtilCompletableMock);
        }

        @Test
        public void onSubscribeCloseIsInvokedOnOrderUtilCompletable() {
            when(orderUtilCompletableMock.setTP(setTPCommand))
                .thenReturn(emptyCompletable());

            setTPCommand
                .completable()
                .subscribe();

            verify(orderUtilCompletableMock).setTP(setTPCommand);
        }
    }
}
