package com.jforex.programming.order.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;

import com.dukascopy.api.IOrder;
import com.google.common.collect.Sets;
import com.jforex.programming.order.OrderParams;
import com.jforex.programming.order.OrderUtil;
import com.jforex.programming.order.OrderUtilHandler;
import com.jforex.programming.order.call.OrderCallRejectException;
import com.jforex.programming.order.command.CloseCommand;
import com.jforex.programming.order.command.MergeCommand;
import com.jforex.programming.order.command.OrderCallCommand;
import com.jforex.programming.order.command.SetAmountCommand;
import com.jforex.programming.order.command.SetGTTCommand;
import com.jforex.programming.order.command.SetLabelCommand;
import com.jforex.programming.order.command.SetOpenPriceCommand;
import com.jforex.programming.order.command.SetSLCommand;
import com.jforex.programming.order.command.SetTPCommand;
import com.jforex.programming.order.command.SubmitCommand;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.event.OrderEventType;
import com.jforex.programming.position.OrderProcessState;
import com.jforex.programming.position.Position;
import com.jforex.programming.position.PositionFactory;
import com.jforex.programming.position.PositionOrders;
import com.jforex.programming.test.common.InstrumentUtilForTest;
import com.jforex.programming.test.fakes.IOrderForTest;

import de.bechte.junit.runners.context.HierarchicalContextRunner;
import rx.Observable;
import rx.observers.TestSubscriber;

@RunWith(HierarchicalContextRunner.class)
public class OrderUtilTest extends InstrumentUtilForTest {

    private OrderUtil orderUtil;

    @Mock
    private PositionFactory positionFactoryMock;
    @Mock
    private OrderUtilHandler orderUtilHandlerMock;
    @Mock
    private PositionOrders positionOrdersMock;
    @Mock
    private Position positionMock;
    @Captor
    private ArgumentCaptor<OrderCallCommand> callCommandCaptor;
    private final TestSubscriber<OrderEvent> orderEventSubscriber = new TestSubscriber<>();
    private final IOrderForTest orderForTest = IOrderForTest.buyOrderEURUSD();
    private final IOrderForTest buyOrder = IOrderForTest.buyOrderEURUSD();
    private final IOrderForTest sellOrder = IOrderForTest.sellOrderEURUSD();
    private final Set<IOrder> toMergeOrders = Sets.newHashSet(buyOrder, sellOrder);
    private final String mergeOrderLabel = "MergeLabel";

    @Before
    public void setUp() {
        setUpMocks();

        orderUtil = new OrderUtil(engineMock,
                                  positionFactoryMock,
                                  orderUtilHandlerMock);
    }

    private void setUpMocks() {
        when(positionFactoryMock.forInstrument(instrumentEURUSD))
                .thenReturn(positionMock);
    }

    private void assertOrderEventNotification(final OrderEvent expectedEvent) {
        orderEventSubscriber.assertValueCount(1);

        final OrderEvent orderEvent = orderEventSubscriber.getOnNextEvents().get(0);

        assertThat(orderEvent.order(), equalTo(expectedEvent.order()));
        assertThat(orderEvent.type(), equalTo(expectedEvent.type()));
    }

    public void verifyOrderUtilMockCall(final Class<? extends OrderCallCommand> clazz) {
        verify(orderUtilHandlerMock).callObservable(isA(clazz));
    }

    private void setOrderUtilHandlerExpectation(final Class<? extends OrderCallCommand> clazz,
                                                final OrderEvent orderEvent) {
        when(orderUtilHandlerMock.callObservable(isA(clazz)))
                .thenReturn(eventObservable(orderEvent));
    }

    private void assertChangeCallCallsOnOrderUtilHandler(final Class<? extends OrderCallCommand> clazz,
                                                         final Observable<OrderEvent> observable) {
        final OrderEvent event = IOrderForTest.notificationEvent(orderForTest);
        setOrderUtilHandlerExpectation(clazz, event);

        observable.subscribe(orderEventSubscriber);

        verify(orderUtilHandlerMock).callObservable(isA(clazz));
        orderEventSubscriber.assertNoErrors();
        orderEventSubscriber.assertValueCount(1);
        orderEventSubscriber.assertCompleted();
        assertThat(orderEventSubscriber.getOnNextEvents().get(0), equalTo(event));
    }

    private void assertNoCallOnOrderUtilHandler(final Class<? extends OrderCallCommand> clazz,
                                                final Observable<OrderEvent> observable) {
        final OrderEvent event = IOrderForTest.notificationEvent(orderForTest);
        setOrderUtilHandlerExpectation(clazz, event);

        observable.subscribe(orderEventSubscriber);

        verify(orderUtilHandlerMock, never()).callObservable(isA(clazz));
        orderEventSubscriber.assertNoErrors();
        orderEventSubscriber.assertValueCount(0);
        orderEventSubscriber.assertCompleted();
    }

    @Test
    public void positionOrdersReturnsCorrectInstance() {
        assertThat(orderUtil.positionOrders(instrumentEURUSD),
                   equalTo(positionMock));
    }

    public class SubmitSetup {

        private final OrderParams orderParams = IOrderForTest.paramsBuyEURUSD();

        private void setOrderUtilHandlerMockResult(final Observable<OrderEvent> observable) {
            when(orderUtilHandlerMock.callObservable(isA(SubmitCommand.class)))
                    .thenReturn(observable);
        }

        @Test
        public void noOrderIsAddedWhenNoDoneEvent() {
            final OrderEvent submitOKEvent =
                    new OrderEvent(orderForTest, OrderEventType.SUBMIT_OK);
            setOrderUtilHandlerMockResult(eventObservable(submitOKEvent));

            orderUtil
                    .submitOrder(orderParams)
                    .subscribe(orderEventSubscriber);

            verifyZeroInteractions(positionMock);
        }

        public class SubmitDone {

            private final OrderEvent submitDoneEvent =
                    new OrderEvent(orderForTest, OrderEventType.FULLY_FILLED);

            @Before
            public void setUp() {
                setOrderUtilHandlerMockResult(eventObservable(submitDoneEvent));

                orderUtil
                        .submitOrder(orderParams)
                        .subscribe(orderEventSubscriber);
            }

            @Test
            public void submitOnOrderUtilHandlerHasBeenCalled() {
                verifyOrderUtilMockCall(SubmitCommand.class);
            }

            @Test
            public void subscriberHasBeenNotifiedWithOrderEvent() {
                assertOrderEventNotification(submitDoneEvent);
            }

            @Test
            public void subscriberCompleted() {
                orderEventSubscriber.assertCompleted();
            }

            @Test
            public void orderHasBeenAddedToPosition() {
                verify(positionMock).addOrder(orderForTest);
            }
        }
    }

    public class MergeSetup {

        private Runnable mergeCall;

        private void setOrderUtilHandlerMockResult(final Observable<OrderEvent> observable) {
            when(orderUtilHandlerMock.callObservable(isA(MergeCommand.class)))
                    .thenReturn(observable);
        }

        @Before
        public void setUp() {
            mergeCall = () -> orderUtil
                    .mergeOrders(mergeOrderLabel, toMergeOrders)
                    .subscribe(orderEventSubscriber);
        }

        public class NotEnoughOrdersForMerge {

            @Before
            public void setUp() {
                orderUtil
                        .mergeOrders(mergeOrderLabel, Sets.newHashSet(orderForTest))
                        .subscribe(orderEventSubscriber);
            }

            @Test
            public void subscriberCompletesEmpty() {
                orderEventSubscriber.assertNoErrors();
                orderEventSubscriber.assertValueCount(0);
                orderEventSubscriber.assertCompleted();
            }

            @Test
            public void noCallToOrderUtilHandler() {
                verifyZeroInteractions(orderUtilHandlerMock);
            }
        }

        @Test
        public void noOrderIsAddedWhenNoDoneEvent() {
            final OrderEvent submitOKEvent =
                    new OrderEvent(orderForTest, OrderEventType.SUBMIT_OK);
            setOrderUtilHandlerMockResult(eventObservable(submitOKEvent));

            mergeCall.run();

            verifyZeroInteractions(positionMock);
        }

        public class MergeDone {

            private final OrderEvent mergeDoneEvent =
                    new OrderEvent(orderForTest, OrderEventType.MERGE_OK);

            @Before
            public void setUp() {
                setOrderUtilHandlerMockResult(eventObservable(mergeDoneEvent));

                mergeCall.run();
            }

            @Test
            public void mergeOnOrderUtilHandlerHasBeenCalled() {
                verifyOrderUtilMockCall(MergeCommand.class);
            }

            @Test
            public void subscriberHasBeenNotifiedWithOrderEvent() {
                assertOrderEventNotification(mergeDoneEvent);
            }

            @Test
            public void subscriberCompleted() {
                orderEventSubscriber.assertCompleted();
            }

            @Test
            public void orderHasBeenAddedToPosition() {
                verify(positionMock).addOrder(orderForTest);
            }
        }
    }

    public class MergePositionSetup {

        private Runnable mergePositionCall;

        private void setOrderUtilHandlerResult(final Class<? extends OrderCallCommand> clazz,
                                               final Observable<OrderEvent> observable) {
            when(orderUtilHandlerMock.callObservable(isA(clazz)))
                    .thenReturn(observable);
        }

        @Before
        public void setUp() {
            mergePositionCall = () -> orderUtil
                    .mergePositionOrders(mergeOrderLabel, instrumentEURUSD)
                    .subscribe(orderEventSubscriber);
        }

        public class NotEnoughOrdersForMerge {

            @Before
            public void setUp() {
                when(positionMock.filled()).thenReturn(Sets.newHashSet());

                mergePositionCall.run();
            }

            @Test
            public void subscriberCompletesEmpty() {
                orderEventSubscriber.assertNoErrors();
                orderEventSubscriber.assertValueCount(0);
                orderEventSubscriber.assertCompleted();
            }

            @Test
            public void noCallToOrderUtilHandler() {
                verifyZeroInteractions(orderUtilHandlerMock);
            }

            @Test
            public void positionOrdersWereNotMarkedAsActive() {
                verify(positionMock, never()).markAllOrders(OrderProcessState.ACTIVE);
            }
        }

        public class EnoughOrdersForMerge {

            @Before
            public void setUp() {
                when(positionMock.filled()).thenReturn(toMergeOrders);
            }

            @Test
            public void allPositionOrdersAreMarkedActive() {
                mergePositionCall.run();

                verify(positionMock).markAllOrders(OrderProcessState.ACTIVE);
            }

            public class RemoveTPCallFail {

                private final OrderEvent rejectTPEvent =
                        new OrderEvent(buyOrder, OrderEventType.CHANGE_TP_REJECTED);

                @Before
                public void setUp() {
                    setOrderUtilHandlerResult(SetTPCommand.class,
                                              rejectObservable(rejectTPEvent));

                    mergePositionCall.run();
                }

                @Test
                public void allPositionOrdersAreMarkedIDLEAgain() {
                    verify(positionMock).markAllOrders(OrderProcessState.IDLE);
                }

                @Test
                public void subscriberErrors() {
                    orderEventSubscriber.assertError(OrderCallRejectException.class);
                }
            }

            public class RemoveTPOKCall {

                private final OrderEvent setTPEvent =
                        new OrderEvent(buyOrder, OrderEventType.CHANGED_TP);

                @Before
                public void setUp() {
                    setOrderUtilHandlerResult(SetTPCommand.class,
                                              eventObservable(setTPEvent));
                }

                @Test
                public void verifyRemoveTPCalls() {
                    mergePositionCall.run();

                    verify(orderUtilHandlerMock, times(2))
                            .callObservable(isA(SetTPCommand.class));
                }

                public class RemoveSLCallFail {

                    private final OrderEvent rejectSLEvent =
                            new OrderEvent(buyOrder, OrderEventType.CHANGE_SL_REJECTED);

                    @Before
                    public void setUp() {
                        setOrderUtilHandlerResult(SetSLCommand.class,
                                                  rejectObservable(rejectSLEvent));

                        mergePositionCall.run();
                    }

                    @Test
                    public void allPositionOrdersAreMarkedIDLEAgain() {
                        verify(positionMock).markAllOrders(OrderProcessState.IDLE);
                    }

                    @Test
                    public void subscriberErrors() {
                        orderEventSubscriber.assertError(OrderCallRejectException.class);
                    }
                }

                public class RemoveSLOKCall {

                    private final OrderEvent setSLEvent =
                            new OrderEvent(buyOrder, OrderEventType.CHANGED_SL);

                    @Before
                    public void setUp() {
                        setOrderUtilHandlerResult(SetSLCommand.class,
                                                  eventObservable(setSLEvent));
                    }

                    @Test
                    public void verifyRemoveSLCalls() {
                        mergePositionCall.run();

                        verify(orderUtilHandlerMock, times(2))
                                .callObservable(isA(SetSLCommand.class));
                    }

                    public class MergeCallFail {

                        private final OrderEvent rejectEvent =
                                new OrderEvent(orderForTest, OrderEventType.MERGE_REJECTED);

                        @Before
                        public void setUp() {
                            setOrderUtilHandlerResult(MergeCommand.class,
                                                      rejectObservable(rejectEvent));

                            mergePositionCall.run();
                        }

                        @Test
                        public void allPositionOrdersAreMarkedIDLEAgain() {
                            verify(positionMock).markAllOrders(OrderProcessState.IDLE);
                        }

                        @Test
                        public void subscriberErrors() {
                            orderEventSubscriber.assertError(OrderCallRejectException.class);
                        }
                    }

                    public class MergeOKCall {

                        private final OrderEvent mergeEvent =
                                new OrderEvent(orderForTest, OrderEventType.MERGE_OK);

                        @Before
                        public void setUp() {
                            setOrderUtilHandlerResult(MergeCommand.class,
                                                      eventObservable(mergeEvent));

                            mergePositionCall.run();
                        }

                        @Test
                        public void verifyMergeCall() {
                            mergePositionCall.run();

                            verify(orderUtilHandlerMock)
                                    .callObservable(isA(MergeCommand.class));
                        }

                        @Test
                        public void testOrderHasBeenAddedToPosition() {
                            verify(positionMock).addOrder(orderForTest);
                        }

                        @Test
                        public void subscriberIsCompleted() {
                            orderEventSubscriber.assertCompleted();
                        }

                        @Test
                        public void positionOrdersAreMarkedAsIDLE() {
                            verify(positionMock).markAllOrders(OrderProcessState.IDLE);
                        }
                    }
                }
            }
        }
    }

    public class ClosePositionSetup {

        private final Set<IOrder> ordersToClose = Sets.newHashSet(buyOrder, sellOrder);
        private final Runnable closeCompletableCall =
                () -> orderUtil
                        .closePosition(instrumentEURUSD)
                        .subscribe(orderEventSubscriber);

        @Before
        public void setUp() {
            buyOrder.setState(IOrder.State.FILLED);
            sellOrder.setState(IOrder.State.CLOSED);

            when(positionMock.filledOrOpened()).thenReturn(ordersToClose);
        }

        @Test
        public void testThatAllPositionOrdersAreMarkedAsActive() {
            closeCompletableCall.run();

            verify(positionMock).markAllOrders(OrderProcessState.ACTIVE);
        }

        public class NoOrdersToClose {

            @Before
            public void setUp() {
                when(positionMock.filledOrOpened()).thenReturn(Sets.newHashSet());

                closeCompletableCall.run();
            }

            @Test
            public void testNoCallToOrderUtilHandler() {
                verify(orderUtilHandlerMock, never())
                        .callObservable(any());
            }

            @Test
            public void testSubscriberCompleted() {
                orderEventSubscriber.assertCompleted();
            }
        }

        public class CloseCallWithJFException {

            @Before
            public void setUp() {
                when(orderUtilHandlerMock.callObservable(isA(CloseCommand.class)))
                        .thenReturn(rejectObservable(null));

                closeCompletableCall.run();
            }

            @Test
            public void errorIsObserved() {
                orderEventSubscriber.assertError(OrderCallRejectException.class);
            }
        }

        public class CloseCall {

            @Before
            public void setUp() {
                closeCompletableCall.run();
            }

            @Test
            public void onlyCloseForBuyOrderIsCalled() throws Exception {
                verify(orderUtilHandlerMock)
                        .callObservable(callCommandCaptor.capture());

                final OrderCallCommand command = callCommandCaptor.getValue();
                command.callable().call();

                verify(buyOrder).close();
            }

            @Test
            public void closeForSellOrderIsNotCalledSinceAlreadyClosed() throws Exception {
                verify(sellOrder, never()).close();
            }

            @Test
            public void testSubscriberCompleted() {
                orderEventSubscriber.assertCompleted();
            }

            @Test
            public void positionOrdersAreMarkedAsIDLE() {
                verify(positionMock).markAllOrders(OrderProcessState.IDLE);
            }
        }
    }

    public class ChangeCallSetup {

        private Observable<OrderEvent> observable;
        private Class<? extends OrderCallCommand> commandClass;

        private void assertOrderUtilHandlerCall() {
            assertChangeCallCallsOnOrderUtilHandler(commandClass,
                                                    observable);
        }

        private void assertNoOrderUtilHandlerCall() {
            assertNoCallOnOrderUtilHandler(commandClass,
                                           observable);
        }

        public class CloseSetup {

            private final IOrder.State newState = IOrder.State.CLOSED;

            @Before
            public void setUp() {
                commandClass = CloseCommand.class;
                observable = orderUtil.close(orderForTest);
            }

            @Test
            public void closeCallsOnOrderUtilHandler() {
                assertOrderUtilHandlerCall();
            }

            @Test
            public void closeCallDoesNotCallOrderUtilHandlerWhenOrderAlreadyClosed() {
                orderForTest.setState(newState);

                assertNoOrderUtilHandlerCall();
            }
        }

        public class SetLabelSetup {

            private final String newLabel = "newLabel";

            @Before
            public void setUp() {
                commandClass = SetLabelCommand.class;
                observable = orderUtil.setLabel(orderForTest, newLabel);
            }

            @Test
            public void setLabelCallsOnOrderUtilHandler() {
                assertOrderUtilHandlerCall();
            }

            @Test
            public void setLabelCallDoesNotCallOrderUtilHandlerWhenLabelAlreadySet() {
                orderForTest.setLabel(newLabel);

                assertNoOrderUtilHandlerCall();
            }
        }

        public class SetGTTSetup {

            private final Long newGTT = 1234L;

            @Before
            public void setUp() {
                commandClass = SetGTTCommand.class;
                observable = orderUtil.setGoodTillTime(orderForTest, newGTT);
            }

            @Test
            public void setGTTCallsOnOrderUtilHandler() {
                assertOrderUtilHandlerCall();
            }

            @Test
            public void setGTTCallDoesNotCallOrderUtilHandlerWhenGTTAlreadySet() {
                orderForTest.setGoodTillTime(newGTT);

                assertNoOrderUtilHandlerCall();
            }
        }

        public class SetAmountSetup {

            private final double newAmount = 0.12;

            @Before
            public void setUp() {
                commandClass = SetAmountCommand.class;
                observable = orderUtil.setRequestedAmount(orderForTest, newAmount);
            }

            @Test
            public void setAmountCallsOnOrderUtilHandler() {
                assertOrderUtilHandlerCall();
            }

            @Test
            public void setAmountCallDoesNotCallOrderUtilHandlerWhenAmountAlreadySet() {
                orderForTest.setRequestedAmount(newAmount);

                assertNoOrderUtilHandlerCall();
            }
        }

        public class SetOpenPriceSetup {

            private final double newOpenPrice = 1.1234;

            @Before
            public void setUp() {
                commandClass = SetOpenPriceCommand.class;
                observable = orderUtil.setOpenPrice(orderForTest, newOpenPrice);
            }

            @Test
            public void setAmountCallsOnOrderUtilHandler() {
                assertOrderUtilHandlerCall();
            }

            @Test
            public void setOpenPriceCallDoesNotCallOrderUtilHandlerWhenOpenPriceAlreadySet() {
                orderForTest.setOpenPrice(newOpenPrice);

                assertNoOrderUtilHandlerCall();
            }
        }

        public class SetTPSetup {

            private final double newTP = 1.11001;

            @Before
            public void setUp() {
                commandClass = SetTPCommand.class;
                observable = orderUtil.setTakeProfitPrice(orderForTest, newTP);
            }

            @Test
            public void setTakeProfitPriceCallsOnOrderUtilHandler() {
                assertOrderUtilHandlerCall();
            }

            @Test
            public void setTakeProfitPriceDoesNotCallOrderUtilHandlerWhenTPAlreadySet() {
                orderForTest.setTakeProfitPrice(newTP);

                assertNoOrderUtilHandlerCall();
            }
        }

        public class SetSLSetup {

            private final double newSL = 1.11001;

            @Before
            public void setUp() {
                commandClass = SetSLCommand.class;
                observable = orderUtil.setStopLossPrice(orderForTest, newSL);
            }

            @Test
            public void setStopLossPriceCallsOnOrderUtilHandler() {
                assertOrderUtilHandlerCall();
            }

            @Test
            public void setStopLossPriceDoesNotCallOrderUtilHandlerWhenSLAlreadySet() {
                orderForTest.setStopLossPrice(newSL);

                assertNoOrderUtilHandlerCall();
            }
        }
    }
}
