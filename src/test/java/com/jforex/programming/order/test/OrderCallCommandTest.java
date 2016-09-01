package com.jforex.programming.order.test;

import static com.jforex.programming.order.event.OrderEventType.CHANGED_AMOUNT;
import static com.jforex.programming.order.event.OrderEventType.CHANGED_GTT;
import static com.jforex.programming.order.event.OrderEventType.CHANGED_LABEL;
import static com.jforex.programming.order.event.OrderEventType.CHANGED_PRICE;
import static com.jforex.programming.order.event.OrderEventType.CHANGED_SL;
import static com.jforex.programming.order.event.OrderEventType.CHANGED_TP;
import static com.jforex.programming.order.event.OrderEventType.CHANGE_AMOUNT_REJECTED;
import static com.jforex.programming.order.event.OrderEventType.CHANGE_GTT_REJECTED;
import static com.jforex.programming.order.event.OrderEventType.CHANGE_LABEL_REJECTED;
import static com.jforex.programming.order.event.OrderEventType.CHANGE_PRICE_REJECTED;
import static com.jforex.programming.order.event.OrderEventType.CHANGE_SL_REJECTED;
import static com.jforex.programming.order.event.OrderEventType.CHANGE_TP_REJECTED;
import static com.jforex.programming.order.event.OrderEventType.CLOSE_OK;
import static com.jforex.programming.order.event.OrderEventType.CLOSE_REJECTED;
import static com.jforex.programming.order.event.OrderEventType.FILL_REJECTED;
import static com.jforex.programming.order.event.OrderEventType.FULLY_FILLED;
import static com.jforex.programming.order.event.OrderEventType.MERGE_CLOSE_OK;
import static com.jforex.programming.order.event.OrderEventType.MERGE_OK;
import static com.jforex.programming.order.event.OrderEventType.MERGE_REJECTED;
import static com.jforex.programming.order.event.OrderEventType.NOTIFICATION;
import static com.jforex.programming.order.event.OrderEventType.PARTIAL_CLOSE_OK;
import static com.jforex.programming.order.event.OrderEventType.PARTIAL_FILL_OK;
import static com.jforex.programming.order.event.OrderEventType.SUBMIT_CONDITIONAL_OK;
import static com.jforex.programming.order.event.OrderEventType.SUBMIT_OK;
import static com.jforex.programming.order.event.OrderEventType.SUBMIT_REJECTED;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.function.Function;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.call.OrderCallCommand;
import com.jforex.programming.order.call.OrderCallReason;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.event.OrderEventType;
import com.jforex.programming.test.common.CommonUtilForTest;

import de.bechte.junit.runners.context.HierarchicalContextRunner;

@RunWith(HierarchicalContextRunner.class)
public class OrderCallCommandTest extends CommonUtilForTest {

    private OrderCallCommand orderCallCommand;

    private final IOrder orderForTest = orderUtilForTest.buyOrderEURUSD();
    private final Callable<IOrder> callableForTest = () -> {
        orderForTest.close();
        return orderForTest;
    };

    private void initOrderCallCommand(final OrderCallReason callReason) {
        orderCallCommand = new OrderCallCommand(callableForTest, callReason);
    }

    private void assertCallReason(final OrderCallReason orderCallReason) {
        assertThat(orderCallCommand.callReason(), equalTo(orderCallReason));
    }

    private void assertEventsForCommand(final OrderEventType... orderEventTypes) {
        assertEvents(orderCallCommand::isEventForCommand, orderEventTypes);
    }

    private void assertDoneEvents(final OrderEventType... orderEventTypes) {
        assertEvents(orderCallCommand::isDoneEvent, orderEventTypes);
    }

    private void assertEvents(final Function<OrderEvent, Boolean> testFunction,
                              final OrderEventType... orderEventTypes) {
        new ArrayList<>(Arrays.asList(orderEventTypes))
            .forEach(type -> {
                final OrderEvent orderEvent = new OrderEvent(orderForTest, type);
                assertTrue(testFunction.apply(orderEvent));
            });
    }

    @Test
    public void returnedCallableIsCorrect() throws Exception {
        initOrderCallCommand(OrderCallReason.CLOSE);

        final Callable<IOrder> callable = orderCallCommand.callable();
        final IOrder order = callable.call();

        assertThat(order, equalTo(orderForTest));
        verify(orderForTest).close();
    }

    public class SubmitReason {

        @Before
        public void setUp() {
            initOrderCallCommand(OrderCallReason.SUBMIT);
        }

        @Test
        public void orderCallReasonIsCorrect() {
            assertCallReason(OrderCallReason.SUBMIT);
        }

        @Test
        public void orderEventTypesAreCorrect() {
            assertDoneEvents(FULLY_FILLED,
                             SUBMIT_CONDITIONAL_OK);

            assertEventsForCommand(NOTIFICATION,
                                   FULLY_FILLED,
                                   SUBMIT_CONDITIONAL_OK,
                                   FILL_REJECTED,
                                   SUBMIT_REJECTED,
                                   SUBMIT_OK,
                                   PARTIAL_FILL_OK);
        }
    }

    public class MergeReason {

        @Before
        public void setUp() {
            initOrderCallCommand(OrderCallReason.MERGE);
        }

        @Test
        public void orderCallReasonIsCorrect() {
            assertCallReason(OrderCallReason.MERGE);
        }

        @Test
        public void orderEventTypesAreCorrect() {
            assertDoneEvents(MERGE_OK,
                             MERGE_CLOSE_OK);

            assertEventsForCommand(MERGE_OK,
                                   NOTIFICATION,
                                   MERGE_CLOSE_OK,
                                   MERGE_REJECTED);
        }
    }

    public class CloseReason {

        @Before
        public void setUp() {
            initOrderCallCommand(OrderCallReason.CLOSE);
        }

        @Test
        public void orderCallReasonIsCorrect() {
            assertCallReason(OrderCallReason.CLOSE);
        }

        @Test
        public void orderEventTypesAreCorrect() {
            assertDoneEvents(CLOSE_OK);

            assertEventsForCommand(NOTIFICATION,
                                   CLOSE_OK,
                                   CLOSE_REJECTED,
                                   PARTIAL_CLOSE_OK,
                                   CLOSE_REJECTED);
        }
    }

    public class ChangeLabelReason {

        @Before
        public void setUp() {
            initOrderCallCommand(OrderCallReason.CHANGE_LABEL);
        }

        @Test
        public void orderCallReasonIsCorrect() {
            assertCallReason(OrderCallReason.CHANGE_LABEL);
        }

        @Test
        public void orderEventTypesAreCorrect() {
            assertDoneEvents(CHANGED_LABEL);

            assertEventsForCommand(NOTIFICATION,
                                   CHANGED_LABEL,
                                   CHANGE_LABEL_REJECTED);
        }
    }

    public class ChangeGTTReason {

        @Before
        public void setUp() {
            initOrderCallCommand(OrderCallReason.CHANGE_GTT);
        }

        @Test
        public void orderCallReasonIsCorrect() {
            assertCallReason(OrderCallReason.CHANGE_GTT);
        }

        @Test
        public void orderEventTypesAreCorrect() {
            assertDoneEvents(CHANGED_GTT);

            assertEventsForCommand(NOTIFICATION,
                                   CHANGED_GTT,
                                   CHANGE_GTT_REJECTED);
        }
    }

    public class ChangeOpenPriceReason {

        @Before
        public void setUp() {
            initOrderCallCommand(OrderCallReason.CHANGE_PRICE);
        }

        @Test
        public void orderCallReasonIsCorrect() {
            assertCallReason(OrderCallReason.CHANGE_PRICE);
        }

        @Test
        public void orderEventTypesAreCorrect() {
            assertDoneEvents(CHANGED_PRICE);

            assertEventsForCommand(NOTIFICATION,
                                   CHANGED_PRICE,
                                   CHANGE_PRICE_REJECTED);
        }
    }

    public class ChangeAmountReason {

        @Before
        public void setUp() {
            initOrderCallCommand(OrderCallReason.CHANGE_AMOUNT);
        }

        @Test
        public void orderCallReasonIsCorrect() {
            assertCallReason(OrderCallReason.CHANGE_AMOUNT);
        }

        @Test
        public void orderEventTypesAreCorrect() {
            assertDoneEvents(CHANGED_AMOUNT);

            assertEventsForCommand(NOTIFICATION,
                                   CHANGED_AMOUNT,
                                   CHANGE_AMOUNT_REJECTED);
        }
    }

    public class ChangeSLReason {

        @Before
        public void setUp() {
            initOrderCallCommand(OrderCallReason.CHANGE_SL);
        }

        @Test
        public void orderCallReasonIsCorrect() {
            assertCallReason(OrderCallReason.CHANGE_SL);
        }

        @Test
        public void orderEventTypesAreCorrect() {
            assertDoneEvents(CHANGED_SL);

            assertEventsForCommand(NOTIFICATION,
                                   CHANGED_SL,
                                   CHANGE_SL_REJECTED);
        }
    }

    public class ChangeTPReason {

        @Before
        public void setUp() {
            initOrderCallCommand(OrderCallReason.CHANGE_TP);
        }

        @Test
        public void orderCallReasonIsCorrect() {
            assertCallReason(OrderCallReason.CHANGE_TP);
        }

        @Test
        public void orderEventTypesAreCorrect() {
            assertDoneEvents(CHANGED_TP);

            assertEventsForCommand(NOTIFICATION,
                                   CHANGED_TP,
                                   CHANGE_TP_REJECTED);
        }
    }
}
