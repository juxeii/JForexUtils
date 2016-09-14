package com.jforex.programming.order.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.Set;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import com.dukascopy.api.IOrder;
import com.google.common.collect.Sets;
import com.jforex.programming.misc.IEngineUtil;
import com.jforex.programming.order.OrderUtilBuilder;
import com.jforex.programming.order.call.OrderCallReason;
import com.jforex.programming.order.command.ChangeCommand;
import com.jforex.programming.order.command.MergeCommand;
import com.jforex.programming.order.command.SubmitCommand;
import com.jforex.programming.order.event.OrderEventType;
import com.jforex.programming.test.common.InstrumentUtilForTest;

import de.bechte.junit.runners.context.HierarchicalContextRunner;

@RunWith(HierarchicalContextRunner.class)
public class OrderUtilBuilderTest extends InstrumentUtilForTest {

    private OrderUtilBuilder orderUtilBuilder;

    @Mock
    private IEngineUtil engineUtilMock;
    @Mock
    private Callable<IOrder> callableMock;
    @Mock
    private Consumer<IOrder> changeConsumer;
    @Mock
    private Consumer<IOrder> rejectConsumer;
    private ChangeCommand changeCommand;

    @Before
    public void setUp() {
        orderUtilBuilder = new OrderUtilBuilder(engineUtilMock);
    }

    private void verifyChangeConsumer(final ChangeCommand changeCommand,
                                      final OrderEventType orderEventType) {
        verifyConsumer(changeConsumer,
                       changeCommand,
                       orderEventType);
    }

    private void verifyRejectConsumer(final ChangeCommand changeCommand,
                                      final OrderEventType orderEventType) {
        verifyConsumer(rejectConsumer,
                       changeCommand,
                       orderEventType);
    }

    private void verifyConsumer(final Consumer<IOrder> consumer,
                                final ChangeCommand changeCommand,
                                final OrderEventType orderEventType) {
        changeCommand
            .eventHandlerForType()
            .get(orderEventType)
            .accept(buyOrderEURUSD);

        verify(consumer).accept(buyOrderEURUSD);
    }

    public class SubmitBuilderTests {

        private SubmitCommand submitCommand;

        @Before
        public void setUp() {
            when(engineUtilMock.submitCallable(buyParamsEURUSD))
                .thenReturn(callableMock);

            submitCommand = orderUtilBuilder
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

            mergeCommand = orderUtilBuilder
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
        orderUtilBuilder
            .closeBuilder(buyOrderEURUSD)
            .build();

        verifyZeroInteractions(callableMock);
    }

    public class SetLabelBuilder {

        private final String newGTT = "newGTT";

        @Before
        public void setUp() {
            changeCommand = orderUtilBuilder
                .setLabelBuilder(buyOrderEURUSD, newGTT)
                .doOnChange(changeConsumer)
                .doOnReject(rejectConsumer)
                .build();
        }

        @Test
        public void callableIsCorrect() throws Exception {
            changeCommand.callable().call();

            verify(buyOrderEURUSD).setLabel(newGTT);
        }

        @Test
        public void callReasonIsCorrect() {
            assertThat(changeCommand.callReason(), equalTo(OrderCallReason.CHANGE_LABEL));
        }

        @Test
        public void changeConsumerIsCorrect() {
            verifyChangeConsumer(changeCommand, OrderEventType.CHANGED_LABEL);
        }

        @Test
        public void rejectConsumerIsCorrect() {
            verifyRejectConsumer(changeCommand, OrderEventType.CHANGE_LABEL_REJECTED);
        }

        @Test
        public void noInteractionsHappensAtCreation() {
            verifyZeroInteractions(callableMock);
        }
    }

    public class SetGTTBuilder {

        private final long newGTT = 1L;

        @Before
        public void setUp() {
            changeCommand = orderUtilBuilder
                .setGTTBuilder(buyOrderEURUSD, newGTT)
                .doOnChange(changeConsumer)
                .doOnReject(rejectConsumer)
                .build();
        }

        @Test
        public void callableIsCorrect() throws Exception {
            changeCommand.callable().call();

            verify(buyOrderEURUSD).setGoodTillTime(newGTT);
        }

        @Test
        public void callReasonIsCorrect() {
            assertThat(changeCommand.callReason(), equalTo(OrderCallReason.CHANGE_GTT));
        }

        @Test
        public void changeConsumerIsCorrect() {
            verifyChangeConsumer(changeCommand, OrderEventType.CHANGED_GTT);
        }

        @Test
        public void rejectConsumerIsCorrect() {
            verifyRejectConsumer(changeCommand, OrderEventType.CHANGE_GTT_REJECTED);
        }

        @Test
        public void noInteractionsHappensAtCreation() {
            verifyZeroInteractions(callableMock);
        }
    }

    public class SetAmountBuilder {

        private final double newAmount = 0.12;

        @Before
        public void setUp() {
            changeCommand = orderUtilBuilder
                .setAmountBuilder(buyOrderEURUSD, newAmount)
                .doOnChange(changeConsumer)
                .doOnReject(rejectConsumer)
                .build();
        }

        @Test
        public void callableIsCorrect() throws Exception {
            changeCommand.callable().call();

            verify(buyOrderEURUSD).setRequestedAmount(newAmount);
        }

        @Test
        public void callReasonIsCorrect() {
            assertThat(changeCommand.callReason(), equalTo(OrderCallReason.CHANGE_AMOUNT));
        }

        @Test
        public void changeConsumerIsCorrect() {
            verifyChangeConsumer(changeCommand, OrderEventType.CHANGED_AMOUNT);
        }

        @Test
        public void rejectConsumerIsCorrect() {
            verifyRejectConsumer(changeCommand, OrderEventType.CHANGE_AMOUNT_REJECTED);
        }

        @Test
        public void noInteractionsHappensAtCreation() {
            verifyZeroInteractions(callableMock);
        }
    }

    public class SetOpenPriceBuilder {

        private final double newOpenPrice = 1.1234;

        @Before
        public void setUp() {
            changeCommand = orderUtilBuilder
                .setOpenPriceBuilder(buyOrderEURUSD, newOpenPrice)
                .doOnChange(changeConsumer)
                .doOnReject(rejectConsumer)
                .build();
        }

        @Test
        public void callableIsCorrect() throws Exception {
            changeCommand.callable().call();

            verify(buyOrderEURUSD).setOpenPrice(newOpenPrice);
        }

        @Test
        public void callReasonIsCorrect() {
            assertThat(changeCommand.callReason(), equalTo(OrderCallReason.CHANGE_PRICE));
        }

        @Test
        public void changeConsumerIsCorrect() {
            verifyChangeConsumer(changeCommand, OrderEventType.CHANGED_PRICE);
        }

        @Test
        public void rejectConsumerIsCorrect() {
            verifyRejectConsumer(changeCommand, OrderEventType.CHANGE_PRICE_REJECTED);
        }

        @Test
        public void noInteractionsHappensAtCreation() {
            verifyZeroInteractions(callableMock);
        }
    }

    public class SetSLBuilder {

        private final double newSL = 1.1234;

        @Before
        public void setUp() {
            changeCommand = orderUtilBuilder
                .setSLBuilder(buyOrderEURUSD, newSL)
                .doOnChange(changeConsumer)
                .doOnReject(rejectConsumer)
                .build();
        }

        @Test
        public void callableIsCorrect() throws Exception {
            changeCommand.callable().call();

            verify(buyOrderEURUSD).setStopLossPrice(newSL);
        }

        @Test
        public void callReasonIsCorrect() {
            assertThat(changeCommand.callReason(), equalTo(OrderCallReason.CHANGE_SL));
        }

        @Test
        public void changeConsumerIsCorrect() {
            verifyChangeConsumer(changeCommand, OrderEventType.CHANGED_SL);
        }

        @Test
        public void rejectConsumerIsCorrect() {
            verifyRejectConsumer(changeCommand, OrderEventType.CHANGE_SL_REJECTED);
        }

        @Test
        public void noInteractionsHappensAtCreation() {
            verifyZeroInteractions(callableMock);
        }
    }

    public class SetTPBuilder {

        private final double newTP = 1.1234;

        @Before
        public void setUp() {
            changeCommand = orderUtilBuilder
                .setTPBuilder(buyOrderEURUSD, newTP)
                .doOnChange(changeConsumer)
                .doOnReject(rejectConsumer)
                .build();
        }

        @Test
        public void callableIsCorrect() throws Exception {
            changeCommand.callable().call();

            verify(buyOrderEURUSD).setTakeProfitPrice(newTP);
        }

        @Test
        public void callReasonIsCorrect() {
            assertThat(changeCommand.callReason(), equalTo(OrderCallReason.CHANGE_TP));
        }

        @Test
        public void changeConsumerIsCorrect() {
            verifyChangeConsumer(changeCommand, OrderEventType.CHANGED_TP);
        }

        @Test
        public void rejectConsumerIsCorrect() {
            verifyRejectConsumer(changeCommand, OrderEventType.CHANGE_TP_REJECTED);
        }

        @Test
        public void noInteractionsHappensAtCreation() {
            verifyZeroInteractions(callableMock);
        }
    }
}
