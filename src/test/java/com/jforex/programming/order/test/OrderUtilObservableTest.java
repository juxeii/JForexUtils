package com.jforex.programming.order.test;

import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.Set;
import java.util.concurrent.Callable;

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
import com.jforex.programming.order.OrderUtilHandler;
import com.jforex.programming.order.OrderUtilObservable;
import com.jforex.programming.order.call.OrderCallCommand;
import com.jforex.programming.order.call.OrderCallReason;
import com.jforex.programming.order.call.OrderCallRejectException;
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
public class OrderUtilObservableTest extends InstrumentUtilForTest {

    private OrderUtilObservable orderUtilObservable;

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

        orderUtilObservable = spy(new OrderUtilObservable(engineMock,
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

    public void verifyAndExecuteCallableOrderUtilMockCall(final OrderCallReason callReason) throws Exception {
        verify(orderUtilHandlerMock).callObservable(callCommandCaptor.capture());

        final OrderCallCommand command = callCommandCaptor.getValue();
        assertThat(command.callReason(), equalTo(callReason));
        final Callable<IOrder> callable = command.callable();
        callable.call();
    }

    private void setOrderUtilHandlerExpectation(final OrderEvent orderEvent) {
        when(orderUtilHandlerMock.callObservable(any()))
            .thenReturn(eventObservable(orderEvent));
    }

    private void assertChangeCallCallsOnOrderUtilHandler(final Observable<OrderEvent> observable) {
        setOrderUtilHandlerExpectation(notificationEvent);
        observable.subscribe(orderEventSubscriber);

        orderEventSubscriber.assertNoErrors();
        orderEventSubscriber.assertValueCount(1);
        orderEventSubscriber.assertCompleted();
        assertThat(orderEventSubscriber.getOnNextEvents().get(0), equalTo(notificationEvent));
    }

    private void assertNoCallOnOrderUtilHandler(final Observable<OrderEvent> observable) {
        observable.subscribe(orderEventSubscriber);

        verify(orderUtilHandlerMock, never()).callObservable(any());
        orderEventSubscriber.assertNoErrors();
        orderEventSubscriber.assertValueCount(0);
        orderEventSubscriber.assertCompleted();
    }

    @Test
    public void positionOrdersReturnsCorrectInstance() {
        assertThat(orderUtilObservable.positionOrders(instrumentEURUSD),
                   equalTo(positionMock));
    }

    public class SubmitSetup {

        private void setOrderUtilHandlerMockResult(final Observable<OrderEvent> observable) {
            when(orderUtilHandlerMock.callObservable(isA(OrderCallCommand.class)))
                .thenReturn(observable);
        }

        public class SubmitAndMergeSetup {

            private Runnable submitAndMergeCall;

            @Before
            public void setUp() {
                submitAndMergeCall = () -> orderUtilObservable
                    .submitAndMergePosition(buyParamsEURUSD, mergeOrderLabel)
                    .subscribe(orderEventSubscriber);
            }

            @Test
            public void theMergeCallIsDeferred() {
                setOrderUtilHandlerMockResult(neverObservable());

                submitAndMergeCall.run();

                verify(orderUtilObservable).submitOrder(buyParamsEURUSD);
                verify(orderUtilObservable, never()).mergeOrders(eq(mergeOrderLabel), any());
            }

            @Test
            public void submitAndMergeAreBothCalledWhenSubmitIsOK() {
                setOrderUtilHandlerMockResult(eventObservable(submitOKEvent));

                submitAndMergeCall.run();

                verify(orderUtilObservable).submitOrder(buyParamsEURUSD);
                verify(orderUtilObservable).mergeOrders(eq(mergeOrderLabel), any());
            }
        }

        @Test
        public void noOrderIsAddedWhenNoDoneEvent() {
            setOrderUtilHandlerMockResult(eventObservable(submitOKEvent));

            orderUtilObservable
                .submitOrder(buyParamsEURUSD)
                .subscribe(orderEventSubscriber);

            verifyZeroInteractions(positionMock);
        }

        @Test
        public void errorIsPropagated() {
            final OrderEvent submitRejectEvent =
                    new OrderEvent(buyOrderEURUSD, OrderEventType.SUBMIT_REJECTED);

            setOrderUtilHandlerMockResult(rejectObservable(submitRejectEvent));
            orderUtilObservable
                .submitOrder(buyParamsEURUSD)
                .subscribe(orderEventSubscriber);

            orderEventSubscriber.assertError(OrderCallRejectException.class);
        }

        public class SubmitDone {

            private final OrderEvent submitDoneEvent =
                    new OrderEvent(buyOrderEURUSD, OrderEventType.FULLY_FILLED);

            @Before
            public void setUp() {
                setOrderUtilHandlerMockResult(eventObservable(submitDoneEvent));

                orderUtilObservable
                    .submitOrder(buyParamsEURUSD)
                    .subscribe(orderEventSubscriber);
            }

            @Test
            public void submitOnOrderUtilHandlerHasBeenCalled() throws Exception {
                verifyAndExecuteCallableOrderUtilMockCall(OrderCallReason.SUBMIT);

                verify(engineMock).submitOrder(buyParamsEURUSD.label(),
                                               buyParamsEURUSD.instrument(),
                                               buyParamsEURUSD.orderCommand(),
                                               buyParamsEURUSD.amount(),
                                               buyParamsEURUSD.price(),
                                               buyParamsEURUSD.slippage(),
                                               buyParamsEURUSD.stopLossPrice(),
                                               buyParamsEURUSD.takeProfitPrice(),
                                               buyParamsEURUSD.goodTillTime(),
                                               buyParamsEURUSD.comment());
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
            when(orderUtilHandlerMock.callObservable(isA(OrderCallCommand.class)))
                .thenReturn(eventObservable(submitOKEvent));

            orderUtilObservable
                .submitAndMergePositionToParams(buyParamsEURUSD, mergeOrderLabel)
                .subscribe(orderEventSubscriber);

            verify(orderUtilObservable).submitAndMergePosition(paramsCaptor.capture(),
                                                               eq(mergeOrderLabel));

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
            mergeCall = () -> orderUtilObservable
                .mergePositionOrders(mergeOrderLabel, instrumentEURUSD)
                .subscribe(orderEventSubscriber);

            setOrderUtilHandlerResult(OrderCallCommand.class,
                                      eventObservable(mergeEvent));
        }

        @Test
        public void positionMergeCallsNormalMerge() {
            final Observable<OrderEvent> expectedObservable = eventObservable(mergeEvent);
            when(positionMock.filled()).thenReturn(toMergeOrders);
            when(orderUtilObservable.mergeOrders(mergeOrderLabel, toMergeOrders))
                .thenReturn(expectedObservable);

            final Observable<OrderEvent> observable = orderUtilObservable
                .mergePositionOrders(mergeOrderLabel, instrumentEURUSD);
            observable.subscribe(orderEventSubscriber);

            verify(orderUtilObservable, times(2)).mergeOrders(mergeOrderLabel, toMergeOrders);
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
                    setOrderUtilHandlerResult(OrderCallCommand.class,
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
                    setOrderUtilHandlerResult(OrderCallCommand.class,
                                              eventObservable(setTPEvent));
                }

                @Test
                public void verifyRemoveTPCalls() {
                    mergeCall.run();

                    verify(orderUtilHandlerMock, times(5))
                        .callObservable(isA(OrderCallCommand.class));
                }

                public class RemoveSLCallFail {

                    private final OrderEvent rejectSLEvent =
                            new OrderEvent(buyOrderEURUSD, OrderEventType.CHANGE_SL_REJECTED);

                    @Before
                    public void setUp() {
                        setOrderUtilHandlerResult(OrderCallCommand.class,
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
                        setOrderUtilHandlerResult(OrderCallCommand.class,
                                                  eventObservable(setSLEvent));
                    }

                    @Test
                    public void verifyRemoveSLCalls() {
                        mergeCall.run();

                        verify(orderUtilHandlerMock, times(5))
                            .callObservable(isA(OrderCallCommand.class));
                    }

                    public class MergeCallFail {

                        private final OrderEvent rejectEvent =
                                new OrderEvent(buyOrderEURUSD, OrderEventType.MERGE_REJECTED);

                        @Before
                        public void setUp() {
                            setOrderUtilHandlerResult(OrderCallCommand.class,
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
                            setOrderUtilHandlerResult(OrderCallCommand.class,
                                                      eventObservable(mergeEvent));

                            mergeCall.run();
                        }

                        @Test
                        public void mergeOnOrderUtilHandlerHasBeenCalled() throws Exception {
                            mergeCall.run();

                            verify(orderUtilHandlerMock, times(6)).callObservable(callCommandCaptor.capture());

                            final OrderCallCommand command = callCommandCaptor.getValue();
                            assertThat(command.callReason(), equalTo(OrderCallReason.MERGE));
                            final Callable<IOrder> callable = command.callable();
                            callable.call();
                            verify(engineMock).mergeOrders(mergeOrderLabel, toMergeOrders);
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
                () -> orderUtilObservable
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
                when(orderUtilHandlerMock.callObservable(isA(OrderCallCommand.class)))
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
                System.out.println(orderEventSubscriber.getOnErrorEvents().get(0).getMessage());
            }

            @Test
            public void positionOrdersAreMarkedAsIDLE() {
                verify(positionMock).markOrdersIdle(ordersToClose);
            }
        }
    }

    public class ChangeCallSetup {

        private Observable<OrderEvent> observable;

        private void assertOrderUtilHandlerCall() {
            assertChangeCallCallsOnOrderUtilHandler(observable);
        }

        private void assertNoOrderUtilHandlerCall() {
            assertNoCallOnOrderUtilHandler(observable);
        }

        private void prepareCallableInvocation(final OrderCallReason orderCallReason) throws Exception {
            setOrderUtilHandlerExpectation(notificationEvent);
            observable.subscribe();

            verifyAndExecuteCallableOrderUtilMockCall(orderCallReason);
        }

        public class CloseSetup {

            private final IOrder.State newState = IOrder.State.CLOSED;

            @Before
            public void setUp() {
                observable = orderUtilObservable.close(buyOrderEURUSD);
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

            @Test
            public void closeOnOrderUtilHandlerHasBeenCalled() throws Exception {
                prepareCallableInvocation(OrderCallReason.CLOSE);

                verify(buyOrderEURUSD).close();
            }
        }

        public class SetLabelSetup {

            private final String newLabel = "newLabel";

            @Before
            public void setUp() {
                observable = orderUtilObservable.setLabel(buyOrderEURUSD, newLabel);
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

            @Test
            public void setLabelOnOrderUtilHandlerHasBeenCalled() throws Exception {
                prepareCallableInvocation(OrderCallReason.CHANGE_LABEL);

                verify(buyOrderEURUSD).setLabel(newLabel);
            }
        }

        public class SetGTTSetup {

            private final Long newGTT = 1234L;

            @Before
            public void setUp() {
                observable = orderUtilObservable.setGoodTillTime(buyOrderEURUSD, newGTT);
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

            @Test
            public void setGTTOnOrderUtilHandlerHasBeenCalled() throws Exception {
                prepareCallableInvocation(OrderCallReason.CHANGE_GTT);

                verify(buyOrderEURUSD).setGoodTillTime(newGTT);
            }
        }

        public class SetAmountSetup {

            private final double newAmount = 0.12;

            @Before
            public void setUp() {
                observable = orderUtilObservable.setRequestedAmount(buyOrderEURUSD, newAmount);
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

            @Test
            public void setAmountOnOrderUtilHandlerHasBeenCalled() throws Exception {
                prepareCallableInvocation(OrderCallReason.CHANGE_AMOUNT);

                verify(buyOrderEURUSD).setRequestedAmount(newAmount);
            }
        }

        public class SetOpenPriceSetup {

            private final double newOpenPrice = 1.1234;

            @Before
            public void setUp() {
                observable = orderUtilObservable.setOpenPrice(buyOrderEURUSD, newOpenPrice);
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

            @Test
            public void setOpenPriceOnOrderUtilHandlerHasBeenCalled() throws Exception {
                prepareCallableInvocation(OrderCallReason.CHANGE_PRICE);

                verify(buyOrderEURUSD).setOpenPrice(newOpenPrice);
            }
        }

        public class SetTPSetup {

            private final double newTP = 1.11001;

            @Before
            public void setUp() {
                observable = orderUtilObservable.setTakeProfitPrice(buyOrderEURUSD, newTP);
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

            @Test
            public void setTPOnOrderUtilHandlerHasBeenCalled() throws Exception {
                prepareCallableInvocation(OrderCallReason.CHANGE_TP);

                verify(buyOrderEURUSD).setTakeProfitPrice(newTP);
            }
        }

        public class SetSLSetup {

            private final double newSL = 1.11001;

            @Before
            public void setUp() {
                observable = orderUtilObservable.setStopLossPrice(buyOrderEURUSD, newSL);
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

            @Test
            public void setSLOnOrderUtilHandlerHasBeenCalled() throws Exception {
                prepareCallableInvocation(OrderCallReason.CHANGE_SL);

                verify(buyOrderEURUSD).setStopLossPrice(newSL);
            }
        }
    }
}
