package com.jforex.programming.order.event.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

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
import com.jforex.programming.order.event.OrderEventType;
import com.jforex.programming.order.event.OrderEventTypeMapper;
import com.jforex.programming.order.event.OrderEventTypeSets;
import com.jforex.programming.rx.JFHotPublisher;
import com.jforex.programming.test.common.CommonUtilForTest;

import de.bechte.junit.runners.context.HierarchicalContextRunner;
import io.reactivex.Observable;

@RunWith(HierarchicalContextRunner.class)
public class OrderEventFactoryTest extends CommonUtilForTest {

    private OrderEventFactory orderEventFactory;

    private final JFHotPublisher<OrderCallRequest> callRequestPublisher = new JFHotPublisher<>();
    private final Observable<OrderCallRequest> callRequestObservable = callRequestPublisher.observable();
    private final IOrder orderForTest = buyOrderEURUSD;

    @Before
    public void setUp() {
        orderEventFactory = new OrderEventFactory(callRequestObservable);
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

        final OrderEvent orderEvent = orderEventFactory.fromMessage(message);

        assertThat(orderEvent.type(), equalTo(expectedType));
    }

    private void assertCorrectMappingForChangeRejectRefinement(final OrderCallReason orderCallReason,
                                                               final OrderEventType expectedType) {
        callRequestPublisher.onNext(new OrderCallRequest(orderForTest, orderCallReason));
        assertCorrectMapping(expectedType, IMessage.Type.ORDER_CHANGED_REJECTED);
    }

    private void registerCallRequest(final OrderCallReason orderCallReason) {
        callRequestPublisher.onNext(new OrderCallRequest(orderForTest, orderCallReason));
    }

    @Test
    public void helperMapperClassesHavePrivateConstructors() throws Exception {
        assertPrivateConstructor(OrderEventTypeSets.class);
        assertPrivateConstructor(OrderEventTypeMapper.class);
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
    public void conditionalSubmitOKIsMappedCorrect() {
        orderUtilForTest.setOrderCommand(orderForTest, OrderCommand.BUYLIMIT);

        assertCorrectMapping(OrderEventType.SUBMIT_OK,
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
    public void notificationIsMappedCorrectForInternalOrder() {
        registerCallRequest(OrderCallReason.SUBMIT);

        orderUtilForTest.setState(orderForTest, IOrder.State.CREATED);
        assertCorrectMapping(OrderEventType.NOTIFICATION,
                             IMessage.Type.NOTIFICATION);
    }

    @Test
    public void partialFillIsMappedCorrectForInternalOrder() {
        registerCallRequest(OrderCallReason.SUBMIT);

        orderUtilForTest.setState(orderForTest, IOrder.State.OPENED);
        orderUtilForTest.setRequestedAmount(orderForTest, 0.2);
        orderUtilForTest.setAmount(orderForTest, 0.1);
        assertCorrectMapping(OrderEventType.PARTIAL_FILL_OK,
                             IMessage.Type.ORDER_FILL_OK);
    }

    @Test
    public void partialCloseOKIsMappedCorrectForInternalOrder() {
        registerCallRequest(OrderCallReason.CLOSE);

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

        final OrderEvent actualEvent = orderEventFactory.fromMessage(message);

        assertThat(actualEvent.type(), equalTo(OrderEventType.CHANGED_REJECTED));
    }

    @Test
    public void unknownOrderGetsExternalInOrderEvent() {
        final IMessage message = mockForIMessage(orderUtilForTest.sellOrderAUDUSD(),
                                                 IMessage.Type.ORDER_CHANGED_REJECTED,
                                                 Sets.newHashSet());

        final OrderEvent actualEvent = orderEventFactory.fromMessage(message);

        assertFalse(actualEvent.isInternal());
    }

    public class MultipleCallRequestsRegistered {

        private OrderEvent getEvent(final IMessage.Type messageType,
                                    final IMessage.Reason... messageReasons) {
            return orderEventFactory.fromMessage(createMessage(messageType, messageReasons));
        }

        @Before
        public void setUp() {
            callRequestPublisher.onNext(new OrderCallRequest(sellOrderAUDUSD, OrderCallReason.SUBMIT));

            registerCallRequest(OrderCallReason.SUBMIT);
            registerCallRequest(OrderCallReason.CHANGE_LABEL);
            registerCallRequest(OrderCallReason.CHANGE_PRICE);
            registerCallRequest(OrderCallReason.CHANGE_SL);
            registerCallRequest(OrderCallReason.CLOSE);
        }

        public class OnSubmitOKForAUDUSD {

            private OrderEvent submitEvent;
            private final IMessage message = mockForIMessage(sellOrderAUDUSD,
                                                             IMessage.Type.ORDER_FILL_OK,
                                                             Sets.newHashSet(IMessage.Reason.ORDER_FULLY_FILLED));

            @Before
            public void setUp() {
                submitEvent = orderEventFactory.fromMessage(message);
            }

            @Test
            public void eventTypeIsFullyFilled() {
                assertThat(submitEvent.type(), equalTo(OrderEventType.FULLY_FILLED));
            }

            @Test
            public void assertOrderIsInternal() {
                assertTrue(submitEvent.isInternal());
            }

            @Test
            public void nextEventForNotQueuedAUDUSDOrderIsInternal() {
                submitEvent = orderEventFactory.fromMessage(message);

                assertTrue(submitEvent.isInternal());
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

                @Test
                public void assertOrderIsInternal() {
                    assertTrue(submitEvent.isInternal());
                }

                public class OnChangeLabelRejected {

                    private OrderEvent changeLabelEvent;

                    @Before
                    public void setUp() {
                        changeLabelEvent = getEvent(IMessage.Type.ORDER_CHANGED_REJECTED);
                    }

                    @Test
                    public void eventTypeIsLabelRejected() {
                        assertThat(changeLabelEvent.type(),
                                   equalTo(OrderEventType.CHANGE_LABEL_REJECTED));
                    }

                    @Test
                    public void assertOrderIsInternal() {
                        assertTrue(changeLabelEvent.isInternal());
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

                        @Test
                        public void assertOrderIsInternal() {
                            assertTrue(changeOpenPriceEvent.isInternal());
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

                            @Test
                            public void assertOrderIsInternal() {
                                assertTrue(changeSLEvent.isInternal());
                            }

                            public class OnClose {

                                private OrderEvent closeEvent;

                                @Before
                                public void setUp() {
                                    orderUtilForTest.setState(orderForTest, IOrder.State.CLOSED);

                                    closeEvent = getEvent(IMessage.Type.ORDER_CLOSE_OK);
                                }

                                @Test
                                public void eventClose() {
                                    assertThat(closeEvent.type(), equalTo(OrderEventType.CLOSE_OK));
                                }

                                @Test
                                public void assertOrderIsInternal() {
                                    assertTrue(submitEvent.isInternal());
                                }

                                @Test
                                public void afterCloseMessageAllEventsAreNowExternal() {
                                    closeEvent = getEvent(IMessage.Type.ORDER_CLOSE_OK);

                                    assertFalse(closeEvent.isInternal());
                                }
                            }

                            public class OnCancelled {

                                private OrderEvent rejectEvent;

                                @Before
                                public void setUp() {
                                    orderUtilForTest.setState(orderForTest, IOrder.State.CANCELED);

                                    rejectEvent = getEvent(IMessage.Type.ORDER_SUBMIT_REJECTED);
                                }

                                @Test
                                public void eventReject() {
                                    assertThat(rejectEvent.type(), equalTo(OrderEventType.SUBMIT_REJECTED));
                                }

                                @Test
                                public void assertOrderIsExternal() {
                                    assertTrue(rejectEvent.isInternal());
                                }

                                @Test
                                public void afterCancelStateMessageAllEventsAreNowExternal() {
                                    rejectEvent = getEvent(IMessage.Type.ORDER_SUBMIT_REJECTED);

                                    assertFalse(rejectEvent.isInternal());
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
