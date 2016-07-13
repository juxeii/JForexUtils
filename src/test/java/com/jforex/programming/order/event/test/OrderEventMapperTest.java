package com.jforex.programming.order.event.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.dukascopy.api.IEngine.OrderCommand;
import com.dukascopy.api.IMessage;
import com.dukascopy.api.IOrder;
import com.google.common.collect.Sets;
import com.jforex.programming.order.OrderMessageData;
import com.jforex.programming.order.call.OrderCallReason;
import com.jforex.programming.order.call.OrderCallRequest;
import com.jforex.programming.order.event.ChangeEventMapper;
import com.jforex.programming.order.event.OrderEventMapper;
import com.jforex.programming.order.event.OrderEventType;
import com.jforex.programming.order.event.OrderEventTypeSets;
import com.jforex.programming.order.event.ReasonEventMapper;
import com.jforex.programming.order.event.TypeEventMapper;
import com.jforex.programming.test.common.CommonUtilForTest;
import com.jforex.programming.test.fakes.IMessageForTest;
import com.jforex.programming.test.fakes.IOrderForTest;

import de.bechte.junit.runners.context.HierarchicalContextRunner;

@RunWith(HierarchicalContextRunner.class)
public class OrderEventMapperTest extends CommonUtilForTest {

    private OrderEventMapper orderEventMapper;

    private final IOrderForTest orderUnderTest = IOrderForTest.buyOrderEURUSD();

    @Before
    public void setUp() {
        initCommonTestFramework();

        orderEventMapper = new OrderEventMapper();
    }

    private OrderMessageData orderMessageData(final IMessage.Type messageType,
                                              final IMessage.Reason... messageReasons) {
        final IMessageForTest message = new IMessageForTest(orderUnderTest,
                                                            messageType,
                                                            Sets.newHashSet(messageReasons));
        return new OrderMessageData(message);
    }

    private void assertCorrectMapping(final OrderEventType expectedType,
                                      final IMessage.Type messageType,
                                      final IMessage.Reason... messageReasons) {
        final OrderMessageData orderMessageData = orderMessageData(messageType, messageReasons);

        final OrderEventType actualType = orderEventMapper.get(orderMessageData);

        assertThat(actualType, equalTo(expectedType));
    }

    private void
            assertCorrectMappingForChangeRejectRefinement(final OrderCallReason orderCallReason,
                                                          final OrderEventType expectedType) {
        orderEventMapper
                .registerOrderCallRequest(new OrderCallRequest(orderUnderTest, orderCallReason));
        assertCorrectMapping(expectedType, IMessage.Type.ORDER_CHANGED_REJECTED);
    }

    private void registerCallRequest(final OrderCallReason orderCallReason) {
        orderEventMapper
                .registerOrderCallRequest(new OrderCallRequest(orderUnderTest, orderCallReason));
    }

    @Test
    public void helperMapperClassesHavePrivateConstructors() throws Exception {
        assertPrivateConstructor(OrderEventTypeSets.class);
        assertPrivateConstructor(ReasonEventMapper.class);
        assertPrivateConstructor(ChangeEventMapper.class);
        assertPrivateConstructor(TypeEventMapper.class);
    }

    @Test
    public void testFullFillIsMappedCorrect() {
        assertCorrectMapping(OrderEventType.FULLY_FILLED,
                             IMessage.Type.ORDER_FILL_OK);
    }

    @Test
    public void testCloseByMergeIsMappedCorrect() {
        assertCorrectMapping(OrderEventType.CLOSED_BY_MERGE,
                             IMessage.Type.ORDER_CLOSE_OK,
                             IMessage.Reason.ORDER_CLOSED_BY_MERGE);
    }

    @Test
    public void testCloseBySLIsMappedCorrect() {
        assertCorrectMapping(OrderEventType.CLOSED_BY_SL,
                             IMessage.Type.ORDER_CLOSE_OK,
                             IMessage.Reason.ORDER_CLOSED_BY_SL);
    }

    @Test
    public void testCloseByTPIsMappedCorrect() {
        assertCorrectMapping(OrderEventType.CLOSED_BY_TP,
                             IMessage.Type.ORDER_CLOSE_OK,
                             IMessage.Reason.ORDER_CLOSED_BY_TP);
    }

    @Test
    public void testSLChangeOKIsMappedCorrect() {
        assertCorrectMapping(OrderEventType.CHANGED_SL,
                             IMessage.Type.ORDER_CHANGED_OK,
                             IMessage.Reason.ORDER_CHANGED_SL);
    }

    @Test
    public void testTPChangeOKIsMappedCorrect() {
        assertCorrectMapping(OrderEventType.CHANGED_TP,
                             IMessage.Type.ORDER_CHANGED_OK,
                             IMessage.Reason.ORDER_CHANGED_TP);
    }

    @Test
    public void testLabelChangeOKIsMappedCorrect() {
        assertCorrectMapping(OrderEventType.CHANGED_LABEL,
                             IMessage.Type.ORDER_CHANGED_OK,
                             IMessage.Reason.ORDER_CHANGED_LABEL);
    }

    @Test
    public void testAmountChangeOKIsMappedCorrect() {
        assertCorrectMapping(OrderEventType.CHANGED_AMOUNT,
                             IMessage.Type.ORDER_CHANGED_OK,
                             IMessage.Reason.ORDER_CHANGED_AMOUNT);
    }

    @Test
    public void testGTTChangeOKIsMappedCorrect() {
        assertCorrectMapping(OrderEventType.CHANGED_GTT,
                             IMessage.Type.ORDER_CHANGED_OK,
                             IMessage.Reason.ORDER_CHANGED_GTT);
    }

    @Test
    public void testPriceChangeOKIsMappedCorrect() {
        assertCorrectMapping(OrderEventType.CHANGED_PRICE,
                             IMessage.Type.ORDER_CHANGED_OK,
                             IMessage.Reason.ORDER_CHANGED_PRICE);
    }

    @Test
    public void testNotificationIsMappedCorrect() {
        assertCorrectMapping(OrderEventType.NOTIFICATION,
                             IMessage.Type.NOTIFICATION);
    }

    @Test
    public void testSubmitRejectedIsMappedCorrect() {
        assertCorrectMapping(OrderEventType.SUBMIT_REJECTED,
                             IMessage.Type.ORDER_SUBMIT_REJECTED);
    }

    @Test
    public void testSubmitOKIsMappedCorrect() {
        assertCorrectMapping(OrderEventType.SUBMIT_OK,
                             IMessage.Type.ORDER_SUBMIT_OK);
    }

    @Test
    public void testConditionalSubmitOKIsMappedCorrect() {
        orderUnderTest.setOrderCommand(OrderCommand.BUYLIMIT);

        assertCorrectMapping(OrderEventType.SUBMIT_CONDITIONAL_OK,
                             IMessage.Type.ORDER_SUBMIT_OK);
    }

    @Test
    public void testFillRejectedIsMappedCorrect() {
        assertCorrectMapping(OrderEventType.FILL_REJECTED,
                             IMessage.Type.ORDER_FILL_REJECTED);
    }

    @Test
    public void testPartialFillIsMappedCorrect() {
        orderUnderTest.setAmount(0.1);
        orderUnderTest.setRequestedAmount(0.2);
        assertCorrectMapping(OrderEventType.PARTIAL_FILL_OK,
                             IMessage.Type.ORDER_FILL_OK);
        assertCorrectMapping(OrderEventType.PARTIAL_FILL_OK,
                             IMessage.Type.ORDER_CHANGED_OK);
    }

    @Test
    public void testChangeRejectIsMappedCorrect() {
        assertCorrectMapping(OrderEventType.CHANGED_REJECTED,
                             IMessage.Type.ORDER_CHANGED_REJECTED);
    }

    @Test
    public void testCloseOKIsMappedCorrect() {
        assertCorrectMapping(OrderEventType.CLOSE_OK,
                             IMessage.Type.ORDER_CLOSE_OK);
    }

    @Test
    public void testCloseRejectedIsMappedCorrect() {
        assertCorrectMapping(OrderEventType.CLOSE_REJECTED,
                             IMessage.Type.ORDER_CLOSE_REJECTED);
    }

    @Test
    public void testMergeOKIsMappedCorrect() {
        assertCorrectMapping(OrderEventType.MERGE_OK,
                             IMessage.Type.ORDERS_MERGE_OK);
    }

    @Test
    public void testMergeCloseOKIsMappedCorrect() {
        orderUnderTest.setState(IOrder.State.CLOSED);

        assertCorrectMapping(OrderEventType.MERGE_CLOSE_OK,
                             IMessage.Type.ORDERS_MERGE_OK);
    }

    @Test
    public void testMergeRejectIsMappedCorrect() {
        assertCorrectMapping(OrderEventType.MERGE_REJECTED,
                             IMessage.Type.ORDERS_MERGE_REJECTED);
    }

    @Test
    public void testPartialCloseOKIsMappedCorrect() {
        orderUnderTest.setState(IOrder.State.FILLED);
        assertCorrectMapping(OrderEventType.PARTIAL_CLOSE_OK,
                             IMessage.Type.ORDER_CLOSE_OK);
    }

    @Test
    public void onChangeGTTRejectRefinementMappingIsCorrect() {
        assertCorrectMappingForChangeRejectRefinement(OrderCallReason.CHANGE_GTT,
                                                      OrderEventType.CHANGE_GTT_REJECTED);
    }

    @Test
    public void onChangeLabelRejectRefinementMappingIsCorrect() {
        assertCorrectMappingForChangeRejectRefinement(OrderCallReason.CHANGE_LABEL,
                                                      OrderEventType.CHANGE_LABEL_REJECTED);
    }

    @Test
    public void onChangeAmountRejectRefinementMappingIsCorrect() {
        assertCorrectMappingForChangeRejectRefinement(OrderCallReason.CHANGE_AMOUNT,
                                                      OrderEventType.CHANGE_AMOUNT_REJECTED);
    }

    @Test
    public void onChangeOpenPriceRejectRefinementMappingIsCorrect() {
        assertCorrectMappingForChangeRejectRefinement(OrderCallReason.CHANGE_PRICE,
                                                      OrderEventType.CHANGE_PRICE_REJECTED);
    }

    @Test
    public void onChangeSLRejectRefinementMappingIsCorrect() {
        assertCorrectMappingForChangeRejectRefinement(OrderCallReason.CHANGE_SL,
                                                      OrderEventType.CHANGE_SL_REJECTED);
    }

    @Test
    public void onChangeTPRejectRefinementMappingIsCorrect() {
        assertCorrectMappingForChangeRejectRefinement(OrderCallReason.CHANGE_TP,
                                                      OrderEventType.CHANGE_TP_REJECTED);
    }

    public class MultipleCallRequestsRegistered {

        private OrderEventType getType(final IMessage.Type messageType,
                                       final IMessage.Reason... messageReasons) {
            final IMessageForTest message = new IMessageForTest(orderUnderTest,
                                                                messageType,
                                                                Sets.newHashSet(messageReasons));
            return orderEventMapper.get(new OrderMessageData(message));
        }

        @Before
        public void setUp() {
            registerCallRequest(OrderCallReason.SUBMIT);
            registerCallRequest(OrderCallReason.CHANGE_LABEL);
            registerCallRequest(OrderCallReason.CHANGE_PRICE);
            registerCallRequest(OrderCallReason.CHANGE_SL);
            registerCallRequest(OrderCallReason.CLOSE);
        }

        public class OnSubmitOK {

            private OrderEventType submitType;

            @Before
            public void setUp() {
                submitType = getType(IMessage.Type.ORDER_FILL_OK,
                                     IMessage.Reason.ORDER_FULLY_FILLED);
            }

            @Test
            public void eventTypeIsFullyFilled() {
                assertThat(submitType, equalTo(OrderEventType.FULLY_FILLED));
            }

            public class OnChangeLabelRejected {

                private OrderEventType changeLabelType;

                @Before
                public void setUp() {
                    changeLabelType = getType(IMessage.Type.ORDER_CHANGED_REJECTED);
                }

                @Test
                public void eventTypeIsLabelRejected() {
                    assertThat(changeLabelType, equalTo(OrderEventType.CHANGE_LABEL_REJECTED));
                }

                public class OnChangeOpenPriceRejected {

                    private OrderEventType changeOpenPriceType;

                    @Before
                    public void setUp() {
                        changeOpenPriceType = getType(IMessage.Type.ORDER_CHANGED_REJECTED);
                    }

                    @Test
                    public void eventTypeIsOpenPriceRejected() {
                        assertThat(changeOpenPriceType,
                                   equalTo(OrderEventType.CHANGE_PRICE_REJECTED));
                    }

                    public class OnChangeSL {

                        private OrderEventType changeSLType;

                        @Before
                        public void setUp() {
                            changeSLType = getType(IMessage.Type.ORDER_CHANGED_OK,
                                                   IMessage.Reason.ORDER_CHANGED_SL);
                        }

                        @Test
                        public void eventTypeChangeSL() {
                            assertThat(changeSLType, equalTo(OrderEventType.CHANGED_SL));
                        }

                        public class OnClose {

                            private OrderEventType closeType;

                            @Before
                            public void setUp() {
                                closeType = getType(IMessage.Type.ORDER_CLOSE_OK);
                            }

                            @Test
                            public void eventClose() {
                                assertThat(closeType, equalTo(OrderEventType.CLOSE_OK));
                            }
                        }
                    }
                }
            }
        }
    }
}
