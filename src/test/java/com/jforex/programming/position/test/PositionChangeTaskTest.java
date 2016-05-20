package com.jforex.programming.position.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.atMost;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.aeonbits.owner.ConfigFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.OrderChangeUtil;
import com.jforex.programming.order.call.OrderCallRejectException;
import com.jforex.programming.order.call.OrderSupplierCall;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.event.OrderEventType;
import com.jforex.programming.position.Position;
import com.jforex.programming.position.PositionChangeTask;
import com.jforex.programming.position.PositionFactory;
import com.jforex.programming.position.PositionRetryLogic;
import com.jforex.programming.position.RestoreSLTPPolicy;
import com.jforex.programming.settings.PlatformSettings;
import com.jforex.programming.test.common.InstrumentUtilForTest;
import com.jforex.programming.test.fakes.IOrderForTest;

import de.bechte.junit.runners.context.HierarchicalContextRunner;
import rx.Observable;
import rx.observables.ConnectableObservable;
import rx.observers.TestSubscriber;
import rx.subjects.PublishSubject;
import rx.subjects.Subject;

@RunWith(HierarchicalContextRunner.class)
public class PositionChangeTaskTest extends InstrumentUtilForTest {

    private PositionChangeTask positionChangeTask;

    @Mock
    private PositionFactory positionFactoryMock;
    @Mock
    private OrderChangeUtil orderChangeUtilMock;
    @Mock
    private RestoreSLTPPolicy restoreSLTPPolicyMock;
    @Captor
    private ArgumentCaptor<OrderSupplierCall> orderCallCaptor;
    @Captor
    private ArgumentCaptor<Set<IOrder>> toMergeOrdersCaptor;
    private Position position;
    private final PositionRetryLogic positionRetryLogic = new PositionRetryLogic();
    private final IOrderForTest buyOrder = IOrderForTest.buyOrderEURUSD();
    private final IOrderForTest sellOrder = IOrderForTest.sellOrderEURUSD();
    private final IOrderForTest mergeOrder = IOrderForTest.buyOrderEURUSD();
    private Subject<OrderEvent, OrderEvent> orderEventSubject = PublishSubject.create();
    private final String mergeOrderLabel = "MergeLabel";

    private final static PlatformSettings platformSettings = ConfigFactory.create(PlatformSettings.class);

    @Before
    public void setUp() {
        initCommonTestFramework();

        orderEventSubject = PublishSubject.create();
        position = new Position(instrumentEURUSD, orderEventSubject);
        setUpMocks();
        positionChangeTask = new PositionChangeTask(positionFactoryMock,
                                                    orderChangeUtilMock,
                                                    positionRetryLogic);
    }

    private void setUpMocks() {
        when(positionFactoryMock.forInstrument(instrumentEURUSD)).thenReturn(position);
    }

    private void assertRejectException(final TestSubscriber<?> subscriber) {
        subscriber.assertValueCount(0);
        subscriber.assertError(OrderCallRejectException.class);
    }

    private void assertOrderEventNotify(final TestSubscriber<OrderEvent> subscriber,
                                        final OrderEvent orderEvent) {
        subscriber.assertValueCount(1);
        final OrderEvent receivedOrderEvent = subscriber.getOnNextEvents().get(0);

        assertThat(orderEvent.order(), equalTo(receivedOrderEvent.order()));
        assertThat(orderEvent.type(), equalTo(receivedOrderEvent.type()));
    }

    private void sendOrderEvent(final IOrder order,
                                final OrderEventType orderEventType) {
        orderEventSubject.onNext(new OrderEvent(order, orderEventType));
    }

    public class ClosePositionSetup {

        private final TestSubscriber<OrderEvent> closeSubscriber = new TestSubscriber<>();
        private final Runnable closePositionCall =
                () -> positionChangeTask.closePosition(instrumentEURUSD).subscribe(closeSubscriber);

        @Before
        public void setUp() {
            buyOrder.setState(IOrder.State.FILLED);
            sellOrder.setState(IOrder.State.FILLED);

            position.addOrder(buyOrder);
            position.addOrder(sellOrder);
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
        }

        public class CloseBuyOrderReject {

            private final OrderEvent orderEvent = new OrderEvent(buyOrder, OrderEventType.CLOSE_REJECTED);

            @Before
            public void setUp() {
                when(orderChangeUtilMock.close(buyOrder))
                        .thenReturn(Observable.error(new OrderCallRejectException("", orderEvent)))
                        .thenReturn(Observable.empty());

                closePositionCall.run();
                
                rxTestUtil.advanceTimeBy(platformSettings.delayOnOrderFailRetry(), TimeUnit.MILLISECONDS);
            }

            @Test
            public void testCloseBuyOrderCalledWithOneRetry() {
                verify(orderChangeUtilMock, times(2)).close(buyOrder);
            }

            @Test
            public void testPositionHasStillBuyAndSellOrder() {
                assertTrue(position.contains(buyOrder));
                assertTrue(position.contains(sellOrder));
            }
        }

        public class CloseBuyOrderOK {

            @Before
            public void setUp() {
                final OrderEvent closeBuyEvent = new OrderEvent(buyOrder, OrderEventType.CLOSE_OK);

                final ConnectableObservable<OrderEvent> orderEventObsForBuy =
                        Observable.just(closeBuyEvent).replay();
                orderEventObsForBuy.connect();

                when(orderChangeUtilMock.close(buyOrder)).thenReturn(orderEventObsForBuy);
            }

            public class CloseSellOrderOK {

                @Before
                public void setUp() {
                    final OrderEvent closeSellEvent = new OrderEvent(sellOrder, OrderEventType.CLOSE_OK);

                    final ConnectableObservable<OrderEvent> orderEventObsForSell =
                            Observable.just(closeSellEvent).replay();
                    orderEventObsForSell.connect();

                    when(orderChangeUtilMock.close(sellOrder)).thenReturn(orderEventObsForSell);
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

//                    @Test
//                    public void testPositionIsEmpty() {
//                        assertFalse(position.contains(buyOrder));
//                        assertFalse(position.contains(sellOrder));
//                    }
                }
            }
        }
    }
}
