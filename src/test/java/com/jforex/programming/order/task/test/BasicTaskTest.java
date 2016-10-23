package com.jforex.programming.order.task.test;

import java.util.Collection;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import com.dukascopy.api.IEngine.OrderCommand;
import com.dukascopy.api.IOrder;
import com.google.common.collect.Sets;
import com.jforex.programming.order.OrderParams;
import com.jforex.programming.order.OrderUtilHandler;
import com.jforex.programming.order.call.OrderCallReason;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.task.BasicTask;
import com.jforex.programming.order.task.TaskExecutor;
import com.jforex.programming.position.Position;
import com.jforex.programming.test.common.InstrumentUtilForTest;

import de.bechte.junit.runners.context.HierarchicalContextRunner;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.observers.TestObserver;

@RunWith(HierarchicalContextRunner.class)
public class BasicTaskTest extends InstrumentUtilForTest {

    private BasicTask basicTask;

    @Mock
    private TaskExecutor orderTaskExecutorMock;
    @Mock
    private OrderUtilHandler orderUtilHandlerMock;
    @Mock
    private Position positionMock;
    private final IOrder orderForTest = buyOrderEURUSD;
    private Observable<OrderEvent> observable;
    private TestObserver<OrderEvent> testObserver;
    private final double closeAmount = 0.12;
    private final double closePrice = 1.1234;
    private final double closeSlippage = 5.5;

    @Before
    public void setUp() {
        basicTask = new BasicTask(orderTaskExecutorMock, orderUtilHandlerMock);
    }

    private void setUpOrderUtilHandlerMock(final Observable<OrderEvent> observable,
                                           final OrderCallReason callReason) {
        when(orderUtilHandlerMock.callObservable(orderForTest, callReason))
            .thenReturn(observable);
    }

    private void verifyOrderUtilHandlerMockCall(final OrderCallReason callReason) {
        verify(orderUtilHandlerMock).callObservable(orderForTest, callReason);
    }

    private void assertTaskFilterCausesNoAction() {
        observable
            .test()
            .assertComplete();

        verifyZeroInteractions(orderTaskExecutorMock);
        verifyZeroInteractions(orderUtilHandlerMock);
    }

    public class SubmitOrderSetup {

        @Before
        public void setUp() {
            when(orderTaskExecutorMock.submitOrder(buyParamsEURUSD)).thenReturn(Single.just(orderForTest));

            observable = basicTask.submitOrder(buyParamsEURUSD);
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

    public class ConditionalSubmitOrderSetup {

        @Before
        public void setUp() {
            final OrderParams conditionalParams = buyParamsEURUSD
                .clone()
                .withOrderCommand(OrderCommand.BUYLIMIT)
                .build();

            when(orderTaskExecutorMock.submitOrder(conditionalParams)).thenReturn(Single.just(orderForTest));

            observable = basicTask.submitOrder(conditionalParams);
        }

        @Test
        public void callIsDeferred() {
            verifyZeroInteractions(orderTaskExecutorMock);
            verifyZeroInteractions(orderUtilHandlerMock);
        }

        public class OnSubscribe {

            @Before
            public void setUp() {
                setUpOrderUtilHandlerMock(emptyObservable(), OrderCallReason.SUBMIT_CONDITIONAL);

                testObserver = observable.test();
            }

            @Test
            public void orderUtilHandlerIsCalled() {
                verifyOrderUtilHandlerMockCall(OrderCallReason.SUBMIT_CONDITIONAL);
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

            observable = basicTask.mergeOrders(mergeOrderLabel, toMergeOrders);
        }

        @Test
        public void callIsDeferred() {
            verifyZeroInteractions(orderTaskExecutorMock);
            verifyZeroInteractions(orderUtilHandlerMock);
        }

        @Test
        public void withNoOrdersToMergeNoCallToUtilHandler() {
            basicTask
                .mergeOrders(mergeOrderLabel, Sets.newHashSet())
                .test()
                .assertComplete()
                .assertNoValues();

            verifyZeroInteractions(orderUtilHandlerMock);
        }

        @Test
        public void withOneOrderForMergeNoCallToUtilHandler() {
            basicTask
                .mergeOrders(mergeOrderLabel, Sets.newHashSet(buyOrderEURUSD))
                .test()
                .assertComplete()
                .assertNoValues();

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
            public void subscriberCompletes() {
                testObserver.assertComplete();
            }
        }
    }

    // public class CloseSetup {
    //
    // @Before
    // public void setUp() {
    // when(orderTaskExecutorMock.close(orderForTest))
    // .thenReturn(emptyCompletable());
    //
    // observable = basicTask.close(orderForTest);
    // }
    //
    // @Test
    // public void callIsDeferred() {
    // verifyZeroInteractions(orderTaskExecutorMock);
    // verifyZeroInteractions(orderUtilHandlerMock);
    // }
    //
    // @Test
    // public void completesImmediatelyWhenOrderAlreadyClosed() {
    // orderUtilForTest.setState(orderForTest, IOrder.State.CLOSED);
    //
    // assertTaskFilterCausesNoAction();
    // }
    //
    // public class OnSubscribe {
    //
    // @Before
    // public void setUp() {
    // orderUtilForTest.setState(orderForTest, IOrder.State.FILLED);
    // setUpOrderUtilHandlerMock(emptyObservable(), OrderCallReason.CLOSE);
    //
    // testObserver = observable.test();
    // }
    //
    // @Test
    // public void orderUtilHandlerIsCalled() {
    // verifyOrderUtilHandlerMockCall(OrderCallReason.CLOSE);
    // }
    //
    // @Test
    // public void subscriberCompletes() {
    // testObserver.assertComplete();
    // }
    // }
    // }

    public class SetLabelSetup {

        private final String newLabel = "newLabel";

        @Before
        public void setUp() {
            when(orderTaskExecutorMock.setLabel(orderForTest, newLabel))
                .thenReturn(emptyCompletable());

            observable = basicTask.setLabel(orderForTest, newLabel);
        }

        @Test
        public void callIsDeferred() {
            verifyZeroInteractions(orderTaskExecutorMock);
            verifyZeroInteractions(orderUtilHandlerMock);
        }

        @Test
        public void completesImmediatelyWhenLabelAlreadyClosed() {
            orderUtilForTest.setLabel(orderForTest, newLabel);

            assertTaskFilterCausesNoAction();
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

            observable = basicTask.setGoodTillTime(orderForTest, newGTT);
        }

        @Test
        public void callIsDeferred() {
            verifyZeroInteractions(orderTaskExecutorMock);
            verifyZeroInteractions(orderUtilHandlerMock);
        }

        @Test
        public void completesImmediatelyWhenGTTAlreadyClosed() {
            orderUtilForTest.setGTT(orderForTest, newGTT);

            assertTaskFilterCausesNoAction();
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

            observable = basicTask.setRequestedAmount(orderForTest, newRequestedAmount);
        }

        @Test
        public void callIsDeferred() {
            verifyZeroInteractions(orderTaskExecutorMock);
            verifyZeroInteractions(orderUtilHandlerMock);
        }

        @Test
        public void completesImmediatelyWhenAmountAlreadyClosed() {
            orderUtilForTest.setRequestedAmount(orderForTest, newRequestedAmount);

            assertTaskFilterCausesNoAction();
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

            observable = basicTask.setOpenPrice(orderForTest, newOpenPrice);
        }

        @Test
        public void callIsDeferred() {
            verifyZeroInteractions(orderTaskExecutorMock);
            verifyZeroInteractions(orderUtilHandlerMock);
        }

        @Test
        public void completesImmediatelyWhenOpenPriceAlreadyClosed() {
            orderUtilForTest.setOpenPrice(orderForTest, newOpenPrice);

            assertTaskFilterCausesNoAction();
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

    // public class SetSLSetup {
    //
    // private final double newSL = 1.1234;
    //
    // @Before
    // public void setUp() {
    // when(orderTaskExecutorMock.setStopLossPrice(orderForTest, newSL))
    // .thenReturn(emptyCompletable());
    //
    // observable = basicTask.setStopLossPrice(orderForTest, newSL);
    // }
    //
    // @Test
    // public void callIsDeferred() {
    // verifyZeroInteractions(orderTaskExecutorMock);
    // verifyZeroInteractions(orderUtilHandlerMock);
    // }
    //
    // @Test
    // public void completesImmediatelyWhenSLAlreadyClosed() {
    // orderUtilForTest.setSL(orderForTest, newSL);
    //
    // assertTaskFilterCausesNoAction();
    // }
    //
    // public class OnSubscribe {
    //
    // @Before
    // public void setUp() {
    // orderUtilForTest.setState(orderForTest, IOrder.State.FILLED);
    // setUpOrderUtilHandlerMock(emptyObservable(), OrderCallReason.CHANGE_SL);
    //
    // testObserver = observable.test();
    // }
    //
    // @Test
    // public void orderUtilHandlerIsCalled() {
    // verifyOrderUtilHandlerMockCall(OrderCallReason.CHANGE_SL);
    // }
    //
    // @Test
    // public void subscriberCompletes() {
    // testObserver.assertComplete();
    // }
    // }
    // }

    public class SetTPSetup {

        private final double newTP = 1.1234;

        @Before
        public void setUp() {
            when(orderTaskExecutorMock.setTakeProfitPrice(orderForTest, newTP))
                .thenReturn(emptyCompletable());

            observable = basicTask.setTakeProfitPrice(orderForTest, newTP);
        }

        @Test
        public void callIsDeferred() {
            verifyZeroInteractions(orderTaskExecutorMock);
            verifyZeroInteractions(orderUtilHandlerMock);
        }

        @Test
        public void completesImmediatelyWhenTPAlreadyClosed() {
            orderUtilForTest.setTP(orderForTest, newTP);

            assertTaskFilterCausesNoAction();
        }

        public class OnSubscribe {

            @Before
            public void setUp() {
                orderUtilForTest.setState(orderForTest, IOrder.State.FILLED);
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
