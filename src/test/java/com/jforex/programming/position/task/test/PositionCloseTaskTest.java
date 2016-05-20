package com.jforex.programming.position.task.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.atMost;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.OrderChangeUtil;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.event.OrderEventType;
import com.jforex.programming.position.Position;
import com.jforex.programming.position.PositionFactory;
import com.jforex.programming.position.PositionRetryLogic;
import com.jforex.programming.position.RestoreSLTPPolicy;
import com.jforex.programming.position.task.PositionCloseTask;
import com.jforex.programming.test.common.PositionCommonTest;
import com.jforex.programming.test.fakes.IOrderForTest;

import de.bechte.junit.runners.context.HierarchicalContextRunner;
import rx.Observable;
import rx.observers.TestSubscriber;
import rx.subjects.PublishSubject;
import rx.subjects.Subject;

@RunWith(HierarchicalContextRunner.class)
public class PositionCloseTaskTest extends PositionCommonTest {

    private PositionCloseTask positionCloseTask;

    @Mock
    private PositionFactory positionFactoryMock;
    @Mock
    private OrderChangeUtil orderChangeUtilMock;
    @Mock
    private RestoreSLTPPolicy restoreSLTPPolicyMock;
    private Position position;
    private final PositionRetryLogic positionRetryLogic = new PositionRetryLogic();
    private final IOrderForTest buyOrder = IOrderForTest.buyOrderEURUSD();
    private final IOrderForTest sellOrder = IOrderForTest.sellOrderEURUSD();
    private final Subject<OrderEvent, OrderEvent> orderEventSubject = PublishSubject.create();

    @Before
    public void setUp() {
        initCommonTestFramework();

        position = new Position(instrumentEURUSD, orderEventSubject);
        setUpMocks();
        positionCloseTask = new PositionCloseTask(positionFactoryMock,
                                                  orderChangeUtilMock,
                                                  positionRetryLogic);
    }

    private void setUpMocks() {
        when(positionFactoryMock.forInstrument(instrumentEURUSD)).thenReturn(position);
    }

    private void sendOrderEvent(final IOrder order,
                                final OrderEventType orderEventType) {
        orderEventSubject.onNext(new OrderEvent(order, orderEventType));
    }

    public class ClosePositionSetup {

        private final TestSubscriber<OrderEvent> closeSubscriber = new TestSubscriber<>();
        private final Runnable closePositionCall =
                () -> positionCloseTask.closePosition(instrumentEURUSD).subscribe(closeSubscriber);

        @Before
        public void setUp() {
            buyOrder.setState(IOrder.State.FILLED);
            sellOrder.setState(IOrder.State.FILLED);

            position.addOrder(buyOrder);
            position.addOrder(sellOrder);
        }

        @Test
        public void testSubscriberNotYetCompleted() {
            when(orderChangeUtilMock.close(buyOrder)).thenReturn(Observable.never());

            closePositionCall.run();

            closeSubscriber.assertNotCompleted();
        }

        public class CloseBuyOrderJFException {

            @Before
            public void setUp() {
                when(orderChangeUtilMock.close(buyOrder)).thenReturn(Observable.error(jfException));

                closePositionCall.run();
            }

            @Test
            public void testOneCloseOrderHasBeenCalledWithoutRetry() {
                verify(orderChangeUtilMock, atLeast(1)).close(any());
                verify(orderChangeUtilMock, atMost(2)).close(any());
            }

            @Test
            public void testPositionHasStillBuyAndSellOrder() {
                assertTrue(position.contains(buyOrder));
                assertTrue(position.contains(sellOrder));
            }

            @Test
            public void testSubscriberGetsJFExceptionNotification() {
                assertJFException(closeSubscriber);
            }
        }

        public class CloseBuyOrderRejectWhichExceedsRetries {

            private final OrderEvent orderEvent = new OrderEvent(buyOrder, OrderEventType.CLOSE_REJECTED);

            @Before
            public void setUp() {
                setRetryExceededMock(() -> orderChangeUtilMock.close(buyOrder),
                                     orderEvent);

                closePositionCall.run();

                rxTestUtil.advanceTimeForAllOrderRetries();
            }

            @Test
            public void testCloseBuyOrderCalledWithAllRetries() {
                verify(orderChangeUtilMock, times(retryExceedCount)).close(buyOrder);
            }

            @Test
            public void testPositionHasStillBuyAndSellOrder() {
                assertTrue(position.contains(buyOrder));
                assertTrue(position.contains(sellOrder));
            }

            @Test
            public void testSubscriberGetsRejectExceptionNotification() {
                assertRejectException(closeSubscriber);
            }
        }

        public class CloseBuyOrderRejectWithFullRetriesThenSuccess {

            private final OrderEvent orderEvent = new OrderEvent(buyOrder, OrderEventType.CLOSE_REJECTED);

            @Before
            public void setUp() {
                setFullRetryMock(() -> orderChangeUtilMock.close(buyOrder),
                                 orderEvent);

                closePositionCall.run();

                rxTestUtil.advanceTimeForAllOrderRetries();

                sendOrderEvent(buyOrder, OrderEventType.CLOSE_OK);
            }

            @Test
            public void testCloseBuyOrderCalledWithOneRetry() {
                verify(orderChangeUtilMock, times(retryExceedCount)).close(buyOrder);
            }

            @Test
            public void testPositionHasOnlySellOrder() {
                assertFalse(position.contains(buyOrder));
                assertTrue(position.contains(sellOrder));
            }

            @Test
            public void testSubscriberCompleted() {
                closeSubscriber.assertCompleted();
            }
        }

        public class CloseBuyOrderOK {

            @Before
            public void setUp() {
                final OrderEvent closeBuyEvent = new OrderEvent(buyOrder, OrderEventType.CLOSE_OK);

                when(orderChangeUtilMock.close(buyOrder)).thenReturn(Observable.just(closeBuyEvent));
            }

            public class CloseSellOrderOK {

                @Before
                public void setUp() {
                    final OrderEvent closeSellEvent = new OrderEvent(sellOrder, OrderEventType.CLOSE_OK);

                    when(orderChangeUtilMock.close(sellOrder)).thenReturn(Observable.just(closeSellEvent));
                }

                public class CloseCall {

                    @Before
                    public void setUp() {
                        closePositionCall.run();

                        sendOrderEvent(buyOrder, OrderEventType.CLOSE_OK);
                        sendOrderEvent(sellOrder, OrderEventType.CLOSE_OK);
                    }

                    @Test
                    public void testCloseOnOrdersHaveBeenCalled() {
                        verify(orderChangeUtilMock).close(buyOrder);
                        verify(orderChangeUtilMock).close(sellOrder);
                    }

                    @Test
                    public void testPositionIsEmpty() {
                        assertFalse(position.contains(buyOrder));
                        assertFalse(position.contains(sellOrder));
                    }

                    @Test
                    public void testSubscriberCompleted() {
                        closeSubscriber.assertCompleted();
                    }
                }
            }
        }
    }
}
