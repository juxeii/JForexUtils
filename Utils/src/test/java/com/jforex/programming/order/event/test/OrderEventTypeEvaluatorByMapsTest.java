package com.jforex.programming.order.event.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.Optional;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Sets;
import com.jforex.programming.order.call.OrderCallRequest;
import com.jforex.programming.order.event.OrderEventType;
import com.jforex.programming.order.event.OrderMessageData;
import com.jforex.programming.order.event.OrderEventTypeEvaluatorByMaps;
import com.jforex.programming.test.common.CommonUtilForTest;
import com.jforex.programming.test.fakes.IMessageForTest;
import com.jforex.programming.test.fakes.IOrderForTest;

import com.dukascopy.api.IMessage;
import com.dukascopy.api.IOrder;
import com.dukascopy.api.JFException;

public class OrderEventTypeEvaluatorByMapsTest extends CommonUtilForTest {

    private OrderEventTypeEvaluatorByMaps eventTypeEvaluator;

    private final IOrderForTest orderUnderTest = IOrderForTest.buyOrderEURUSD();

    @Before
    public void setUp() {
        initCommonTestFramework();

        eventTypeEvaluator = new OrderEventTypeEvaluatorByMaps();
    }

    private OrderMessageData orderMessageData(final IMessage.Type messageType,
                                              final IMessage.Reason... messageReasons) {
        final IMessageForTest message =
                new IMessageForTest(orderUnderTest, messageType, Sets.newHashSet(messageReasons));
        return new OrderMessageData(message);
    }

    private void assertCorrectEventTypeMapping(final OrderEventType expectedType,
                                               final IMessage.Type messageType,
                                               final IMessage.Reason... messageReasons) {
        assertCorrectMapping(expectedType, messageType, Optional.empty(), messageReasons);
    }

    private void assertCorrectEventTypeMappingWithCallRequest(final OrderEventType expectedType,
                                                              final IMessage.Type messageType,
                                                              final OrderCallRequest orderCallRequest,
                                                              final IMessage.Reason... messageReasons) {
        assertCorrectMapping(expectedType, messageType, Optional.of(orderCallRequest), messageReasons);
    }

    private void assertCorrectMapping(final OrderEventType expectedType,
                                      final IMessage.Type messageType,
                                      final Optional<OrderCallRequest> orderCallRequestOpt,
                                      final IMessage.Reason... messageReasons) {
        final OrderMessageData orderMessageData = orderMessageData(messageType, messageReasons);

        final OrderEventType actualType = eventTypeEvaluator.get(orderMessageData, orderCallRequestOpt);

        assertThat(actualType, equalTo(expectedType));
    }

    @Test
    public void testNotificationIsMappedCorrect() {
        assertCorrectEventTypeMapping(OrderEventType.NOTIFICATION,
                                      IMessage.Type.NOTIFICATION);
    }

    @Test
    public void testSubmitRejectedIsMappedCorrect() {
        assertCorrectEventTypeMapping(OrderEventType.SUBMIT_REJECTED,
                                      IMessage.Type.ORDER_SUBMIT_REJECTED);
    }

    @Test
    public void testSubmitOKIsMappedCorrect() {
        assertCorrectEventTypeMapping(OrderEventType.SUBMIT_OK,
                                      IMessage.Type.ORDER_SUBMIT_OK);
    }

    @Test
    public void testFillRejectedIsMappedCorrect() {
        assertCorrectEventTypeMapping(OrderEventType.FILL_REJECTED,
                                      IMessage.Type.ORDER_FILL_REJECTED);
    }

    @Test
    public void testPartialFillIsMappedCorrect() throws JFException {
        orderUnderTest.setAmount(0.1);
        orderUnderTest.setRequestedAmount(0.2);
        assertCorrectEventTypeMapping(OrderEventType.PARTIAL_FILL_OK,
                                      IMessage.Type.ORDER_FILL_OK);
        assertCorrectEventTypeMapping(OrderEventType.PARTIAL_FILL_OK,
                                      IMessage.Type.ORDER_CHANGED_OK);
    }

    @Test
    public void testChangeRejectIsMappedCorrect() {
        assertCorrectEventTypeMapping(OrderEventType.CHANGE_REJECTED,
                                      IMessage.Type.ORDER_CHANGED_REJECTED);
    }

    @Test
    public void testCloseOKIsMappedCorrect() {
        assertCorrectEventTypeMapping(OrderEventType.CLOSE_OK,
                                      IMessage.Type.ORDER_CLOSE_OK);
    }

    @Test
    public void testCloseRejectedIsMappedCorrect() {
        assertCorrectEventTypeMapping(OrderEventType.CLOSE_REJECTED,
                                      IMessage.Type.ORDER_CLOSE_REJECTED);
    }

    @Test
    public void testMergeOKIsMappedCorrect() {
        assertCorrectEventTypeMapping(OrderEventType.MERGE_OK,
                                      IMessage.Type.ORDERS_MERGE_OK);
    }

    @Test
    public void testMergeRejectIsMappedCorrect() {
        assertCorrectEventTypeMapping(OrderEventType.MERGE_REJECTED,
                                      IMessage.Type.ORDERS_MERGE_REJECTED);
    }

    @Test
    public void testFullFillIsMappedCorrect() {
        assertCorrectEventTypeMapping(OrderEventType.FULL_FILL_OK,
                                      IMessage.Type.ORDER_FILL_OK,
                                      IMessage.Reason.ORDER_FULLY_FILLED);
    }

    @Test
    public void testCloseByMergeIsMappedCorrect() {
        assertCorrectEventTypeMapping(OrderEventType.CLOSED_BY_MERGE,
                                      IMessage.Type.ORDER_CLOSE_OK,
                                      IMessage.Reason.ORDER_CLOSED_BY_MERGE);
    }

    @Test
    public void testCloseBySLIsMappedCorrect() {
        assertCorrectEventTypeMapping(OrderEventType.CLOSED_BY_SL,
                                      IMessage.Type.ORDER_CLOSE_OK,
                                      IMessage.Reason.ORDER_CLOSED_BY_SL);
    }

    @Test
    public void testCloseByTPIsMappedCorrect() {
        assertCorrectEventTypeMapping(OrderEventType.CLOSED_BY_TP,
                                      IMessage.Type.ORDER_CLOSE_OK,
                                      IMessage.Reason.ORDER_CLOSED_BY_TP);
    }

    @Test
    public void testSLChangeOKIsMappedCorrect() {
        assertCorrectEventTypeMapping(OrderEventType.SL_CHANGE_OK,
                                      IMessage.Type.ORDER_CHANGED_OK,
                                      IMessage.Reason.ORDER_CHANGED_SL);
    }

    @Test
    public void testTPChangeOKIsMappedCorrect() {
        assertCorrectEventTypeMapping(OrderEventType.TP_CHANGE_OK,
                                      IMessage.Type.ORDER_CHANGED_OK,
                                      IMessage.Reason.ORDER_CHANGED_TP);
    }

    @Test
    public void testLabelChangeOKIsMappedCorrect() {
        assertCorrectEventTypeMapping(OrderEventType.LABEL_CHANGE_OK,
                                      IMessage.Type.ORDER_CHANGED_OK,
                                      IMessage.Reason.ORDER_CHANGED_LABEL);
    }

    @Test
    public void testAmountChangeOKIsMappedCorrect() {
        assertCorrectEventTypeMapping(OrderEventType.AMOUNT_CHANGE_OK,
                                      IMessage.Type.ORDER_CHANGED_OK,
                                      IMessage.Reason.ORDER_CHANGED_AMOUNT);
    }

    @Test
    public void testGTTChangeOKIsMappedCorrect() {
        assertCorrectEventTypeMapping(OrderEventType.GTT_CHANGE_OK,
                                      IMessage.Type.ORDER_CHANGED_OK,
                                      IMessage.Reason.ORDER_CHANGED_GTT);
    }

    @Test
    public void testPriceChangeOKIsMappedCorrect() {
        assertCorrectEventTypeMapping(OrderEventType.PRICE_CHANGE_OK,
                                      IMessage.Type.ORDER_CHANGED_OK,
                                      IMessage.Reason.ORDER_CHANGED_PRICE);
    }

    @Test
    public void testPartialCloseOKIsMappedCorrect() {
        orderUnderTest.setState(IOrder.State.FILLED);
        assertCorrectEventTypeMapping(OrderEventType.PARTIAL_CLOSE_OK,
                                      IMessage.Type.ORDER_CLOSE_OK);
    }

    @Test
    public void testLabelChangeRejectIsMappedCorrect() {
        assertCorrectEventTypeMappingWithCallRequest(OrderEventType.CHANGE_LABEL_REJECTED,
                                                     IMessage.Type.ORDER_CHANGED_REJECTED,
                                                     OrderCallRequest.CHANGE_LABEL);
    }

//
    @Test
    public void testGTTChangeRejectIsMappedCorrect() {
        assertCorrectEventTypeMappingWithCallRequest(OrderEventType.CHANGE_GTT_REJECTED,
                                                     IMessage.Type.ORDER_CHANGED_REJECTED,
                                                     OrderCallRequest.CHANGE_GTT);
    }

    @Test
    public void testAmountChangeRejectIsMappedCorrect() {
        assertCorrectEventTypeMappingWithCallRequest(OrderEventType.CHANGE_AMOUNT_REJECTED,
                                                     IMessage.Type.ORDER_CHANGED_REJECTED,
                                                     OrderCallRequest.CHANGE_AMOUNT);
    }

    @Test
    public void testOpenPriceChangeRejectIsMappedCorrect() {
        assertCorrectEventTypeMappingWithCallRequest(OrderEventType.CHANGE_OPENPRICE_REJECTED,
                                                     IMessage.Type.ORDER_CHANGED_REJECTED,
                                                     OrderCallRequest.CHANGE_OPENPRICE);
    }

    @Test
    public void testSLChangeRejectIsMappedCorrect() {
        assertCorrectEventTypeMappingWithCallRequest(OrderEventType.CHANGE_SL_REJECTED,
                                                     IMessage.Type.ORDER_CHANGED_REJECTED,
                                                     OrderCallRequest.CHANGE_SL);
    }

    @Test
    public void testTPChangeRejectIsMappedCorrect() {
        assertCorrectEventTypeMappingWithCallRequest(OrderEventType.CHANGE_TP_REJECTED,
                                                     IMessage.Type.ORDER_CHANGED_REJECTED,
                                                     OrderCallRequest.CHANGE_TP);
    }
}
