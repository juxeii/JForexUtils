package com.jforex.programming.order.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import com.dukascopy.api.IOrder;
import com.dukascopy.api.JFException;
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
import com.jforex.programming.position.PositionOrders;
import com.jforex.programming.position.PositionRemoveTPSLTask;
import com.jforex.programming.position.PositionSingleTask;
import com.jforex.programming.test.common.InstrumentUtilForTest;
import com.jforex.programming.test.fakes.IOrderForTest;

import de.bechte.junit.runners.context.HierarchicalContextRunner;
import rx.Observable;
import rx.observers.TestSubscriber;
import rx.subjects.PublishSubject;
import rx.subjects.Subject;

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
    private PositionRemoveTPSLTask positionRemoveTPSLTaskMock;
    @Mock
    private PositionOrders positionOrdersMock;
    @Mock
    private Position positionMock;
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
                                  positionRemoveTPSLTaskMock);
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

    private void expectOnOrderUtilHandler(final Class<? extends OrderCallCommand> clazz) {
        when(orderUtilHandlerMock.callObservable(any(clazz)))
                .thenReturn(testObservable);
    }

    @Test
    public void positionOrdersReturnsCorrectInstance() {
        assertThat(orderUtil.positionOrders(instrumentEURUSD),
                   equalTo(positionMock));
    }

    @Test
    public void retryObservableWorksCorrect() throws JFException {
        final Subject<OrderEvent, OrderEvent> orderEventSubject = PublishSubject.create();

        final OrderEvent closeRejectEvent =
                new OrderEvent(orderForTest, OrderEventType.CLOSE_REJECTED);
        final OrderEvent closeOKEvent =
                new OrderEvent(orderForTest, OrderEventType.CLOSE_OK);

        when(orderUtilHandlerMock.callObservable(any(CloseCommand.class)))
                .thenReturn(orderEventSubject);

        when(orderUtilHandlerMock.rejectAsErrorObservable(any()))
                .thenReturn(orderEventSubject);

        orderUtil
                .close(orderForTest)
                .flatMap(orderUtil::retryObservable)
                .subscribe(orderEventSubscriber);

        orderEventSubject.onNext(closeRejectEvent);
        orderEventSubject.onNext(closeOKEvent);
        orderEventSubject.onCompleted();

        orderEventSubscriber.assertNoErrors();
        orderEventSubscriber.assertValueCount(1);
    }

    public class SubmitSetup {

        private final OrderParams orderParams = IOrderForTest.paramsBuyEURUSD();

        private void setOrderUtilHandlerMockResult(final Observable<OrderEvent> observable) {
            when(orderUtilHandlerMock.callObservable(any(SubmitCommand.class)))
                    .thenReturn(observable);
        }

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

        private void setOrderUtilHandlerMockResult(final Observable<OrderEvent> observable) {
            when(orderUtilHandlerMock.callObservable(any(MergeCommand.class)))
                    .thenReturn(observable);
        }

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

        private final OrderEvent mergeEvent =
                new OrderEvent(orderForTest, OrderEventType.MERGE_OK);
        private final Runnable mergePositionCall =
                () -> orderUtil
                        .mergePositionOrders(mergeOrderLabel,
                                             instrumentEURUSD)
                        .subscribe(orderEventSubscriber);

        private void setOrderUtilHandlerRetryMockResult(final Observable<OrderEvent> observable) {
            when(orderUtilHandlerMock.callWithRetryObservable(any(MergeCommand.class)))
                    .thenReturn(observable);

            when(orderUtilHandlerMock.callObservable(any(MergeCommand.class)))
                    .thenReturn(observable);
        }

        private void setRemoveTPSLMockResult(final Observable<OrderEvent> observable) {
            when(positionRemoveTPSLTaskMock.observable(toMergeOrders))
                    .thenReturn(observable);
        }

        public class NotEnoughOrdersForMerge {

            @Before
            public void setUp() {
                when(positionMock.filled()).thenReturn(Sets.newHashSet());

                orderUtil
                        .mergePositionOrders(mergeOrderLabel,
                                             instrumentEURUSD)
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
                verifyZeroInteractions(positionRemoveTPSLTaskMock);
            }

            @Test
            public void positionOrdersWereNotMarkedAsActive() {
                verify(positionMock, never()).markAllOrders(OrderProcessState.ACTIVE);
            }
        }

        public class RemoveTPSLOKCall {

            @Before
            public void setUp() {
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

                verify(positionRemoveTPSLTaskMock).observable(toMergeOrders);
            }

            public class MergeOKCall {

                @Before
                public void setUp() {
                    when(orderUtilHandlerMock.rejectAsErrorObservable(any()))
                            .thenReturn(doneEventObservable(mergeEvent));

                    setOrderUtilHandlerRetryMockResult(doneEventObservable(mergeEvent));

                    mergePositionCall.run();
                }

                @Test
                public void testOrderHasBeenAddedToPosition() {
                    verify(positionMock).addOrder(orderForTest);
                }

                @Test
                public void subscriberIsCompleted() {
                    orderEventSubscriber.assertCompleted();
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
        expectOnOrderUtilHandler(CloseCommand.class);

        final Observable<OrderEvent> observable =
                orderUtil.close(orderForTest);

        assertThat(observable, equalTo(testObservable));
    }

    @Test
    public void setLabelCallsOnOrderUtilHandler() {
        final String newLabel = "NewLabel";
        expectOnOrderUtilHandler(SetLabelCommand.class);

        final Observable<OrderEvent> observable =
                orderUtil.setLabel(orderForTest, newLabel);

        assertThat(observable, equalTo(testObservable));
    }

    @Test
    public void setGTTCallsOnOrderUtilHandler() {
        final long newGTT = 123456L;
        expectOnOrderUtilHandler(SetGTTCommand.class);

        final Observable<OrderEvent> observable =
                orderUtil.setGoodTillTime(orderForTest, newGTT);

        assertThat(observable, equalTo(testObservable));
    }

    @Test
    public void setOpenPriceCallsOnOrderUtilHandler() {
        final double newOpenPrice = 1.12122;
        expectOnOrderUtilHandler(SetOpenPriceCommand.class);

        final Observable<OrderEvent> observable =
                orderUtil.setOpenPrice(orderForTest, newOpenPrice);

        assertThat(observable, equalTo(testObservable));
    }

    @Test
    public void setRequestedAmountCallsOnOrderUtilHandler() {
        final double newRequestedAmount = 0.12;
        expectOnOrderUtilHandler(SetAmountCommand.class);

        final Observable<OrderEvent> observable =
                orderUtil.setRequestedAmount(orderForTest, newRequestedAmount);

        assertThat(observable, equalTo(testObservable));
    }

    @Test
    public void setStopLossPriceCallsOnOrderUtilHandler() {
        final double newSL = 1.10987;
        expectOnOrderUtilHandler(SetSLCommand.class);

        final Observable<OrderEvent> observable =
                orderUtil.setStopLossPrice(orderForTest, newSL);

        assertThat(observable, equalTo(testObservable));
    }

    @Test
    public void setTakeProfitPriceCallsOnOrderUtilHandler() {
        final double newTP = 1.11001;
        expectOnOrderUtilHandler(SetTPCommand.class);

        final Observable<OrderEvent> observable =
                orderUtil.setTakeProfitPrice(orderForTest, newTP);

        assertThat(observable, equalTo(testObservable));
    }
}
