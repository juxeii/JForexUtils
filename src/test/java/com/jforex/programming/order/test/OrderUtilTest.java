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
import com.jforex.programming.position.PositionMultiTask;
import com.jforex.programming.position.PositionOrders;
import com.jforex.programming.position.PositionSingleTask;
import com.jforex.programming.position.RestoreSLTPData;
import com.jforex.programming.position.RestoreSLTPPolicy;
import com.jforex.programming.test.common.InstrumentUtilForTest;
import com.jforex.programming.test.common.OrderParamsForTest;
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
    private PositionSingleTask positionSingleTaskMock;
    @Mock
    private PositionMultiTask positionMultiTaskMock;
    @Mock
    private RestoreSLTPPolicy restoreSLTPPolicyMock;
    @Mock
    private PositionOrders positionOrdersMock;
    @Mock
    private Position positionMock;
    @Captor
    private ArgumentCaptor<RestoreSLTPData> restoreSLTPCaptor;
    private final TestSubscriber<OrderEvent> orderEventSubscriber = new TestSubscriber<>();
    private final IOrderForTest orderForTest = IOrderForTest.buyOrderEURUSD();
    private final Set<IOrder> toMergeOrders = Sets.newHashSet(IOrderForTest.buyOrderEURUSD(),
                                                              IOrderForTest.sellOrderEURUSD());
    private final String mergeOrderLabel = "MergeLabel";
    private final Observable<OrderEvent> testObservable = Observable.just(null);

    @Before
    public void setUp() {
        setUpMocks();

        orderUtil = new OrderUtil(engineMock,
                                  positionFactoryMock,
                                  orderUtilHandlerMock,
                                  positionSingleTaskMock,
                                  positionMultiTaskMock);
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
        verify(orderUtilHandlerMock).callObservable(any(clazz));
    }

    private void setOrderUtilHandlerMockResult(final Observable<OrderEvent> observable) {
        when(orderUtilHandlerMock.callObservable(any(SubmitCommand.class)))
                .thenReturn(observable);
    }

    private void expectOnOrderUtilHadler(final Class<? extends OrderCallCommand> clazz) {
        when(orderUtilHandlerMock.callObservable(any(clazz)))
                .thenReturn(testObservable);
    }

    @Test
    public void positionOrdersReturnsCorrectInstance() {
        assertThat(orderUtil.positionOrders(instrumentEURUSD),
                   equalTo(positionMock));
    }

    public class SubmitSetup {

        private final OrderParams orderParams = OrderParamsForTest.paramsBuyEURUSD();

        @Test
        public void noOrderIsAddedWhenNoDoneEvent() {
            final OrderEvent submitOKEvent =
                    new OrderEvent(orderForTest, OrderEventType.SUBMIT_OK);
            setOrderUtilHandlerMockResult(doneEventObservable(submitOKEvent));

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
                setOrderUtilHandlerMockResult(doneEventObservable(submitDoneEvent));

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

        @Test
        public void noOrderIsAddedWhenNoDoneEvent() {
            final OrderEvent submitOKEvent =
                    new OrderEvent(orderForTest, OrderEventType.SUBMIT_OK);
            setOrderUtilHandlerMockResult(doneEventObservable(submitOKEvent));

            orderUtil
                    .mergeOrders(mergeOrderLabel, toMergeOrders)
                    .subscribe(orderEventSubscriber);

            verifyZeroInteractions(positionMock);
        }

        public class MergeDone {

            private final OrderEvent mergeDoneEvent =
                    new OrderEvent(orderForTest, OrderEventType.MERGE_OK);

            @Before
            public void setUp() {
                setOrderUtilHandlerMockResult(doneEventObservable(mergeDoneEvent));

                orderUtil
                        .mergeOrders(mergeOrderLabel, toMergeOrders)
                        .subscribe(orderEventSubscriber);
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

        private final double restoreSL = 1.10234;
        private final double restoreTP = 1.11234;
        private final OrderEvent mergeEvent =
                new OrderEvent(orderForTest, OrderEventType.MERGE_OK);
        private final OrderEvent rejectEvent =
                new OrderEvent(orderForTest, OrderEventType.MERGE_REJECTED);
        private final Runnable mergePositionCall =
                () -> orderUtil
                        .mergePositionOrders(mergeOrderLabel,
                                             instrumentEURUSD,
                                             restoreSLTPPolicyMock)
                        .subscribe(orderEventSubscriber);

        private void setOrderUtilHandlerRetryMockResult(final Observable<OrderEvent> observable) {
            when(orderUtilHandlerMock.callWithRetriesObservable(any(MergeCommand.class)))
                    .thenReturn(observable);
        }

        private void setRemoveTPSLMockResult(final Observable<OrderEvent> observable) {
            when(positionMultiTaskMock.removeTPSLObservable(toMergeOrders))
                    .thenReturn(observable);
        }

        private void setRestoreMockResult(final Observable<OrderEvent> observable) {
            when(positionMultiTaskMock
                    .restoreSLTPObservable(eq(orderForTest),
                                           restoreSLTPCaptor.capture())).thenReturn(observable);
        }

        public class NotEnoughOrdersForMerge {

            @Before
            public void setUp() {
                when(positionMock.filled()).thenReturn(Sets.newHashSet());

                orderUtil
                        .mergePositionOrders(mergeOrderLabel,
                                             instrumentEURUSD,
                                             restoreSLTPPolicyMock)
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

            @Test
            public void noCallToPositionMultiTask() {
                verifyZeroInteractions(positionMultiTaskMock);
            }

            @Test
            public void positionOrdersWereNotMarkedAsActive() {
                verify(positionMock, never()).markAllOrders(OrderProcessState.ACTIVE);
            }
        }

        public class RemoveTPSLOKCall {

            @Before
            public void setUp() {
                final RestoreSLTPData restoreSLTPData = new RestoreSLTPData(restoreSL, restoreTP);

                when(restoreSLTPPolicyMock.restoreSL(toMergeOrders))
                        .thenReturn(restoreSLTPData.sl());
                when(restoreSLTPPolicyMock.restoreTP(toMergeOrders))
                        .thenReturn(restoreSLTPData.tp());

                when(positionMock.filled()).thenReturn(toMergeOrders);

                setRemoveTPSLMockResult(doneObservable());
                setOrderUtilHandlerRetryMockResult(busyObservable());
            }

            @Test
            public void allPositionOrdersAreMarkedActive() {
                mergePositionCall.run();

                verify(positionMock).markAllOrders(OrderProcessState.ACTIVE);
            }

            @Test
            public void testSubscriberIsNotCompletedYet() {
                mergePositionCall.run();

                orderEventSubscriber.assertNotCompleted();
            }

            @Test
            public void testRemoveTPSLHasBeenCalledCorrect() {
                mergePositionCall.run();

                verify(positionMultiTaskMock).removeTPSLObservable(toMergeOrders);
            }

            public class MergeOKCall {

                @Test
                public void testOrderHasBeenAddedToPosition() {
                    setOrderUtilHandlerRetryMockResult(doneEventObservable(mergeEvent));

                    mergePositionCall.run();

                    verify(positionMock).addOrder(orderForTest);
                }

                public class RestoreSLTPWithRejection {

                    @Before
                    public void setUp() {
                        setRestoreMockResult(rejectObservable(rejectEvent));
                        setOrderUtilHandlerRetryMockResult(doneEventObservable(mergeEvent));

                        mergePositionCall.run();
                    }

                    @Test
                    public void testRestoreSLTPOnMultiUtilHasBeenCalledWithoutRetry() {
                        verify(positionMultiTaskMock).restoreSLTPObservable(eq(orderForTest),
                                                                            restoreSLTPCaptor
                                                                                    .capture());
                    }

                    @Test
                    public void testSubscriberGetsRejectExceptionNotification() {
                        assertRejectException(orderEventSubscriber);
                    }

                    @Test
                    public void positionOrdersAreMarkedAsIDLE() {
                        verify(positionMock).markAllOrders(OrderProcessState.IDLE);
                    }
                }

                public class RestoreSLTPOKCall {

                    private final OrderEvent restoreTPEvent =
                            new OrderEvent(orderForTest, OrderEventType.CHANGED_TP);

                    @Before
                    public void setUp() {
                        setRestoreMockResult(doneEventObservable(restoreTPEvent));
                        setOrderUtilHandlerRetryMockResult(doneEventObservable(mergeEvent));

                        mergePositionCall.run();
                    }

                    @Test
                    public void testSubscriberCompleted() {
                        orderEventSubscriber.assertCompleted();
                    }

                    @Test
                    public void testSubscriberHasBeenNotifiedWithOrderEvent() {
                        assertOrderEventNotification(restoreTPEvent);
                    }

                    @Test
                    public void positionOrdersAreMarkedAsIDLE() {
                        verify(positionMock).markAllOrders(OrderProcessState.IDLE);
                    }
                }
            }
        }
    }

    public class CloseCompletableSetup {

        private final IOrderForTest buyOrder = IOrderForTest.buyOrderEURUSD();
        private final IOrderForTest sellOrder = IOrderForTest.sellOrderEURUSD();
        private final Set<IOrder> ordersToClose = Sets.newHashSet(buyOrder, sellOrder);

        private final Runnable closeCompletableCall =
                () -> orderUtil
                        .closePosition(instrumentEURUSD)
                        .subscribe(orderEventSubscriber);

        private void setPositionSingleTaskMockResult(final IOrder orderToClose,
                                                     final Observable<OrderEvent> observable) {
            when(positionSingleTaskMock.closeObservable(orderToClose))
                    .thenReturn(observable);
        }

        @Before
        public void setUp() {
            buyOrder.setState(IOrder.State.FILLED);
            sellOrder.setState(IOrder.State.FILLED);
            when(positionMock.filledOrOpened())
                    .thenReturn(ordersToClose);
        }

        @Test
        public void testAllPositionOrdersAreMarkedActive() {
            setPositionSingleTaskMockResult(buyOrder, doneObservable());

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
            public void testNoCallToSingleUtil() {
                verify(positionSingleTaskMock, never()).closeObservable(any());
            }

            @Test
            public void testSubscriberCompleted() {
                orderEventSubscriber.assertCompleted();
            }
        }

        public class CloseBuyOK {

            @Before
            public void setUp() {
                when(positionSingleTaskMock.closeObservable(buyOrder))
                        .thenReturn(doneObservable());
            }

            public class CloseSellOK {

                @Before
                public void setUp() {
                    when(positionSingleTaskMock.closeObservable(sellOrder))
                            .thenReturn(doneObservable());

                    closeCompletableCall.run();
                }

                @Test
                public void testCloseOnSingleTaskForAllOrdersHasBeenCalled() {
                    verify(positionSingleTaskMock).closeObservable(buyOrder);
                    verify(positionSingleTaskMock).closeObservable(sellOrder);
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

        public class SingleTaskCallWithJFException {

            @Before
            public void setUp() {
                when(positionSingleTaskMock.closeObservable(buyOrder))
                        .thenReturn(exceptionObservable());
                when(positionSingleTaskMock.closeObservable(sellOrder))
                        .thenReturn(exceptionObservable());

                closeCompletableCall.run();
            }

            @Test
            public void testCloseOnOrderHasBeenCalledWithoutRetry() {
                verify(positionSingleTaskMock).closeObservable(any());
            }

            @Test
            public void testSubscriberGetsJFExceptionNotification() {
                assertJFException(orderEventSubscriber);
            }

            @Test
            public void positionOrdersAreMarkedAsIDLE() {
                verify(positionMock).markAllOrders(OrderProcessState.IDLE);
            }
        }
    }

    @Test
    public void closeCallsOnOrderUtilHandler() {
        expectOnOrderUtilHadler(CloseCommand.class);

        final Observable<OrderEvent> observable =
                orderUtil.close(orderForTest);

        assertThat(observable, equalTo(testObservable));
    }

    @Test
    public void setLabelCallsOnOrderUtilHandler() {
        final String newLabel = "NewLabel";
        expectOnOrderUtilHadler(SetLabelCommand.class);

        final Observable<OrderEvent> observable =
                orderUtil.setLabel(orderForTest, newLabel);

        assertThat(observable, equalTo(testObservable));
    }

    @Test
    public void setGTTCallsOnOrderUtilHandler() {
        final long newGTT = 123456L;
        expectOnOrderUtilHadler(SetGTTCommand.class);

        final Observable<OrderEvent> observable =
                orderUtil.setGoodTillTime(orderForTest, newGTT);

        assertThat(observable, equalTo(testObservable));
    }

    @Test
    public void setOpenPriceCallsOnOrderUtilHandler() {
        final double newOpenPrice = 1.12122;
        expectOnOrderUtilHadler(SetOpenPriceCommand.class);

        final Observable<OrderEvent> observable =
                orderUtil.setOpenPrice(orderForTest, newOpenPrice);

        assertThat(observable, equalTo(testObservable));
    }

    @Test
    public void setRequestedAmountCallsOnOrderUtilHandler() {
        final double newRequestedAmount = 0.12;
        expectOnOrderUtilHadler(SetAmountCommand.class);

        final Observable<OrderEvent> observable =
                orderUtil.setRequestedAmount(orderForTest, newRequestedAmount);

        assertThat(observable, equalTo(testObservable));
    }

    @Test
    public void setStopLossPriceCallsOnOrderUtilHandler() {
        final double newSL = 1.10987;
        expectOnOrderUtilHadler(SetSLCommand.class);

        final Observable<OrderEvent> observable =
                orderUtil.setStopLossPrice(orderForTest, newSL);

        assertThat(observable, equalTo(testObservable));
    }

    @Test
    public void setTakeProfitPriceCallsOnOrderUtilHandler() {
        final double newTP = 1.11001;
        expectOnOrderUtilHadler(SetTPCommand.class);

        final Observable<OrderEvent> observable =
                orderUtil.setTakeProfitPrice(orderForTest, newTP);

        assertThat(observable, equalTo(testObservable));
    }
}
