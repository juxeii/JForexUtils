package com.jforex.programming.order.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;

import com.dukascopy.api.IOrder;
import com.google.common.collect.Sets;
import com.jforex.programming.order.OrderChangeUtil;
import com.jforex.programming.order.OrderCreateUtil;
import com.jforex.programming.order.OrderParams;
import com.jforex.programming.order.OrderUtil;
import com.jforex.programming.order.call.OrderCallRejectException;
import com.jforex.programming.order.call.OrderSupplierCall;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.event.OrderEventType;
import com.jforex.programming.position.Position;
import com.jforex.programming.position.PositionFactory;
import com.jforex.programming.test.common.InstrumentUtilForTest;
import com.jforex.programming.test.common.OrderParamsForTest;
import com.jforex.programming.test.fakes.IOrderForTest;

import de.bechte.junit.runners.context.HierarchicalContextRunner;
import rx.Observable;
import rx.observables.ConnectableObservable;
import rx.observers.TestSubscriber;
import rx.subjects.PublishSubject;
import rx.subjects.Subject;

@RunWith(HierarchicalContextRunner.class)
public class OrderUtilTest extends InstrumentUtilForTest {

    private OrderUtil orderUtil;

    @Mock
    private OrderCreateUtil orderCreateUtilMock;
    @Mock
    private OrderChangeUtil orderChangeUtilMock;
    @Mock
    private PositionFactory positionFactoryMock;
    @Captor
    private ArgumentCaptor<OrderSupplierCall> orderCallCaptor;
    @Captor
    private ArgumentCaptor<Set<IOrder>> toMergeOrdersCaptor;
    private Position position;
    private final IOrderForTest buyOrder = IOrderForTest.buyOrderEURUSD();
    private final IOrderForTest sellOrder = IOrderForTest.sellOrderEURUSD();
    private final IOrderForTest mergeOrder = IOrderForTest.buyOrderEURUSD();
    // private final RestoreSLTPPolicy noRestoreSLTPPolicy = new
    // NoRestorePolicy();
    private Subject<OrderEvent, OrderEvent> orderEventSubject = PublishSubject.create();
    private ConnectableObservable<OrderEvent> orderEventObs;
    private final IOrderForTest orderToChange = IOrderForTest.buyOrderEURUSD();
    private final String mergeOrderLabel = "MergeLabel";
//    private final Set<IOrder> toMergeOrders =
//            Sets.newHashSet(IOrderForTest.buyOrderEURUSD(), IOrderForTest.sellOrderEURUSD());
    private final String newLabel = "NewLabel";
    private final long newGTT = 123456L;
    private final double newRequestedAmount = 0.12;
    private final double newOpenPrice = 1.12122;
    private final double newSL = 1.10987;
    private final double newTP = 1.11001;

    @Before
    public void setUp() {
        initCommonTestFramework();

        orderEventSubject = PublishSubject.create();
        position = new Position(instrumentEURUSD, orderEventSubject);
        setUpMocks();
        orderUtil = new OrderUtil(orderCreateUtilMock,
                                  orderChangeUtilMock,
                                  positionFactoryMock);
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

    @Test
    public void testSetLabelCallOrderChangeUtil() {
        final OrderEvent orderEvent = new OrderEvent(orderToChange, OrderEventType.LABEL_CHANGE_OK);
        orderEventObs = Observable.just(orderEvent).replay();
        when(orderChangeUtilMock.setLabel(orderToChange, newLabel)).thenReturn(orderEventObs);

        final Observable<OrderEvent> setLabelObs = orderUtil.setLabel(orderToChange, newLabel);

        verify(orderChangeUtilMock).setLabel(orderToChange, newLabel);
        assertThat(setLabelObs, equalTo(orderEventObs));
    }

    @Test
    public void testSetGTTCallOrderChangeUtil() {
        final OrderEvent orderEvent = new OrderEvent(orderToChange, OrderEventType.GTT_CHANGE_OK);
        orderEventObs = Observable.just(orderEvent).replay();
        when(orderChangeUtilMock.setGoodTillTime(orderToChange, newGTT)).thenReturn(orderEventObs);

        final Observable<OrderEvent> setGTTObs = orderUtil.setGoodTillTime(orderToChange, newGTT);

        verify(orderChangeUtilMock).setGoodTillTime(orderToChange, newGTT);
        assertThat(setGTTObs, equalTo(orderEventObs));
    }

    @Test
    public void testSetOpenPriceCallOrderChangeUtil() {
        final OrderEvent orderEvent = new OrderEvent(orderToChange, OrderEventType.OPENPRICE_CHANGE_OK);
        orderEventObs = Observable.just(orderEvent).replay();
        when(orderChangeUtilMock.setOpenPrice(orderToChange, newOpenPrice)).thenReturn(orderEventObs);

        final Observable<OrderEvent> setGTTObs = orderUtil.setOpenPrice(orderToChange, newOpenPrice);

        verify(orderChangeUtilMock).setOpenPrice(orderToChange, newOpenPrice);
        assertThat(setGTTObs, equalTo(orderEventObs));
    }

    @Test
    public void testSetRequestedAmountCallOrderChangeUtil() {
        final OrderEvent orderEvent = new OrderEvent(orderToChange, OrderEventType.REQUESTED_AMOUNT_CHANGE_OK);
        orderEventObs = Observable.just(orderEvent).replay();
        when(orderChangeUtilMock.setRequestedAmount(orderToChange, newRequestedAmount)).thenReturn(orderEventObs);

        final Observable<OrderEvent> setGTTObs = orderUtil.setRequestedAmount(orderToChange, newRequestedAmount);

        verify(orderChangeUtilMock).setRequestedAmount(orderToChange, newRequestedAmount);
        assertThat(setGTTObs, equalTo(orderEventObs));
    }

    @Test
    public void testSetStopLossPriceCallOrderChangeUtil() {
        final OrderEvent orderEvent = new OrderEvent(orderToChange, OrderEventType.SL_CHANGE_OK);
        orderEventObs = Observable.just(orderEvent).replay();
        when(orderChangeUtilMock.setStopLossPrice(orderToChange, newSL)).thenReturn(orderEventObs);

        final Observable<OrderEvent> setGTTObs = orderUtil.setStopLossPrice(orderToChange, newSL);

        verify(orderChangeUtilMock).setStopLossPrice(orderToChange, newSL);
        assertThat(setGTTObs, equalTo(orderEventObs));
    }

    @Test
    public void testSetTakeProfitPriceCallOrderChangeUtil() {
        final OrderEvent orderEvent = new OrderEvent(orderToChange, OrderEventType.TP_CHANGE_OK);
        orderEventObs = Observable.just(orderEvent).replay();
        when(orderChangeUtilMock.setTakeProfitPrice(orderToChange, newTP)).thenReturn(orderEventObs);

        final Observable<OrderEvent> setGTTObs = orderUtil.setTakeProfitPrice(orderToChange, newTP);

        verify(orderChangeUtilMock).setTakeProfitPrice(orderToChange, newTP);
        assertThat(setGTTObs, equalTo(orderEventObs));
    }

    public class BuySubmitSetup {

        private final OrderParams orderParamsBUY = OrderParamsForTest.paramsBuyEURUSD();
        private final TestSubscriber<OrderEvent> buySubmitSubscriber = new TestSubscriber<>();
        private final Runnable buySubmitCall =
                () -> orderUtil.submitOrder(orderParamsBUY).subscribe(buySubmitSubscriber);

        private void prepareSubmitObservable(final OrderEvent orderEvent) {
            final ConnectableObservable<OrderEvent> observable = Observable.just(orderEvent).replay();
            when(orderCreateUtilMock.submitOrder(orderParamsBUY)).thenReturn(observable);
            observable.connect();
        }

        private void prepareObservableForReject(final OrderEvent orderEvent) {
            final ConnectableObservable observable =
                    Observable.error(new OrderCallRejectException("", orderEvent)).replay();
            when(orderCreateUtilMock.submitOrder(orderParamsBUY)).thenReturn(observable);
            observable.connect();
        }

        public class SubmitCall {

            @Before
            public void setUp() {
                buyOrder.setState(IOrder.State.CREATED);
            }

            public class WhenSubmitRejected {

                private final OrderEvent orderEvent = new OrderEvent(buyOrder, OrderEventType.SUBMIT_REJECTED);

                @Before
                public void setUp() {
                    prepareObservableForReject(orderEvent);

                    buySubmitCall.run();
                }

                @Test
                public void testNoOrderIsAddedToPosition() {
                    assertTrue(position.orders().isEmpty());
                }

                @Test
                public void testBuySubscriberCompletedWithRejection() {
                    assertRejectException(buySubmitSubscriber);
                }

                @Test
                public void testNoRetryIsDone() {
                    verify(orderCreateUtilMock).submitOrder(orderParamsBUY);
                }
            }

            public class WhenFillRejected {

                private final OrderEvent orderEvent = new OrderEvent(buyOrder, OrderEventType.FILL_REJECTED);

                @Before
                public void setUp() {
                    prepareObservableForReject(orderEvent);

                    buySubmitCall.run();
                }

                @Test
                public void testNoOrderIsAddedToPosition() {
                    assertTrue(position.orders().isEmpty());
                }

                @Test
                public void testBuySubscriberCompletedWithRejection() {
                    assertRejectException(buySubmitSubscriber);
                }

                @Test
                public void testNoRetryIsDone() {
                    verify(orderCreateUtilMock).submitOrder(orderParamsBUY);
                }
            }

            public class WhenSubmitOK {

                private final OrderEvent orderEvent = new OrderEvent(buyOrder, OrderEventType.SUBMIT_OK);

                @Before
                public void setUp() {
                    prepareSubmitObservable(orderEvent);

                    buySubmitCall.run();
                }

                @Test
                public void testNoOrderIsAddedToPosition() {
                    assertTrue(position.orders().isEmpty());
                }

                @Test
                public void testBuySubscriberCompletedWithRejection() {
                    assertOrderEventNotify(buySubmitSubscriber, orderEvent);
                }
            }

            public class WhenFilled {

                private final OrderEvent orderEvent = new OrderEvent(buyOrder, OrderEventType.FULL_FILL_OK);

                @Before
                public void setUp() {
                    buyOrder.setState(IOrder.State.FILLED);

                    prepareSubmitObservable(orderEvent);

                    buySubmitCall.run();
                }

                @Test
                public void testBuyOrderIsAddedToPosition() {
                    assertTrue(position.contains(buyOrder));
                }

                @Test
                public void testBuySubscriberIsNotified() {
                    assertOrderEventNotify(buySubmitSubscriber, orderEvent);
                }

                @Test
                public void testBuySubscriberCompleted() {
                    buySubmitSubscriber.assertCompleted();
                }

                public class CloseSetup {

                    private final TestSubscriber<OrderEvent> closeSubscriber = new TestSubscriber<>();
                    private final Runnable closeCall =
                            () -> orderUtil.close(buyOrder).subscribe(closeSubscriber);
                    private final OrderEvent orderEvent = new OrderEvent(buyOrder, OrderEventType.CLOSE_OK);

                    private void prepareCloseObservable(final OrderEvent orderEvent) {
                        setCloseMockObservable(Observable.just(orderEvent).replay());
                    }

                    private void prepareObservableForCloseReject(final OrderEvent orderEvent) {
                        setCloseMockObservable(Observable.error(new OrderCallRejectException("", orderEvent)).replay());
                    }

                    private void setCloseMockObservable(final ConnectableObservable observable) {
                        when(orderChangeUtilMock.close(buyOrder)).thenReturn(observable);
                        observable.connect();
                    }

                    public class WhenClosedRejected {

                        private final OrderEvent orderEvent = new OrderEvent(buyOrder, OrderEventType.CLOSE_REJECTED);

                        @Before
                        public void setUp() {
                            prepareObservableForCloseReject(orderEvent);

                            closeCall.run();
                        }

                        @Test
                        public void testPositionHasStillBuyOrder() {
                            assertTrue(position.contains(buyOrder));
                        }

                        @Test
                        public void testCloseSubscriberCompletedWithRejection() {
                            assertRejectException(closeSubscriber);
                        }

                        @Test
                        public void testNoRetryIsDone() {
                            verify(orderChangeUtilMock).close(buyOrder);
                        }
                    }

                    public class WhenClosed {

                        @Before
                        public void setUp() {
                            prepareCloseObservable(orderEvent);

                            closeCall.run();

                            buyOrder.setState(IOrder.State.CLOSED);
                            sendOrderEvent(buyOrder, OrderEventType.CLOSE_OK);
                        }

                        @Test
                        public void testBuyOrderIsRemovedFromPosition() {
                            assertFalse(position.contains(buyOrder));
                        }

                        @Test
                        public void testCloseSubscriberIsNotified() {
                            assertOrderEventNotify(closeSubscriber, orderEvent);
                        }

                        @Test
                        public void testCloseSubscriberCompleted() {
                            closeSubscriber.assertCompleted();
                        }
                    }
                }
            }
        }
    }

    public class MergeSetup {

        private final Set<IOrder> toMergeOrders = Sets.newHashSet(buyOrder, sellOrder);
        private final TestSubscriber<OrderEvent> mergeSubscriber = new TestSubscriber<>();
        private final Runnable mergeCall =
                () -> orderUtil.mergeOrders(mergeOrderLabel, toMergeOrders).subscribe(mergeSubscriber);

        private void prepareMergeObservable(final OrderEvent orderEvent) {
            setMergeMockObservable(Observable.just(orderEvent).replay());
        }

        private void prepareObservableForMergeReject(final OrderEvent orderEvent) {
            setMergeMockObservable(Observable.error(new OrderCallRejectException("", orderEvent)).replay());
        }

        private void setMergeMockObservable(final ConnectableObservable observable) {
            when(orderCreateUtilMock.mergeOrders(mergeOrderLabel, toMergeOrders)).thenReturn(observable);
            observable.connect();
        }

        public class WhenMergeRejected {

            private final OrderEvent orderEvent = new OrderEvent(mergeOrder, OrderEventType.MERGE_REJECTED);

            @Before
            public void setUp() {
                prepareObservableForMergeReject(orderEvent);

                mergeCall.run();
            }

            @Test
            public void testMergeSubscriberCompletedWithRejection() {
                assertRejectException(mergeSubscriber);
            }

            @Test
            public void testNoRetryIsDone() {
                verify(orderCreateUtilMock).mergeOrders(mergeOrderLabel, toMergeOrders);
            }

            @Test
            public void testPositionHasHasNoOrder() {
                assertFalse(position.contains(mergeOrder));
            }
        }

        public class WhenMerged {

            private final OrderEvent orderEvent = new OrderEvent(mergeOrder, OrderEventType.MERGE_OK);

            @Before
            public void setUp() {
                prepareMergeObservable(orderEvent);

                mergeCall.run();

                mergeOrder.setState(IOrder.State.FILLED);
                sendOrderEvent(mergeOrder, OrderEventType.FULL_FILL_OK);
            }

            @Test
            public void testPositionHasNowMergedOrder() {
                assertTrue(position.contains(mergeOrder));
            }

            @Test
            public void testMergeSubscriberIsNotified() {
                assertOrderEventNotify(mergeSubscriber, orderEvent);
            }

            @Test
            public void testMergeSubscriberCompleted() {
                mergeSubscriber.assertCompleted();
            }
        }
    }

//    public class PositionWithBuyAndSellOrder {
//
//        private final IOrderForTest buyOrder = IOrderForTest.buyOrderEURUSD();
//        private final IOrderForTest sellOrder = IOrderForTest.sellOrderEURUSD();
//
//        @Before
//        public void setUp() {
//            buyOrder.setState(IOrder.State.FILLED);
//            sellOrder.setState(IOrder.State.FILLED);
//
//            position.addOrder(buyOrder);
//            position.addOrder(sellOrder);
//        }
//    }
}
