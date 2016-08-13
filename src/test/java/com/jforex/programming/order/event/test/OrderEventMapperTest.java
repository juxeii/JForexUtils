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
import com.jforex.programming.order.call.OrderCallReason;
import com.jforex.programming.order.call.OrderCallRequest;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.event.OrderEventFactory;
import com.jforex.programming.order.event.OrderEventMapperData;
import com.jforex.programming.order.event.OrderEventType;
import com.jforex.programming.order.event.OrderEventTypeSets;
import com.jforex.programming.test.common.CommonUtilForTest;

import de.bechte.junit.runners.context.HierarchicalContextRunner;

@RunWith(HierarchicalContextRunner.class)
public class OrderEventMapperTest extends CommonUtilForTest {

    private OrderEventFactory orderEventMapper;

    private final IOrder orderForTest = buyOrderEURUSD;

    @Before
    public void setUp() {
        orderEventMapper = new OrderEventFactory();
    }

    private IMessage createMessage(final IMessage.Type messageType,
                                   final IMessage.Reason... messageReasons) {
        return mockForIMessage(orderForTest,
                               messageType,
                               Sets.newHashSet(messageReasons));
    }

    private void assertCorrectMapping(final OrderEventType expectedType,
                                      final IMessage.Type messageType,
                                      final IMessage.Reason... messageReasons) {
        final IMessage message = createMessage(messageType, messageReasons);

        final OrderEvent orderEvent = orderEventMapper.fromMessage(message);

        assertThat(orderEvent.type(), equalTo(expectedType));
    }

    private void
            assertCorrectMappingForChangeRejectRefinement(final OrderCallReason orderCallReason,
                                                          final OrderEventType expectedType) {
        orderEventMapper
            .registerOrderCallRequest(new OrderCallRequest(orderForTest, orderCallReason));
        assertCorrectMapping(expectedType, IMessage.Type.ORDER_CHANGED_REJECTED);
    }

    private void registerCallRequest(final OrderCallReason orderCallReason) {
        orderEventMapper
            .registerOrderCallRequest(new OrderCallRequest(orderForTest, orderCallReason));
    }

    @Test
    public void helperMapperClassesHavePrivateConstructors() throws Exception {
        assertPrivateConstructor(OrderEventTypeSets.class);
        assertPrivateConstructor(OrderEventMapperData.class);
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
        orderUtilForTest.setOrderCommand(orderForTest, OrderCommand.BUYLIMIT);

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
        orderUtilForTest.setRequestedAmount(orderForTest, 0.2);
        orderUtilForTest.setAmount(orderForTest, 0.1);

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
        orderUtilForTest.setState(orderForTest, IOrder.State.CLOSED);

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
        orderUtilForTest.setState(orderForTest, IOrder.State.FILLED);
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

    @Test
    public void notRegisteredOrderGetsOnlyChangeRejected() {
        registerCallRequest(OrderCallReason.CHANGE_LABEL);
        final IMessage message = mockForIMessage(orderUtilForTest.sellOrderAUDUSD(),
                                                 IMessage.Type.ORDER_CHANGED_REJECTED,
                                                 Sets.newHashSet());

        final OrderEvent actualEvent = orderEventMapper.fromMessage(message);

        assertThat(actualEvent.type(), equalTo(OrderEventType.CHANGED_REJECTED));
    }

    public class MultipleCallRequestsRegistered {

        private OrderEvent getEvent(final IMessage.Type messageType,
                                    final IMessage.Reason... messageReasons) {
            return orderEventMapper.fromMessage(createMessage(messageType, messageReasons));
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

            private OrderEvent submitEvent;

            @Before
            public void setUp() {
                submitEvent = getEvent(IMessage.Type.ORDER_FILL_OK,
                                       IMessage.Reason.ORDER_FULLY_FILLED);
            }

            @Test
            public void eventTypeIsFullyFilled() {
                assertThat(submitEvent.type(), equalTo(OrderEventType.FULLY_FILLED));
            }

            public class OnChangeLabelRejected {

                private OrderEvent changeLabelEvent;

                @Before
                public void setUp() {
                    changeLabelEvent = getEvent(IMessage.Type.ORDER_CHANGED_REJECTED);
                }

                @Test
                public void eventTypeIsLabelRejected() {
                    assertThat(changeLabelEvent.type(), equalTo(OrderEventType.CHANGE_LABEL_REJECTED));
                }

                public class OnChangeOpenPriceRejected {

                    private OrderEvent changeOpenPriceEvent;

                    @Before
                    public void setUp() {
                        changeOpenPriceEvent = getEvent(IMessage.Type.ORDER_CHANGED_REJECTED);
                    }

                    @Test
                    public void eventTypeIsOpenPriceRejected() {
                        assertThat(changeOpenPriceEvent.type(),
                                   equalTo(OrderEventType.CHANGE_PRICE_REJECTED));
                    }

                    public class OnChangeSL {

                        private OrderEvent changeSLEvent;

                        @Before
                        public void setUp() {
                            changeSLEvent = getEvent(IMessage.Type.ORDER_CHANGED_OK,
                                                     IMessage.Reason.ORDER_CHANGED_SL);
                        }

                        @Test
                        public void eventTypeChangeSL() {
                            assertThat(changeSLEvent.type(), equalTo(OrderEventType.CHANGED_SL));
                        }

                        public class OnClose {

                            private OrderEvent closeEvent;

                            @Before
                            public void setUp() {
                                closeEvent = getEvent(IMessage.Type.ORDER_CLOSE_OK);
                            }

                            @Test
                            public void eventClose() {
                                assertThat(closeEvent.type(), equalTo(OrderEventType.CLOSE_OK));
                            }
                        }
                    }
                }
            }
        }
    }
}
