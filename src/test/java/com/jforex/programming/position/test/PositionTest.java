package com.jforex.programming.position.test;

import static com.jforex.programming.misc.JForexUtil.uss;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyDouble;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import com.jforex.programming.order.OrderParams;
import com.jforex.programming.order.OrderUtilObservable;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.event.OrderEventType;
import com.jforex.programming.position.Position;
import com.jforex.programming.position.PositionTaskRejectException;
import com.jforex.programming.position.RestoreSLTPPolicy;
import com.jforex.programming.test.common.InstrumentUtilForTest;
import com.jforex.programming.test.common.OrderParamsForTest;
import com.jforex.programming.test.fakes.IOrderForTest;

import com.dukascopy.api.IOrder;
import com.dukascopy.api.JFException;

import de.bechte.junit.runners.context.HierarchicalContextRunner;
import rx.subjects.PublishSubject;
import rx.subjects.Subject;

@RunWith(HierarchicalContextRunner.class)
public class PositionTest extends InstrumentUtilForTest {

    private Position position;

    @Mock
    private RestoreSLTPPolicy restoreSLTPPolicyMock;
    @Mock
    private OrderUtilObservable orderUtilObservableMock;
    private Subject<IOrder, IOrder> orderSubject;
    private Subject<OrderEvent, OrderEvent> orderEventSubject;
    private OrderParams orderParamsEURUSDBuy;
    private OrderParams orderParamsEURUSDSell;
    private String mergeLabel;
    private final IOrderForTest buyOrderEURUSD = IOrderForTest.buyOrderEURUSD();
    private final IOrderForTest sellOrderEURUSD = IOrderForTest.sellOrderEURUSD();
    private final IOrderForTest mergeOrderEURUSD = IOrderForTest.buyOrderEURUSD();
    private final double restoreSL = 1.12345;
    private final double restoreTP = 1.12543;

    @Before
    public void setUp() throws JFException {
        initCommonTestFramework();

        orderSubject = PublishSubject.create();
        orderEventSubject = PublishSubject.create();
        orderParamsEURUSDBuy = OrderParamsForTest.paramsBuyEURUSD();
        orderParamsEURUSDSell = OrderParamsForTest.paramsSellEURUSD();
        mergeLabel = uss.ORDER_MERGE_LABEL_PREFIX() + orderParamsEURUSDBuy.label();
        mergeOrderEURUSD.setLabel(mergeLabel);
        setUpMocks();

        position = new Position(instrumentEURUSD,
                                orderUtilObservableMock,
                                restoreSLTPPolicyMock);
    }

    private void setUpMocks() {
        when(restoreSLTPPolicyMock.restoreSL(any())).thenReturn(restoreSL);
        when(restoreSLTPPolicyMock.restoreTP(any())).thenReturn(restoreTP);

        when(orderUtilObservableMock.orderEventObservable()).thenReturn(orderEventSubject);
        when(orderUtilObservableMock.submit(orderParamsEURUSDBuy)).thenReturn(orderSubject);
        when(orderUtilObservableMock.submit(orderParamsEURUSDSell)).thenReturn(orderSubject);
        when(orderUtilObservableMock.setSL(eq(buyOrderEURUSD), anyDouble())).thenReturn(orderSubject);
        when(orderUtilObservableMock.setSL(eq(sellOrderEURUSD), anyDouble())).thenReturn(orderSubject);
        when(orderUtilObservableMock.setTP(eq(buyOrderEURUSD), anyDouble())).thenReturn(orderSubject);
        when(orderUtilObservableMock.setTP(eq(sellOrderEURUSD), anyDouble())).thenReturn(orderSubject);
        when(orderUtilObservableMock.merge(eq(mergeLabel), any())).thenReturn(orderSubject);
        when(orderUtilObservableMock.setSL(eq(mergeOrderEURUSD), anyDouble())).thenReturn(orderSubject);
        when(orderUtilObservableMock.setTP(eq(mergeOrderEURUSD), anyDouble())).thenReturn(orderSubject);
        when(orderUtilObservableMock.close(buyOrderEURUSD)).thenReturn(orderSubject);
        when(orderUtilObservableMock.close(sellOrderEURUSD)).thenReturn(orderSubject);
        when(orderUtilObservableMock.close(mergeOrderEURUSD)).thenReturn(orderSubject);
    }

    private boolean isRepositoryEmpty() {
        return position.filter(order -> true).isEmpty();
    }

    private boolean positionHasOrder(final IOrder orderToFind) {
        return position.filter(order -> order.getLabel().equals(orderToFind.getLabel())).size() == 1;
    }

    @Test
    public void testCloseOnEmptyPositionIsIgnored() {
        position.close();
    }

    @Test
    public void testMultipleSubmitCallsAreNotBlocked() {
        position.submit(orderParamsEURUSDBuy);
        position.submit(orderParamsEURUSDBuy);

        verify(orderUtilObservableMock, times(2)).submit(orderParamsEURUSDBuy);
    }

    public class SubmitWithException {

        @Before
        public void setUp() {
            when(orderUtilObservableMock.submit(orderParamsEURUSDBuy)).thenReturn(orderSubject);

            position.submit(orderParamsEURUSDBuy);
        }

        @Test
        public void testSubmitOnOrderUtilIsCalled() {
            verify(orderUtilObservableMock).submit(orderParamsEURUSDBuy);
        }

        @Test
        public void testPositionHasNoOrder() {
            assertTrue(isRepositoryEmpty());
        }
    }

    public class SubmitNoException {

        @Before
        public void setUp() {
            buyOrderEURUSD.setState(IOrder.State.CREATED);

            position.submit(orderParamsEURUSDBuy);

            verify(orderUtilObservableMock).submit(orderParamsEURUSDBuy);
        }

        @Test
        public void testPositionHasNoOrder() {
            assertTrue(isRepositoryEmpty());
        }

        public class SubmitOKMessage {

            @Before
            public void setUp() {
                buyOrderEURUSD.setState(IOrder.State.OPENED);
            }

            public class FillRejectMessage {

                @Before
                public void setUp() {
                    buyOrderEURUSD.setState(IOrder.State.CANCELED);

                    orderSubject.onError(new PositionTaskRejectException("", null));
                }

                @Test
                public void testPositionHasNoOrder() {
                    assertTrue(isRepositoryEmpty());
                }
            }

            public class FullFillMessage {

                @Before
                public void setUp() {
                    buyOrderEURUSD.setState(IOrder.State.FILLED);

                    orderSubject.onNext(buyOrderEURUSD);
                }

                @Test
                public void testPositionHasBuyOrder() {
                    assertTrue(positionHasOrder(buyOrderEURUSD));
                }

                public class CloseOnSL {

                    @Before
                    public void setUp() {
                        buyOrderEURUSD.setState(IOrder.State.CLOSED);

                        orderEventSubject.onNext(new OrderEvent(buyOrderEURUSD, OrderEventType.CLOSED_BY_SL));
                    }

                    @Test
                    public void testPositionHasNoOrder() {
                        assertTrue(isRepositoryEmpty());
                    }
                }

                public class CloseOnTP {

                    @Before
                    public void setUp() {
                        buyOrderEURUSD.setState(IOrder.State.CLOSED);

                        orderEventSubject.onNext(new OrderEvent(buyOrderEURUSD, OrderEventType.CLOSED_BY_TP));
                    }

                    @Test
                    public void testPositionHasNoOrder() {
                        assertTrue(isRepositoryEmpty());
                    }
                }

                public class SecondSubmit {

                    @Before
                    public void setUp() {
                        sellOrderEURUSD.setState(IOrder.State.CREATED);

                        position.submit(orderParamsEURUSDSell);

                        verify(orderUtilObservableMock).submit(orderParamsEURUSDSell);
                    }

                    public class FullFillMessageForSecondOrder {

                        @Before
                        public void setUp() {
                            sellOrderEURUSD.setState(IOrder.State.FILLED);

                            orderSubject.onNext(sellOrderEURUSD);
                        }

                        @Test
                        public void testPositionHasBuyAndSellOrder() {
                            assertTrue(positionHasOrder(buyOrderEURUSD));
                            assertTrue(positionHasOrder(sellOrderEURUSD));
                        }
                    }
                }

                public class MergeCall {

                    @Before
                    public void setUp() {
                        position.merge(mergeLabel);
                    }

                    @Test
                    public void testNoOrderUtilInteractions() {
                        ;
                    }
                }

                public class CloseCall {

                    @Before
                    public void setUp() {
                        position.close();

                        verify(orderUtilObservableMock).close(buyOrderEURUSD);
                    }

                    @Test
                    public void testRepositoryHoldsStillBuyOrder() {
                        assertTrue(positionHasOrder(buyOrderEURUSD));
                    }

                    public class CloseOKMessage {

                        @Before
                        public void setUp() {
                            buyOrderEURUSD.setState(IOrder.State.CLOSED);

                            orderSubject.onNext(buyOrderEURUSD);
                        }

                        @Test
                        public void testPositionHasNoOrder() {
                            assertTrue(isRepositoryEmpty());
                        }
                    }
                }
            }
        }

        public class SubmitRejectMessage {

            @Before
            public void setUp() {
                buyOrderEURUSD.setState(IOrder.State.CANCELED);

                orderSubject.onError(new PositionTaskRejectException("", null));
            }

            @Test
            public void testPositionHasNoOrder() {
                assertTrue(isRepositoryEmpty());
            }
        }
    }
}
