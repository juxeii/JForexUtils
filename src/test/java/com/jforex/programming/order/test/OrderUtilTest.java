package com.jforex.programming.order.test;

import java.util.Collection;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import com.dukascopy.api.IOrder;
import com.google.common.collect.Sets;
import com.jforex.programming.order.OrderTaskExecutor;
import com.jforex.programming.order.OrderUtil;
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
public class OrderUtilTest extends InstrumentUtilForTest {

    private OrderUtil orderUtil;

    @Mock
    private OrderTaskExecutor orderTaskExecutorMock;
    @Mock
    private OrderUtilHandler orderUtilHandlerMock;
    @Mock
    private Position positionMock;
    private final IOrder orderForTest = buyOrderEURUSD;
    private TestObserver<OrderEvent> testObserver;

    @Before
    public void setUp() {
        orderUtil = new OrderUtil(orderTaskExecutorMock, orderUtilHandlerMock);
    }

    private void setUpOrderUtilHandlerMock(final Observable<OrderEvent> observable,
                                           final OrderCallReason callReason) {
        when(orderUtilHandlerMock.callObservable(orderForTest, callReason))
            .thenReturn(observable);
    }

    private void verifyOrderUtilHandlerMockCall(final OrderCallReason callReason) {
        verify(orderUtilHandlerMock).callObservable(orderForTest, callReason);
    }

    public class SubmitOrderSetup {

        private Observable<OrderEvent> observable;

        @Before
        public void setUp() {
            when(orderTaskExecutorMock.submitOrder(buyParamsEURUSD)).thenReturn(Single.just(orderForTest));

            observable = orderUtil.submitOrder(buyParamsEURUSD);
        }

        @Test
        public void noCallToOrderUtilHanlder() {
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
            public void submitCompleted() {
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

            observable = orderUtil.mergeOrders(mergeOrderLabel, toMergeOrders);
        }

        @Test
        public void noCallToOrderUtilHanlder() {
            verifyZeroInteractions(orderUtilHandlerMock);
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
            public void submitCompleted() {
                testObserver.assertComplete();
            }
        }
    }

    public class CloseSetup {

        private Observable<OrderEvent> observable;

        @Before
        public void setUp() {
            when(orderTaskExecutorMock.close(orderForTest))
                .thenReturn(emptyCompletable());

            observable = orderUtil.close(orderForTest);
        }

        @Test
        public void noCallToOrderUtilHanlder() {
            verifyZeroInteractions(orderUtilHandlerMock);
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
            public void submitCompleted() {
                testObserver.assertComplete();
            }
        }
    }

    public class SetLabelSetup {

        private Observable<OrderEvent> observable;
        private final String newLabel = "newLabel";

        @Before
        public void setUp() {
            when(orderTaskExecutorMock.setLabel(orderForTest, newLabel))
                .thenReturn(emptyCompletable());

            observable = orderUtil.setLabel(orderForTest, newLabel);
        }

        @Test
        public void noCallToOrderUtilHanlder() {
            verifyZeroInteractions(orderUtilHandlerMock);
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
            public void submitCompleted() {
                testObserver.assertComplete();
            }
        }
    }

    public class SetGTTSetup {

        private Observable<OrderEvent> observable;
        private final long newGTT = 1L;

        @Before
        public void setUp() {
            when(orderTaskExecutorMock.setGoodTillTime(orderForTest, newGTT))
                .thenReturn(emptyCompletable());

            observable = orderUtil.setGoodTillTime(orderForTest, newGTT);
        }

        @Test
        public void noCallToOrderUtilHanlder() {
            verifyZeroInteractions(orderUtilHandlerMock);
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
            public void submitCompleted() {
                testObserver.assertComplete();
            }
        }
    }

    public class SetRequestedAmountSetup {

        private Observable<OrderEvent> observable;
        private final double newRequestedAmount = 0.12;

        @Before
        public void setUp() {
            when(orderTaskExecutorMock.setRequestedAmount(orderForTest, newRequestedAmount))
                .thenReturn(emptyCompletable());

            observable = orderUtil.setRequestedAmount(orderForTest, newRequestedAmount);
        }

        @Test
        public void noCallToOrderUtilHanlder() {
            verifyZeroInteractions(orderUtilHandlerMock);
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
            public void submitCompleted() {
                testObserver.assertComplete();
            }
        }
    }

    public class SetOpenPriceSetup {

        private Observable<OrderEvent> observable;
        private final double newOpenPrice = 1.1234;

        @Before
        public void setUp() {
            when(orderTaskExecutorMock.setOpenPrice(orderForTest, newOpenPrice))
                .thenReturn(emptyCompletable());

            observable = orderUtil.setOpenPrice(orderForTest, newOpenPrice);
        }

        @Test
        public void noCallToOrderUtilHanlder() {
            verifyZeroInteractions(orderUtilHandlerMock);
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
            public void submitCompleted() {
                testObserver.assertComplete();
            }
        }
    }

    public class SetSLSetup {

        private Observable<OrderEvent> observable;
        private final double newSL = 1.1234;

        @Before
        public void setUp() {
            when(orderTaskExecutorMock.setStopLossPrice(orderForTest, newSL))
                .thenReturn(emptyCompletable());

            observable = orderUtil.setStopLossPrice(orderForTest, newSL);
        }

        @Test
        public void noCallToOrderUtilHanlder() {
            verifyZeroInteractions(orderUtilHandlerMock);
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
            public void submitCompleted() {
                testObserver.assertComplete();
            }
        }
    }

    public class SetTPSetup {

        private Observable<OrderEvent> observable;
        private final double newTP = 1.1234;

        @Before
        public void setUp() {
            when(orderTaskExecutorMock.setTakeProfitPrice(orderForTest, newTP))
                .thenReturn(emptyCompletable());

            observable = orderUtil.setTakeProfitPrice(orderForTest, newTP);
        }

        @Test
        public void noCallToOrderUtilHanlder() {
            verifyZeroInteractions(orderUtilHandlerMock);
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
            public void submitCompleted() {
                testObserver.assertComplete();
            }
        }
    }
}
