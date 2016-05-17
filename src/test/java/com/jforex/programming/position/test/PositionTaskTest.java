package com.jforex.programming.position.test;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import com.dukascopy.api.IOrder;
import com.dukascopy.api.JFException;
import com.google.common.collect.Sets;
import com.jforex.programming.misc.CalculationUtil;
import com.jforex.programming.order.OrderParams;
import com.jforex.programming.order.OrderUtil;
import com.jforex.programming.order.call.OrderCallRejectException;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.event.OrderEventType;
import com.jforex.programming.position.PositionTask;
import com.jforex.programming.test.common.InstrumentUtilForTest;
import com.jforex.programming.test.common.OrderParamsForTest;
import com.jforex.programming.test.fakes.IOrderForTest;

import de.bechte.junit.runners.context.HierarchicalContextRunner;
import rx.Observable;
import rx.Subscription;
import rx.observers.TestSubscriber;

@RunWith(HierarchicalContextRunner.class)
public class PositionTaskTest extends InstrumentUtilForTest {

    private PositionTask positionTask;

    @Mock private OrderUtil orderUtilMock;
    private final IOrderForTest buyOrder = IOrderForTest.buyOrderEURUSD();
    private final int noOfRetries = platformSettings.maxRetriesOnOrderFail();
    private final long retryDelay = platformSettings.delayOnOrderFailRetry();

    @Before
    public void setUp() throws JFException {
        initCommonTestFramework();

        positionTask = new PositionTask(instrumentEURUSD, orderUtilMock);
    }

    private OrderCallRejectException createRejectException(final OrderEventType orderEventType) {
        final OrderEvent rejectEvent = new OrderEvent(buyOrder, orderEventType);
        return new OrderCallRejectException("", rejectEvent);
    }

    private Observable<OrderEvent>[] createRejectObsArray(final int noOfRejects,
                                                          final OrderEventType orderEventType) {
        @SuppressWarnings("unchecked")
        final Observable<OrderEvent> rejectObservables[] = new Observable[noOfRejects];
        for (int i = 0; i < noOfRejects; ++i)
            rejectObservables[i] = Observable.error(createRejectException(orderEventType));
        return rejectObservables;
    }

    private void setUpOrderUtilAllRetriesWithSuccess(final Supplier<Observable<OrderEvent>> obs,
                                                     final OrderEventType orderEventType) {
        when(obs.get())
                .thenReturn(Observable.error(createRejectException(orderEventType)),
                            createRejectObsArray(noOfRetries - 1, orderEventType))
                .thenReturn(Observable.empty());
    }

    private void setUpOrderUtilWithMoreRejectsThanRetries(final Supplier<Observable<OrderEvent>> obs,
                                                          final OrderEventType orderEventType) {
        when(obs.get())
                .thenReturn(Observable.error(createRejectException(orderEventType)),
                            createRejectObsArray(noOfRetries, orderEventType))
                .thenReturn(Observable.empty());
    }

    private void setUpJFException(final Supplier<Observable<OrderEvent>> obs) {
        when(obs.get()).thenReturn(Observable.error(jfException));
    }

    private void assertSubscriberCompletes(final TestSubscriber<?> subscriber) {
        subscriber.assertNoErrors();
        subscriber.assertCompleted();
    }

    public class SubmitObservableSetup {

        protected final OrderParams orderParamsBuy = OrderParamsForTest.paramsBuyEURUSD();
        protected TestSubscriber<IOrder> submitSubscriber = new TestSubscriber<>();
        protected Supplier<Subscription> submitSubscriptionSupplier =
                () -> positionTask.submitObservable(orderParamsBuy).subscribe(submitSubscriber);
        protected final Supplier<Observable<OrderEvent>> submitSupplierCall =
                () -> orderUtilMock.submitOrder(orderParamsBuy);

        public class OnSubmitOK {

            @Before
            public void setUp() {
                when(orderUtilMock.submitOrder(orderParamsBuy)).thenReturn(Observable.empty());

                submitSubscriptionSupplier.get();
            }

            @Test
            public void testSubmitOnOrderUtilIsDone() {
                verify(orderUtilMock).submitOrder(orderParamsBuy);
            }

            @Test
            public void testSubscriberCompletes() {
                assertSubscriberCompletes(submitSubscriber);
            }
        }

        public class OnSubmitReject {

            @Before
            public void setUp() {
                when(orderUtilMock.submitOrder(orderParamsBuy))
                        .thenReturn(Observable.error(createRejectException(OrderEventType.SUBMIT_REJECTED)));

                submitSubscriptionSupplier.get();
            }

            @Test
            public void testNoRetryDoneForRejections() {
                verify(orderUtilMock).submitOrder(orderParamsBuy);
            }

            @Test
            public void testSubscriberCompletedWithRejectException() {
                submitSubscriber.assertError(OrderCallRejectException.class);
            }
        }

        public class OnJFException {

            @Before
            public void setUp() {
                setUpJFException(submitSupplierCall);

                submitSubscriptionSupplier.get();
            }

            @Test
            public void testNoRetryDoneForJFExceptions() {
                verify(orderUtilMock).submitOrder(orderParamsBuy);
            }

            @Test
            public void testSubscriberCompletedWithJFException() {
                submitSubscriber.assertError(JFException.class);
            }
        }
    }

    public class MergeObservableSetup {

        protected TestSubscriber<IOrder> mergeSubscriber = new TestSubscriber<>();
        protected String mergeLabel = "MergeLabel";
        protected Supplier<Subscription> mergeSubscriptionSupplier =
                () -> positionTask.mergeObservable(mergeLabel, Sets.newConcurrentHashSet()).subscribe(mergeSubscriber);
        protected final Supplier<Observable<OrderEvent>> mergeSupplierCall =
                () -> orderUtilMock.mergeOrders(mergeLabel, Sets.newConcurrentHashSet());

        public class OnMergeOK {

            @Before
            public void setUp() {
                when(orderUtilMock.mergeOrders(eq(mergeLabel), any())).thenReturn(Observable.empty());

                mergeSubscriptionSupplier.get();
            }

            @Test
            public void testMergeOnOrderUtilIsDone() {
                verify(orderUtilMock).mergeOrders(eq(mergeLabel), any());
            }

            @Test
            public void testSubscriberCompletes() {
                assertSubscriberCompletes(mergeSubscriber);
            }
        }

        public class MergeRejectWithAllRetriesAndSuccess {

            @Before
            public void setUp() {
                setUpOrderUtilAllRetriesWithSuccess(mergeSupplierCall, OrderEventType.MERGE_REJECTED);

                mergeSubscriptionSupplier.get();

                rxTestUtil.advanceTimeBy(retryDelay * noOfRetries, TimeUnit.MILLISECONDS);
            }

            @Test
            public void testRetryCallIsDone() {
                verify(orderUtilMock, times(1 + noOfRetries)).mergeOrders(eq(mergeLabel), any());
            }

            @Test
            public void testSubscriberCompletes() {
                assertSubscriberCompletes(mergeSubscriber);
            }
        }

        public class MergeRejectWithMoreRejectsThanRetries {

            @Before
            public void setUp() {
                setUpOrderUtilWithMoreRejectsThanRetries(mergeSupplierCall, OrderEventType.MERGE_REJECTED);

                mergeSubscriptionSupplier.get();

                rxTestUtil.advanceTimeBy(retryDelay * noOfRetries, TimeUnit.MILLISECONDS);
            }

            @Test
            public void testRetryCallIsDone() {
                verify(orderUtilMock, times(1 + noOfRetries)).mergeOrders(eq(mergeLabel), any());
            }

            @Test
            public void testSubscriberCompletedWithRejectException() {
                mergeSubscriber.assertError(OrderCallRejectException.class);
            }
        }

        public class OnJFException {

            @Before
            public void setUp() {
                setUpJFException(mergeSupplierCall);

                mergeSubscriptionSupplier.get();
            }

            @Test
            public void testNoRetryDoneForJFExceptions() {
                verify(orderUtilMock).mergeOrders(eq(mergeLabel), any());
            }

            @Test
            public void testSubscriberCompletedWithJFException() {
                mergeSubscriber.assertError(JFException.class);
            }
        }
    }

    public class CloseCompletableSetup {

        protected TestSubscriber<?> closeSubscriber = new TestSubscriber<>();
        protected Runnable closeCompletableCall =
                () -> positionTask.closeCompletable(buyOrder).subscribe(closeSubscriber);

        public class WhenOrderIsAlreadyClosed {

            @Before
            public void setUp() {
                buyOrder.setState(IOrder.State.CLOSED);

                closeCompletableCall.run();
            }

            @Test
            public void testNoCallOnOrderUtil() {
                verifyZeroInteractions(orderUtilMock);
            }

            @Test
            public void testSubscriberCompletes() {
                assertSubscriberCompletes(closeSubscriber);
            }
        }

        public class WhenOrderIsNotClosed {

            protected final Supplier<Observable<OrderEvent>> closeSupplierCall =
                    () -> orderUtilMock.close(buyOrder);

            @Before
            public void setUp() {
                buyOrder.setState(IOrder.State.FILLED);
            }

            public class CloseOK {

                @Before
                public void setUp() {
                    when(orderUtilMock.close(buyOrder)).thenReturn(Observable.empty());

                    closeCompletableCall.run();
                }

                @Test
                public void testCloseOnOrderUtilIsCalled() {
                    verify(orderUtilMock).close(buyOrder);
                }

                @Test
                public void testSubscriberCompletes() {
                    assertSubscriberCompletes(closeSubscriber);
                }
            }

            public class CloseRejectWithAllRetriesAndSuccess {

                @Before
                public void setUp() {
                    setUpOrderUtilAllRetriesWithSuccess(closeSupplierCall, OrderEventType.CLOSE_REJECTED);

                    closeCompletableCall.run();

                    rxTestUtil.advanceTimeBy(retryDelay * 3, TimeUnit.MILLISECONDS);
                }

                @Test
                public void testRetryCallIsDone() {
                    verify(orderUtilMock, times(1 + noOfRetries)).close(buyOrder);
                }

                @Test
                public void testSubscriberCompletes() {
                    assertSubscriberCompletes(closeSubscriber);
                }
            }

            public class CloseRejectWithMoreRejectsThanRetries {

                @Before
                public void setUp() {
                    setUpOrderUtilWithMoreRejectsThanRetries(closeSupplierCall, OrderEventType.CLOSE_REJECTED);

                    closeCompletableCall.run();

                    rxTestUtil.advanceTimeBy(retryDelay * noOfRetries, TimeUnit.MILLISECONDS);
                }

                @Test
                public void testRetryCallIsDone() {
                    verify(orderUtilMock, times(1 + noOfRetries)).close(buyOrder);
                }

                @Test
                public void testSubscriberIsCompleted() {
                    closeSubscriber.assertError(OrderCallRejectException.class);
                }
            }

            public class OnJFException {

                @Before
                public void setUp() {
                    setUpJFException(closeSupplierCall);

                    closeCompletableCall.run();
                }

                @Test
                public void testNoRetryDoneForJFExceptions() {
                    verify(orderUtilMock).close(buyOrder);
                }

                @Test
                public void testSubscriberCompletedWithJFException() {
                    closeSubscriber.assertError(JFException.class);
                }
            }
        }
    }

    public class SetSLCompletableSetup {

        protected TestSubscriber<?> setSLSubscriber = new TestSubscriber<>();
        protected final double orderSL = buyOrder.getStopLossPrice();

        @Before
        public void setUp() {
            buyOrder.setState(IOrder.State.FILLED);
        }

        public class WhenToSetSLEqualsOrderSL {

            @Before
            public void setUp() {
                positionTask.setSLCompletable(buyOrder, orderSL).subscribe(setSLSubscriber);
            }

            @Test
            public void testNoCallOnOrderUtil() {
                verifyZeroInteractions(orderUtilMock);
            }

            @Test
            public void testSubscriberCompletes() {
                assertSubscriberCompletes(setSLSubscriber);
            }
        }

        public class WhenToSetSLIsNotEqualOrderSL {

            protected double toSetSL = CalculationUtil.addPips(instrumentEURUSD, orderSL, -5.3);
            protected Runnable setSLCompletableCall =
                    () -> positionTask.setSLCompletable(buyOrder, toSetSL).subscribe(setSLSubscriber);
            protected final Supplier<Observable<OrderEvent>> setSLSupplierCall =
                    () -> orderUtilMock.setStopLossPrice(buyOrder, toSetSL);

            public class SLChangeOK {

                @Before
                public void setUp() {
                    when(orderUtilMock.setStopLossPrice(buyOrder, toSetSL)).thenReturn(Observable.empty());

                    setSLCompletableCall.run();
                }

                @Test
                public void testSetSLOnOrderUtilIsCalled() {
                    verify(orderUtilMock).setStopLossPrice(buyOrder, toSetSL);
                }

                @Test
                public void testSubscriberCompletes() {
                    assertSubscriberCompletes(setSLSubscriber);
                }
            }

            public class SLChangeRejectWithAllRetriesAndSuccess {

                @Before
                public void setUp() {
                    setUpOrderUtilAllRetriesWithSuccess(setSLSupplierCall, OrderEventType.CHANGE_SL_REJECTED);

                    setSLCompletableCall.run();

                    rxTestUtil.advanceTimeBy(retryDelay * noOfRetries, TimeUnit.MILLISECONDS);
                }

                @Test
                public void testRetryCallIsDone() {
                    verify(orderUtilMock, times(1 + noOfRetries)).setStopLossPrice(buyOrder, toSetSL);
                }

                @Test
                public void testSubscriberCompletes() {
                    assertSubscriberCompletes(setSLSubscriber);
                }
            }

            public class SLChangeRejectWithMoreRejectsThanRetries {

                @Before
                public void setUp() {
                    setUpOrderUtilWithMoreRejectsThanRetries(setSLSupplierCall, OrderEventType.CHANGE_SL_REJECTED);

                    setSLCompletableCall.run();

                    rxTestUtil.advanceTimeBy(retryDelay * noOfRetries, TimeUnit.MILLISECONDS);
                }

                @Test
                public void testRetryCallIsDone() {
                    verify(orderUtilMock, times(1 + noOfRetries)).setStopLossPrice(buyOrder, toSetSL);
                }

                @Test
                public void testSubscriberIsCompleted() {
                    setSLSubscriber.assertError(OrderCallRejectException.class);
                }
            }

            public class OnJFException {

                @Before
                public void setUp() {
                    setUpJFException(setSLSupplierCall);

                    setSLCompletableCall.run();
                }

                @Test
                public void testNoRetryDoneForJFExceptions() {
                    verify(orderUtilMock).setStopLossPrice(buyOrder, toSetSL);
                }

                @Test
                public void testSubscriberCompletedWithJFException() {
                    setSLSubscriber.assertError(JFException.class);
                }
            }
        }
    }

    public class SetTPCompletableSetup {

        protected TestSubscriber<?> setTPSubscriber = new TestSubscriber<>();
        protected final double orderTP = buyOrder.getTakeProfitPrice();

        @Before
        public void setUp() {
            buyOrder.setState(IOrder.State.FILLED);
        }

        public class WhenToSetTPEqualsOrderTP {

            @Before
            public void setUp() {
                positionTask.setTPCompletable(buyOrder, orderTP).subscribe(setTPSubscriber);
            }

            @Test
            public void testNoCallOnOrderUtil() {
                verifyZeroInteractions(orderUtilMock);
            }

            @Test
            public void testSubscriberCompletes() {
                assertSubscriberCompletes(setTPSubscriber);
            }
        }

        public class WhenToSetTPIsNotEqualOrderTP {

            protected double toSetTP = CalculationUtil.addPips(instrumentEURUSD, orderTP, 5.3);
            protected Runnable setTPCompletableCall =
                    () -> positionTask.setTPCompletable(buyOrder, toSetTP).subscribe(setTPSubscriber);
            protected final Supplier<Observable<OrderEvent>> setTPSupplierCall =
                    () -> orderUtilMock.setTakeProfitPrice(buyOrder, toSetTP);

            public class TPChangeOK {

                @Before
                public void setUp() {
                    when(orderUtilMock.setTakeProfitPrice(buyOrder, toSetTP)).thenReturn(Observable.empty());

                    setTPCompletableCall.run();
                }

                @Test
                public void testSetTPOnOrderUtilIsCalled() {
                    verify(orderUtilMock).setTakeProfitPrice(buyOrder, toSetTP);
                }

                @Test
                public void testSubscriberCompletes() {
                    assertSubscriberCompletes(setTPSubscriber);
                }
            }

            public class TPChangeRejectWithAllRetriesAndSuccess {

                @Before
                public void setUp() {
                    setUpOrderUtilAllRetriesWithSuccess(setTPSupplierCall, OrderEventType.CHANGE_TP_REJECTED);

                    setTPCompletableCall.run();

                    rxTestUtil.advanceTimeBy(retryDelay * noOfRetries, TimeUnit.MILLISECONDS);
                }

                @Test
                public void testRetryCallIsDone() {
                    verify(orderUtilMock, times(1 + noOfRetries)).setTakeProfitPrice(buyOrder, toSetTP);
                }

                @Test
                public void testSubscriberCompletes() {
                    assertSubscriberCompletes(setTPSubscriber);
                }
            }

            public class TPChangeRejectWithMoreRejectsThanRetries {

                @Before
                public void setUp() {
                    setUpOrderUtilWithMoreRejectsThanRetries(setTPSupplierCall, OrderEventType.CHANGE_TP_REJECTED);

                    setTPCompletableCall.run();

                    rxTestUtil.advanceTimeBy(retryDelay * noOfRetries, TimeUnit.MILLISECONDS);
                }

                @Test
                public void testRetryCallIsDone() {
                    verify(orderUtilMock, times(1 + noOfRetries)).setTakeProfitPrice(buyOrder, toSetTP);
                }

                @Test
                public void testSubscriberCompletedWithRejectException() {
                    setTPSubscriber.assertError(OrderCallRejectException.class);
                }
            }

            public class OnJFException {

                @Before
                public void setUp() {
                    setUpJFException(setTPSupplierCall);

                    setTPCompletableCall.run();
                }

                @Test
                public void testNoRetryDoneForJFExceptions() {
                    verify(orderUtilMock).setTakeProfitPrice(buyOrder, toSetTP);
                }

                @Test
                public void testSubscriberCompletedWithJFException() {
                    setTPSubscriber.assertError(JFException.class);
                }
            }
        }
    }
}
