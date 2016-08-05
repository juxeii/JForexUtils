package com.jforex.programming.order.test;

import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;

import com.dukascopy.api.IEngine.OrderCommand;
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
import com.jforex.programming.position.Position;
import com.jforex.programming.position.PositionFactory;
import com.jforex.programming.position.PositionOrders;
import com.jforex.programming.test.common.InstrumentUtilForTest;

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
    @Captor
    private ArgumentCaptor<OrderParams> paramsCaptor;
    private final String mergeOrderLabel = "MergeLabel";
    private final TestSubscriber<OrderEvent> orderEventSubscriber = new TestSubscriber<>();
    private final OrderEvent submitOKEvent = new OrderEvent(buyOrderEURUSD, OrderEventType.SUBMIT_OK);
    private final OrderEvent notificationEvent = new OrderEvent(buyOrderEURUSD, OrderEventType.NOTIFICATION);

    @Before
    public void setUp() {
        setUpMocks();

        orderUtil = spy(new OrderUtil(engineMock,
                                      positionFactoryMock,
                                      orderUtilHandlerMock));
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
        setOrderUtilHandlerExpectation(clazz, notificationEvent);

        observable.subscribe(orderEventSubscriber);

        verify(orderUtilHandlerMock).callObservable(isA(clazz));
        orderEventSubscriber.assertNoErrors();
        orderEventSubscriber.assertValueCount(1);
        orderEventSubscriber.assertCompleted();
        assertThat(orderEventSubscriber.getOnNextEvents().get(0), equalTo(notificationEvent));
    }

    private void assertNoCallOnOrderUtilHandler(final Class<? extends OrderCallCommand> clazz,
                                                final Observable<OrderEvent> observable) {
        setOrderUtilHandlerExpectation(clazz, notificationEvent);

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

        private void setOrderUtilHandlerMockResult(final Observable<OrderEvent> observable) {
            when(orderUtilHandlerMock.callObservable(isA(SubmitCommand.class)))
                    .thenReturn(observable);
        }

        public class SubmitAndMergeSetup {

            private Runnable submitAndMergeCall;

            @Before
            public void setUp() {
                submitAndMergeCall = () -> orderUtil
                        .submitAndMergePosition(mergeOrderLabel, buyParamsEURUSD)
                        .subscribe(orderEventSubscriber);
            }

            @Test
            public void theMergeCallIsDeferred() {
                setOrderUtilHandlerMockResult(neverObservable());

                submitAndMergeCall.run();

                verify(orderUtil).submitOrder(buyParamsEURUSD);
                verify(orderUtil, never()).mergeOrders(eq(mergeOrderLabel), any());
            }

            @Test
            public void submitAndMergeAreBothCalledWhenSubmitIsOK() {
                setOrderUtilHandlerMockResult(eventObservable(submitOKEvent));

                submitAndMergeCall.run();

                verify(orderUtil).submitOrder(buyParamsEURUSD);
                verify(orderUtil).mergeOrders(eq(mergeOrderLabel), any());
            }
        }

        @Test
        public void noOrderIsAddedWhenNoDoneEvent() {
            setOrderUtilHandlerMockResult(eventObservable(submitOKEvent));

            orderUtil
                    .submitOrder(buyParamsEURUSD)
                    .subscribe(orderEventSubscriber);

            verifyZeroInteractions(positionMock);
        }

        public class SubmitDone {

            private final OrderEvent submitDoneEvent =
                    new OrderEvent(buyOrderEURUSD, OrderEventType.FULLY_FILLED);

            @Before
            public void setUp() {
                setOrderUtilHandlerMockResult(eventObservable(submitDoneEvent));

                orderUtil
                        .submitOrder(buyParamsEURUSD)
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
                verify(positionMock).addOrder(buyOrderEURUSD);
            }
        }
    }

    public class SubmitAndMergeToParamsSetup {

        private OrderParams adaptedOrderParams;

        private void callAndVerifyMergeInvocation(final double positionExposure) {
            when(positionMock.signedExposure()).thenReturn(positionExposure);
            when(orderUtilHandlerMock.callObservable(isA(SubmitCommand.class)))
                    .thenReturn(eventObservable(submitOKEvent));

            orderUtil
                    .submitAndMergePositionToParams(mergeOrderLabel, buyParamsEURUSD)
                    .subscribe(orderEventSubscriber);

            verify(orderUtil).submitAndMergePosition(eq(mergeOrderLabel),
                                                     paramsCaptor.capture());
            adaptedOrderParams = paramsCaptor.getValue();
        }

        private void assertAdaptedOrderParams(final OrderCommand orderCommand,
                                              final double amount) {
            assertThat(adaptedOrderParams.orderCommand(),
                       equalTo(orderCommand));
            assertThat(adaptedOrderParams.amount(),
                       closeTo(amount, 0.0001));
        }

        @Test
        public void returnedObservableIsCorrectInstance() {
            callAndVerifyMergeInvocation(0.0);

            orderEventSubscriber.assertNoErrors();
            orderEventSubscriber.assertValueCount(1);
            assertThat(orderEventSubscriber.getOnNextEvents().get(0), equalTo(submitOKEvent));
        }

        @Test
        public void whenPositionHasNoExposureOrderParamsAreTaken() {
            callAndVerifyMergeInvocation(0.0);

            assertAdaptedOrderParams(buyParamsEURUSD.orderCommand(), buyParamsEURUSD.amount());
        }

        @Test
        public void whenPositionHasPositiveExposureOrderParamsAreSHORT() {
            callAndVerifyMergeInvocation(0.12);

            assertAdaptedOrderParams(OrderCommand.SELL, 0.02);
        }

        @Test
        public void whenPositionHasNegativeExposureOrderParamsAreLONG() {
            callAndVerifyMergeInvocation(-0.12);

            assertAdaptedOrderParams(OrderCommand.BUY, 0.22);
        }
    }

    public class MergeSetup {

        private final Set<IOrder> toMergeOrders = Sets.newHashSet(buyOrderEURUSD, sellOrderEURUSD);
        private final OrderEvent mergeEvent = new OrderEvent(buyOrderEURUSD, OrderEventType.MERGE_OK);
        private Runnable mergeCall;

        private void setOrderUtilHandlerResult(final Class<? extends OrderCallCommand> clazz,
                                               final Observable<OrderEvent> observable) {
            when(orderUtilHandlerMock.callObservable(isA(clazz)))
                    .thenReturn(observable);
        }

        @Before
        public void setUp() {
            mergeCall = () -> orderUtil
                    .mergePositionOrders(mergeOrderLabel, instrumentEURUSD)
                    .subscribe(orderEventSubscriber);

            setOrderUtilHandlerResult(MergeCommand.class,
                                      eventObservable(mergeEvent));
        }

        @Test
        public void positionMergeCallsNormalMerge() {
            final Observable<OrderEvent> expectedObservable = eventObservable(mergeEvent);
            when(positionMock.filled()).thenReturn(toMergeOrders);
            when(orderUtil.mergeOrders(mergeOrderLabel, toMergeOrders))
                    .thenReturn(expectedObservable);

            final Observable<OrderEvent> observable = orderUtil
                    .mergePositionOrders(mergeOrderLabel, instrumentEURUSD);
            observable.subscribe(orderEventSubscriber);

            verify(orderUtil, times(2)).mergeOrders(mergeOrderLabel, toMergeOrders);
            orderEventSubscriber.assertNoErrors();
            orderEventSubscriber.assertValueCount(1);
            orderEventSubscriber.assertCompleted();
        }

        public class NotEnoughOrdersForMerge {

            @Before
            public void setUp() {
                when(positionMock.filled()).thenReturn(Sets.newHashSet());

                mergeCall.run();
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
                verify(positionMock, never()).markOrdersActive(any());
            }
        }

        public class EnoughOrdersForMerge {

            @Before
            public void setUp() {
                when(positionMock.filled()).thenReturn(toMergeOrders);
            }

            @Test
            public void allPositionOrdersAreMarkedActive() {
                mergeCall.run();

                verify(positionMock).markOrdersActive(toMergeOrders);
            }

            public class RemoveTPCallFail {

                private final OrderEvent rejectTPEvent =
                        new OrderEvent(buyOrderEURUSD, OrderEventType.CHANGE_TP_REJECTED);

                @Before
                public void setUp() {
                    setOrderUtilHandlerResult(SetTPCommand.class,
                                              rejectObservable(rejectTPEvent));

                    mergeCall.run();
                }

                @Test
                public void allPositionOrdersAreMarkedIDLEAgain() {
                    verify(positionMock).markOrdersIdle(toMergeOrders);
                }

                @Test
                public void subscriberErrors() {
                    orderEventSubscriber.assertError(OrderCallRejectException.class);
                }
            }

            public class RemoveTPOKCall {

                private final OrderEvent setTPEvent =
                        new OrderEvent(buyOrderEURUSD, OrderEventType.CHANGED_TP);

                @Before
                public void setUp() {
                    setOrderUtilHandlerResult(SetTPCommand.class,
                                              eventObservable(setTPEvent));
                }

                @Test
                public void verifyRemoveTPCalls() {
                    mergeCall.run();

                    verify(orderUtilHandlerMock, times(2))
                            .callObservable(isA(SetTPCommand.class));
                }

                public class RemoveSLCallFail {

                    private final OrderEvent rejectSLEvent =
                            new OrderEvent(buyOrderEURUSD, OrderEventType.CHANGE_SL_REJECTED);

                    @Before
                    public void setUp() {
                        setOrderUtilHandlerResult(SetSLCommand.class,
                                                  rejectObservable(rejectSLEvent));

                        mergeCall.run();
                    }

                    @Test
                    public void allPositionOrdersAreMarkedIDLEAgain() {
                        verify(positionMock).markOrdersIdle(toMergeOrders);
                    }

                    @Test
                    public void subscriberErrors() {
                        orderEventSubscriber.assertError(OrderCallRejectException.class);
                    }
                }

                public class RemoveSLOKCall {

                    private final OrderEvent setSLEvent =
                            new OrderEvent(buyOrderEURUSD, OrderEventType.CHANGED_SL);

                    @Before
                    public void setUp() {
                        setOrderUtilHandlerResult(SetSLCommand.class,
                                                  eventObservable(setSLEvent));
                    }

                    @Test
                    public void verifyRemoveSLCalls() {
                        mergeCall.run();

                        verify(orderUtilHandlerMock, times(2))
                                .callObservable(isA(SetSLCommand.class));
                    }

                    public class MergeCallFail {

                        private final OrderEvent rejectEvent =
                                new OrderEvent(buyOrderEURUSD, OrderEventType.MERGE_REJECTED);

                        @Before
                        public void setUp() {
                            setOrderUtilHandlerResult(MergeCommand.class,
                                                      rejectObservable(rejectEvent));

                            mergeCall.run();
                        }

                        @Test
                        public void allPositionOrdersAreMarkedIDLEAgain() {
                            verify(positionMock).markOrdersIdle(toMergeOrders);
                        }

                        @Test
                        public void subscriberErrors() {
                            orderEventSubscriber.assertError(OrderCallRejectException.class);
                        }
                    }

                    public class MergeOKCall {

                        @Before
                        public void setUp() {
                            setOrderUtilHandlerResult(MergeCommand.class,
                                                      eventObservable(mergeEvent));

                            mergeCall.run();
                        }

                        @Test
                        public void verifyMergeCall() {
                            verify(orderUtilHandlerMock)
                                    .callObservable(isA(MergeCommand.class));
                        }

                        @Test
                        public void testOrderHasBeenAddedToPosition() {
                            verify(positionMock).addOrder(buyOrderEURUSD);
                        }

                        @Test
                        public void subscriberIsCompleted() {
                            orderEventSubscriber.assertCompleted();
                        }

                        @Test
                        public void positionOrdersAreMarkedAsIDLE() {
                            verify(positionMock).markOrdersIdle(toMergeOrders);
                        }
                    }
                }
            }
        }
    }

    public class ClosePositionSetup {

        private final Set<IOrder> ordersToClose = Sets.newHashSet(buyOrderEURUSD, sellOrderEURUSD);
        private final Runnable closeCompletableCall =
                () -> orderUtil
                        .closePosition(instrumentEURUSD)
                        .subscribe(orderEventSubscriber);

        @Before
        public void setUp() {
            orderUtilForTest.setState(buyOrderEURUSD, IOrder.State.FILLED);
            orderUtilForTest.setState(sellOrderEURUSD, IOrder.State.CLOSED);

            when(positionMock.filledOrOpened()).thenReturn(ordersToClose);
        }

        @Test
        public void testThatAllPositionOrdersAreMarkedAsActive() {
            closeCompletableCall.run();

            verify(positionMock).markOrdersActive(ordersToClose);
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

                verify(buyOrderEURUSD).close();
            }

            @Test
            public void closeForSellOrderIsNotCalledSinceAlreadyClosed() throws Exception {
                verify(sellOrderEURUSD, never()).close();
            }

            @Test
            public void testSubscriberCompleted() {
                orderEventSubscriber.assertCompleted();
            }

            @Test
            public void positionOrdersAreMarkedAsIDLE() {
                verify(positionMock).markOrdersIdle(ordersToClose);
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
                observable = orderUtil.close(buyOrderEURUSD);
            }

            @Test
            public void closeCallsOnOrderUtilHandler() {
                assertOrderUtilHandlerCall();
            }

            @Test
            public void closeCallDoesNotCallOrderUtilHandlerWhenOrderAlreadyClosed() {
                orderUtilForTest.setState(buyOrderEURUSD, newState);

                assertNoOrderUtilHandlerCall();
            }
        }

        public class SetLabelSetup {

            private final String newLabel = "newLabel";

            @Before
            public void setUp() {
                commandClass = SetLabelCommand.class;
                observable = orderUtil.setLabel(buyOrderEURUSD, newLabel);
            }

            @Test
            public void setLabelCallsOnOrderUtilHandler() {
                assertOrderUtilHandlerCall();
            }

            @Test
            public void setLabelCallDoesNotCallOrderUtilHandlerWhenLabelAlreadySet() {
                orderUtilForTest.setLabel(buyOrderEURUSD, newLabel);

                assertNoOrderUtilHandlerCall();
            }
        }

        public class SetGTTSetup {

            private final Long newGTT = 1234L;

            @Before
            public void setUp() {
                commandClass = SetGTTCommand.class;
                observable = orderUtil.setGoodTillTime(buyOrderEURUSD, newGTT);
            }

            @Test
            public void setGTTCallsOnOrderUtilHandler() {
                assertOrderUtilHandlerCall();
            }

            @Test
            public void setGTTCallDoesNotCallOrderUtilHandlerWhenGTTAlreadySet() {
                orderUtilForTest.setGTT(buyOrderEURUSD, newGTT);

                assertNoOrderUtilHandlerCall();
            }
        }

        public class SetAmountSetup {

            private final double newAmount = 0.12;

            @Before
            public void setUp() {
                commandClass = SetAmountCommand.class;
                observable = orderUtil.setRequestedAmount(buyOrderEURUSD, newAmount);
            }

            @Test
            public void setAmountCallsOnOrderUtilHandler() {
                assertOrderUtilHandlerCall();
            }

            @Test
            public void setAmountCallDoesNotCallOrderUtilHandlerWhenAmountAlreadySet() {
                orderUtilForTest.setRequestedAmount(buyOrderEURUSD, newAmount);

                assertNoOrderUtilHandlerCall();
            }
        }

        public class SetOpenPriceSetup {

            private final double newOpenPrice = 1.1234;

            @Before
            public void setUp() {
                commandClass = SetOpenPriceCommand.class;
                observable = orderUtil.setOpenPrice(buyOrderEURUSD, newOpenPrice);
            }

            @Test
            public void setAmountCallsOnOrderUtilHandler() {
                assertOrderUtilHandlerCall();
            }

            @Test
            public void setOpenPriceCallDoesNotCallOrderUtilHandlerWhenOpenPriceAlreadySet() {
                orderUtilForTest.setOpenPrice(buyOrderEURUSD, newOpenPrice);

                assertNoOrderUtilHandlerCall();
            }
        }

        public class SetTPSetup {

            private final double newTP = 1.11001;

            @Before
            public void setUp() {
                commandClass = SetTPCommand.class;
                observable = orderUtil.setTakeProfitPrice(buyOrderEURUSD, newTP);
            }

            @Test
            public void setTakeProfitPriceCallsOnOrderUtilHandler() {
                assertOrderUtilHandlerCall();
            }

            @Test
            public void setTakeProfitPriceDoesNotCallOrderUtilHandlerWhenTPAlreadySet() {
                orderUtilForTest.setTP(buyOrderEURUSD, newTP);

                assertNoOrderUtilHandlerCall();
            }
        }

        public class SetSLSetup {

            private final double newSL = 1.11001;

            @Before
            public void setUp() {
                commandClass = SetSLCommand.class;
                observable = orderUtil.setStopLossPrice(buyOrderEURUSD, newSL);
            }

            @Test
            public void setStopLossPriceCallsOnOrderUtilHandler() {
                assertOrderUtilHandlerCall();
            }

            @Test
            public void setStopLossPriceDoesNotCallOrderUtilHandlerWhenSLAlreadySet() {
                orderUtilForTest.setSL(buyOrderEURUSD, newSL);

                assertNoOrderUtilHandlerCall();
            }
        }
    }
}
