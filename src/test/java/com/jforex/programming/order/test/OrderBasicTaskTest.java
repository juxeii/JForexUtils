package com.jforex.programming.order.test;

import java.util.Collection;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import com.dukascopy.api.IOrder;
import com.google.common.collect.Sets;
import com.jforex.programming.order.OrderBasicTask;
import com.jforex.programming.order.OrderTaskExecutor;
import com.jforex.programming.order.OrderUtilHandler;
import com.jforex.programming.order.call.OrderCallReason;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.position.Position;
import com.jforex.programming.test.common.InstrumentUtilForTest;

import de.bechte.junit.runners.context.HierarchicalContextRunner;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.observers.TestObserver;

@RunWith(HierarchicalContextRunner.class)
public class OrderBasicTaskTest extends InstrumentUtilForTest {

    private OrderBasicTask orderBasicTask;

    @Mock
    private OrderTaskExecutor orderTaskExecutorMock;
    @Mock
    private OrderUtilHandler orderUtilHandlerMock;
    @Mock
    private Position positionMock;
    private final IOrder orderForTest = buyOrderEURUSD;
    private Observable<OrderEvent> observable;
    private TestObserver<OrderEvent> testObserver;

    @Before
    public void setUp() {
        orderBasicTask = new OrderBasicTask(orderTaskExecutorMock, orderUtilHandlerMock);
    }

    private void setUpOrderUtilHandlerMock(final Observable<OrderEvent> observable,
                                           final OrderCallReason callReason) {
        when(orderUtilHandlerMock.callObservable(orderForTest, callReason))
            .thenReturn(observable);
    }

    private void verifyOrderUtilHandlerMockCall(final OrderCallReason callReason) {
        verify(orderUtilHandlerMock).callObservable(orderForTest, callReason);
    }

    private void assertValueAlreadySet() {
        testObserver = observable.test();

        testObserver.assertComplete();
        verifyZeroInteractions(orderTaskExecutorMock);
        verifyZeroInteractions(orderUtilHandlerMock);
    }

    public class SubmitOrderSetup {

        @Before
        public void setUp() {
            when(orderTaskExecutorMock.submitOrder(buyParamsEURUSD)).thenReturn(Single.just(orderForTest));

            observable = orderBasicTask.submitOrder(buyParamsEURUSD);
        }

        @Test
        public void callIsDeferred() {
            verifyZeroInteractions(orderTaskExecutorMock);
            verifyZeroInteractions(orderUtilHandlerMock);
        }

        public class OnSubscribe {

            @Before
            public void setUp() {
                setUpOrderUtilHandlerMock(emptyObservable(), OrderCallReason.SUBMIT);

                testObserver = observable.test();
            }

            @Test
            public void orderUtilHandlerIsCalled() {
                verifyOrderUtilHandlerMockCall(OrderCallReason.SUBMIT);
            }

            @Test
            public void subscriberCompletes() {
                testObserver.assertComplete();
            }
        }
    }

    public class MergeOrdersSetup {

        private Observable<OrderEvent> observable;
        private final String mergeOrderLabel = "mergeOrderLabel";
        private final Collection<IOrder> toMergeOrders = Sets.newHashSet(buyOrderEURUSD, sellOrderEURUSD);

        @Before
        public void setUp() {
            when(orderTaskExecutorMock.mergeOrders(mergeOrderLabel, toMergeOrders))
                .thenReturn(Single.just(orderForTest));

            observable = orderBasicTask.mergeOrders(mergeOrderLabel, toMergeOrders);
        }

        @Test
        public void callIsDeferred() {
            verifyZeroInteractions(orderTaskExecutorMock);
            verifyZeroInteractions(orderUtilHandlerMock);
        }

        @Test
        public void withNoOrdersToMergeNoCallToUtilHandler() {
            testObserver = orderBasicTask
                .mergeOrders(mergeOrderLabel, Sets.newHashSet())
                .test();

            verifyZeroInteractions(orderUtilHandlerMock);
            testObserver.assertNoValues();
            testObserver.assertComplete();
        }

        @Test
        public void withOneOrderForMergeNoCallToUtilHandler() {
            testObserver = orderBasicTask
                .mergeOrders(mergeOrderLabel, Sets.newHashSet(buyOrderEURUSD))
                .test();

            verifyZeroInteractions(orderUtilHandlerMock);
            testObserver.assertNoValues();
            testObserver.assertComplete();
        }

        public class OnSubscribe {

            @Before
            public void setUp() {
                setUpOrderUtilHandlerMock(emptyObservable(), OrderCallReason.MERGE);

                testObserver = observable.test();
            }

            @Test
            public void orderUtilHandlerIsCalled() {
                verifyOrderUtilHandlerMockCall(OrderCallReason.MERGE);
            }

            @Test
            public void subscriberCompletes() {
                testObserver.assertComplete();
            }
        }
    }

    public class CloseSetup {

        @Before
        public void setUp() {
            when(orderTaskExecutorMock.close(orderForTest))
                .thenReturn(emptyCompletable());

            observable = orderBasicTask.close(orderForTest);
        }

        @Test
        public void callIsDeferred() {
            verifyZeroInteractions(orderTaskExecutorMock);
            verifyZeroInteractions(orderUtilHandlerMock);
        }

        @Test
        public void completesImmediatelyWhenOrderAlreadyClosed() {
            orderUtilForTest.setState(orderForTest, IOrder.State.CLOSED);

            assertValueAlreadySet();
        }

        public class OnSubscribe {

            @Before
            public void setUp() {
                setUpOrderUtilHandlerMock(emptyObservable(), OrderCallReason.CLOSE);

                testObserver = observable.test();
            }

            @Test
            public void orderUtilHandlerIsCalled() {
                verifyOrderUtilHandlerMockCall(OrderCallReason.CLOSE);
            }

            @Test
            public void subscriberCompletes() {
                testObserver.assertComplete();
            }
        }
    }

    public class SetLabelSetup {

        private final String newLabel = "newLabel";

        @Before
        public void setUp() {
            when(orderTaskExecutorMock.setLabel(orderForTest, newLabel))
                .thenReturn(emptyCompletable());

            observable = orderBasicTask.setLabel(orderForTest, newLabel);
        }

        @Test
        public void callIsDeferred() {
            verifyZeroInteractions(orderTaskExecutorMock);
            verifyZeroInteractions(orderUtilHandlerMock);
        }

        @Test
        public void completesImmediatelyWhenLabelAlreadyClosed() {
            orderUtilForTest.setLabel(orderForTest, newLabel);

            assertValueAlreadySet();
        }

        public class OnSubscribe {

            @Before
            public void setUp() {
                setUpOrderUtilHandlerMock(emptyObservable(), OrderCallReason.CHANGE_LABEL);

                testObserver = observable.test();
            }

            @Test
            public void orderUtilHandlerIsCalled() {
                verifyOrderUtilHandlerMockCall(OrderCallReason.CHANGE_LABEL);
            }

            @Test
            public void subscriberCompletes() {
                testObserver.assertComplete();
            }
        }
    }

    public class SetGTTSetup {

        private final long newGTT = 1L;

        @Before
        public void setUp() {
            when(orderTaskExecutorMock.setGoodTillTime(orderForTest, newGTT))
                .thenReturn(emptyCompletable());

            observable = orderBasicTask.setGoodTillTime(orderForTest, newGTT);
        }

        @Test
        public void callIsDeferred() {
            verifyZeroInteractions(orderTaskExecutorMock);
            verifyZeroInteractions(orderUtilHandlerMock);
        }

        @Test
        public void completesImmediatelyWhenGTTAlreadyClosed() {
            orderUtilForTest.setGTT(orderForTest, newGTT);

            assertValueAlreadySet();
        }

        public class OnSubscribe {

            @Before
            public void setUp() {
                setUpOrderUtilHandlerMock(emptyObservable(), OrderCallReason.CHANGE_GTT);

                testObserver = observable.test();
            }

            @Test
            public void orderUtilHandlerIsCalled() {
                verifyOrderUtilHandlerMockCall(OrderCallReason.CHANGE_GTT);
            }

            @Test
            public void subscriberCompletes() {
                testObserver.assertComplete();
            }
        }
    }

    public class SetRequestedAmountSetup {

        private final double newRequestedAmount = 0.12;

        @Before
        public void setUp() {
            when(orderTaskExecutorMock.setRequestedAmount(orderForTest, newRequestedAmount))
                .thenReturn(emptyCompletable());

            observable = orderBasicTask.setRequestedAmount(orderForTest, newRequestedAmount);
        }

        @Test
        public void callIsDeferred() {
            verifyZeroInteractions(orderTaskExecutorMock);
            verifyZeroInteractions(orderUtilHandlerMock);
        }

        @Test
        public void completesImmediatelyWhenAmountAlreadyClosed() {
            orderUtilForTest.setRequestedAmount(orderForTest, newRequestedAmount);

            assertValueAlreadySet();
        }

        public class OnSubscribe {

            @Before
            public void setUp() {
                setUpOrderUtilHandlerMock(emptyObservable(), OrderCallReason.CHANGE_AMOUNT);

                testObserver = observable.test();
            }

            @Test
            public void orderUtilHandlerIsCalled() {
                verifyOrderUtilHandlerMockCall(OrderCallReason.CHANGE_AMOUNT);
            }

            @Test
            public void subscriberCompletes() {
                testObserver.assertComplete();
            }
        }
    }

    public class SetOpenPriceSetup {

        private final double newOpenPrice = 1.1234;

        @Before
        public void setUp() {
            when(orderTaskExecutorMock.setOpenPrice(orderForTest, newOpenPrice))
                .thenReturn(emptyCompletable());

            observable = orderBasicTask.setOpenPrice(orderForTest, newOpenPrice);
        }

        @Test
        public void callIsDeferred() {
            verifyZeroInteractions(orderTaskExecutorMock);
            verifyZeroInteractions(orderUtilHandlerMock);
        }

        @Test
        public void completesImmediatelyWhenOpenPriceAlreadyClosed() {
            orderUtilForTest.setOpenPrice(orderForTest, newOpenPrice);

            assertValueAlreadySet();
        }

        public class OnSubscribe {

            @Before
            public void setUp() {
                setUpOrderUtilHandlerMock(emptyObservable(), OrderCallReason.CHANGE_PRICE);

                testObserver = observable.test();
            }

            @Test
            public void orderUtilHandlerIsCalled() {
                verifyOrderUtilHandlerMockCall(OrderCallReason.CHANGE_PRICE);
            }

            @Test
            public void subscriberCompletes() {
                testObserver.assertComplete();
            }
        }
    }

    public class SetSLSetup {

        private final double newSL = 1.1234;

        @Before
        public void setUp() {
            when(orderTaskExecutorMock.setStopLossPrice(orderForTest, newSL))
                .thenReturn(emptyCompletable());

            observable = orderBasicTask.setStopLossPrice(orderForTest, newSL);
        }

        @Test
        public void callIsDeferred() {
            verifyZeroInteractions(orderTaskExecutorMock);
            verifyZeroInteractions(orderUtilHandlerMock);
        }

        @Test
        public void completesImmediatelyWhenSLAlreadyClosed() {
            orderUtilForTest.setSL(orderForTest, newSL);

            assertValueAlreadySet();
        }

        public class OnSubscribe {

            @Before
            public void setUp() {
                setUpOrderUtilHandlerMock(emptyObservable(), OrderCallReason.CHANGE_SL);

                testObserver = observable.test();
            }

            @Test
            public void orderUtilHandlerIsCalled() {
                verifyOrderUtilHandlerMockCall(OrderCallReason.CHANGE_SL);
            }

            @Test
            public void subscriberCompletes() {
                testObserver.assertComplete();
            }
        }
    }

    public class SetTPSetup {

        private final double newTP = 1.1234;

        @Before
        public void setUp() {
            when(orderTaskExecutorMock.setTakeProfitPrice(orderForTest, newTP))
                .thenReturn(emptyCompletable());

            observable = orderBasicTask.setTakeProfitPrice(orderForTest, newTP);
        }

        @Test
        public void callIsDeferred() {
            verifyZeroInteractions(orderTaskExecutorMock);
            verifyZeroInteractions(orderUtilHandlerMock);
        }

        @Test
        public void completesImmediatelyWhenTPAlreadyClosed() {
            orderUtilForTest.setTP(orderForTest, newTP);

            assertValueAlreadySet();
        }

        public class OnSubscribe {

            @Before
            public void setUp() {
                setUpOrderUtilHandlerMock(emptyObservable(), OrderCallReason.CHANGE_TP);

                testObserver = observable.test();
            }

            @Test
            public void orderUtilHandlerIsCalled() {
                verifyOrderUtilHandlerMockCall(OrderCallReason.CHANGE_TP);
            }

            @Test
            public void subscriberCompletes() {
                testObserver.assertComplete();
            }
        }
    }
}
