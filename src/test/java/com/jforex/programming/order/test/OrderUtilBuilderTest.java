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
import com.jforex.programming.order.command.Command;
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
    private Command command;

    @Before
    public void setUp() {
        orderUtilBuilder = new OrderUtilBuilder(engineUtilMock);
    }

    private void verifyChangeConsumer(final Command command,
                                      final OrderEventType orderEventType) {
        verifyConsumer(changeConsumer,
                       command,
                       orderEventType);
    }

    private void verifyRejectConsumer(final Command command,
                                      final OrderEventType orderEventType) {
        verifyConsumer(rejectConsumer,
                       command,
                       orderEventType);
    }

    private void verifyConsumer(final Consumer<IOrder> consumer,
                                final Command command,
                                final OrderEventType orderEventType) {
        command
            .eventHandlerForType()
            .get(orderEventType)
            .accept(buyOrderEURUSD);

        verify(consumer).accept(buyOrderEURUSD);
    }

    public class SubmitBuilderTests {

        @Before
        public void setUp() {
            when(engineUtilMock.submitCallable(buyParamsEURUSD))
                .thenReturn(callableMock);

            command = orderUtilBuilder
                .submitBuilder(buyParamsEURUSD)
                .doOnSubmit(changeConsumer)
                .doOnPartialFill(changeConsumer)
                .doOnFill(changeConsumer)
                .doOnFillReject(rejectConsumer)
                .doOnSubmitReject(rejectConsumer)
                .build();
        }

        @Test
        public void callableIsSet() {
            assertThat(command.callable(), equalTo(callableMock));
        }

        @Test
        public void changeConsumerForSubmitIsCorrect() {
            verifyChangeConsumer(command, OrderEventType.SUBMIT_OK);
        }

        @Test
        public void changeConsumerForConditionalSubmitIsCorrect() {
            verifyChangeConsumer(command, OrderEventType.SUBMIT_CONDITIONAL_OK);
        }

        @Test
        public void changeConsumerForPartialFillIsCorrect() {
            verifyChangeConsumer(command, OrderEventType.PARTIAL_FILL_OK);
        }

        @Test
        public void changeConsumerForFillIsCorrect() {
            verifyChangeConsumer(command, OrderEventType.FULLY_FILLED);
        }

        @Test
        public void rejectConsumerForSubmitIsCorrect() {
            verifyRejectConsumer(command, OrderEventType.SUBMIT_REJECTED);
        }

        @Test
        public void rejectConsumerForFillIsCorrect() {
            verifyRejectConsumer(command, OrderEventType.FILL_REJECTED);
        }

        @Test
        public void noInteractionsHappensAtCreation() {
            verifyZeroInteractions(callableMock);
        }
    }

    public class MergeBuilderTests {

        private final String mergeOrderLabel = "mergeOrderLabel";
        private final Set<IOrder> toMergeOrders = Sets.newHashSet(buyOrderEURUSD, sellOrderEURUSD);

        @Before
        public void setUp() {
            when(engineUtilMock.mergeCallable(mergeOrderLabel, toMergeOrders))
                .thenReturn(callableMock);

            command = orderUtilBuilder
                .mergeBuilder(mergeOrderLabel, toMergeOrders)
                .doOnMerge(changeConsumer)
                .doOnMergeReject(rejectConsumer)
                .doOnMergeClose(changeConsumer)
                .build();
        }

        @Test
        public void callableIsSet() {
            assertThat(command.callable(), equalTo(callableMock));
        }

        @Test
        public void changeConsumerIsCorrect() {
            verifyChangeConsumer(command, OrderEventType.MERGE_OK);
        }

        @Test
        public void rejectConsumerIsCorrect() {
            verifyRejectConsumer(command, OrderEventType.MERGE_REJECTED);
        }

        @Test
        public void changeConsumerForMergeCloseOKIsCorrect() {
            verifyChangeConsumer(command, OrderEventType.MERGE_CLOSE_OK);
        }

        @Test
        public void noInteractionsHappensAtCreation() {
            verifyZeroInteractions(callableMock);
        }
    }

    public class CloseBuilderTests {

        @Before
        public void setUp() {
            command = orderUtilBuilder
                .closeBuilder(buyOrderEURUSD)
                .doOnClose(changeConsumer)
                .doOnPartialClose(changeConsumer)
                .doOnCloseReject(rejectConsumer)
                .build();
        }

        @Test
        public void callableIsCorrect() throws Exception {
            command.callable().call();

            verify(buyOrderEURUSD).close();
        }

        @Test
        public void callReasonIsCorrect() {
            assertThat(command.callReason(), equalTo(OrderCallReason.CLOSE));
        }

        @Test
        public void changeConsumerIsCorrect() {
            verifyChangeConsumer(command, OrderEventType.CLOSE_OK);
        }

        @Test
        public void changeConsumerForPartialCloseIsCorrect() {
            verifyChangeConsumer(command, OrderEventType.PARTIAL_CLOSE_OK);
        }

        @Test
        public void rejectConsumerIsCorrect() {
            verifyRejectConsumer(command, OrderEventType.CLOSE_REJECTED);
        }

        @Test
        public void noInteractionsHappensAtCreation() {
            verifyZeroInteractions(callableMock);
        }
    }

    public class SetLabelBuilder {

        private final String newGTT = "newGTT";

        @Before
        public void setUp() {
            command = orderUtilBuilder
                .setLabelBuilder(buyOrderEURUSD, newGTT)
                .doOnChange(changeConsumer)
                .doOnReject(rejectConsumer)
                .build();
        }

        @Test
        public void callableIsCorrect() throws Exception {
            command.callable().call();

            verify(buyOrderEURUSD).setLabel(newGTT);
        }

        @Test
        public void callReasonIsCorrect() {
            assertThat(command.callReason(), equalTo(OrderCallReason.CHANGE_LABEL));
        }

        @Test
        public void changeConsumerIsCorrect() {
            verifyChangeConsumer(command, OrderEventType.CHANGED_LABEL);
        }

        @Test
        public void rejectConsumerIsCorrect() {
            verifyRejectConsumer(command, OrderEventType.CHANGE_LABEL_REJECTED);
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
            command = orderUtilBuilder
                .setGTTBuilder(buyOrderEURUSD, newGTT)
                .doOnChange(changeConsumer)
                .doOnReject(rejectConsumer)
                .build();
        }

        @Test
        public void callableIsCorrect() throws Exception {
            command.callable().call();

            verify(buyOrderEURUSD).setGoodTillTime(newGTT);
        }

        @Test
        public void callReasonIsCorrect() {
            assertThat(command.callReason(), equalTo(OrderCallReason.CHANGE_GTT));
        }

        @Test
        public void changeConsumerIsCorrect() {
            verifyChangeConsumer(command, OrderEventType.CHANGED_GTT);
        }

        @Test
        public void rejectConsumerIsCorrect() {
            verifyRejectConsumer(command, OrderEventType.CHANGE_GTT_REJECTED);
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
            command = orderUtilBuilder
                .setAmountBuilder(buyOrderEURUSD, newAmount)
                .doOnChange(changeConsumer)
                .doOnReject(rejectConsumer)
                .build();
        }

        @Test
        public void callableIsCorrect() throws Exception {
            command.callable().call();

            verify(buyOrderEURUSD).setRequestedAmount(newAmount);
        }

        @Test
        public void callReasonIsCorrect() {
            assertThat(command.callReason(), equalTo(OrderCallReason.CHANGE_AMOUNT));
        }

        @Test
        public void changeConsumerIsCorrect() {
            verifyChangeConsumer(command, OrderEventType.CHANGED_AMOUNT);
        }

        @Test
        public void rejectConsumerIsCorrect() {
            verifyRejectConsumer(command, OrderEventType.CHANGE_AMOUNT_REJECTED);
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
            command = orderUtilBuilder
                .setOpenPriceBuilder(buyOrderEURUSD, newOpenPrice)
                .doOnChange(changeConsumer)
                .doOnReject(rejectConsumer)
                .build();
        }

        @Test
        public void callableIsCorrect() throws Exception {
            command.callable().call();

            verify(buyOrderEURUSD).setOpenPrice(newOpenPrice);
        }

        @Test
        public void callReasonIsCorrect() {
            assertThat(command.callReason(), equalTo(OrderCallReason.CHANGE_PRICE));
        }

        @Test
        public void changeConsumerIsCorrect() {
            verifyChangeConsumer(command, OrderEventType.CHANGED_PRICE);
        }

        @Test
        public void rejectConsumerIsCorrect() {
            verifyRejectConsumer(command, OrderEventType.CHANGE_PRICE_REJECTED);
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
            command = orderUtilBuilder
                .setSLBuilder(buyOrderEURUSD, newSL)
                .doOnChange(changeConsumer)
                .doOnReject(rejectConsumer)
                .build();
        }

        @Test
        public void callableIsCorrect() throws Exception {
            command.callable().call();

            verify(buyOrderEURUSD).setStopLossPrice(newSL);
        }

        @Test
        public void callReasonIsCorrect() {
            assertThat(command.callReason(), equalTo(OrderCallReason.CHANGE_SL));
        }

        @Test
        public void changeConsumerIsCorrect() {
            verifyChangeConsumer(command, OrderEventType.CHANGED_SL);
        }

        @Test
        public void rejectConsumerIsCorrect() {
            verifyRejectConsumer(command, OrderEventType.CHANGE_SL_REJECTED);
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
            command = orderUtilBuilder
                .setTPBuilder(buyOrderEURUSD, newTP)
                .doOnChange(changeConsumer)
                .doOnReject(rejectConsumer)
                .build();
        }

        @Test
        public void callableIsCorrect() throws Exception {
            command.callable().call();

            verify(buyOrderEURUSD).setTakeProfitPrice(newTP);
        }

        @Test
        public void callReasonIsCorrect() {
            assertThat(command.callReason(), equalTo(OrderCallReason.CHANGE_TP));
        }

        @Test
        public void changeConsumerIsCorrect() {
            verifyChangeConsumer(command, OrderEventType.CHANGED_TP);
        }

        @Test
        public void rejectConsumerIsCorrect() {
            verifyRejectConsumer(command, OrderEventType.CHANGE_TP_REJECTED);
        }

        @Test
        public void noInteractionsHappensAtCreation() {
            verifyZeroInteractions(callableMock);
        }
    }
}
