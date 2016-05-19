package com.jforex.programming.order.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Set;

import org.aeonbits.owner.ConfigFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;

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
import com.jforex.programming.position.RestoreSLTPPolicy;
import com.jforex.programming.settings.PlatformSettings;
import com.jforex.programming.test.common.InstrumentUtilForTest;
import com.jforex.programming.test.common.OrderParamsForTest;
import com.jforex.programming.test.fakes.IOrderForTest;

import com.dukascopy.api.IOrder;

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
    @Mock
    private RestoreSLTPPolicy restoreSLTPPolicyMock;
    @Captor
    private ArgumentCaptor<OrderSupplierCall> orderCallCaptor;
    @Captor
    private ArgumentCaptor<Set<IOrder>> toMergeOrdersCaptor;
    private Position position;
    private final IOrderForTest buyOrder = IOrderForTest.buyOrderEURUSD();
    private final IOrderForTest sellOrder = IOrderForTest.sellOrderEURUSD();
    private final IOrderForTest mergeOrder = IOrderForTest.buyOrderEURUSD();
    private Subject<OrderEvent, OrderEvent> orderEventSubject = PublishSubject.create();
    private ConnectableObservable<OrderEvent> orderEventObs;
    private final IOrderForTest orderToChange = IOrderForTest.buyOrderEURUSD();
    private final String mergeOrderLabel = "MergeLabel";
    private final String newLabel = "NewLabel";
    private final long newGTT = 123456L;
    private final double newRequestedAmount = 0.12;
    private final double newOpenPrice = 1.12122;
    private final double newSL = 1.10987;
    private final double newTP = 1.11001;

    private final static PlatformSettings platformSettings = ConfigFactory.create(PlatformSettings.class);

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

        @SuppressWarnings("unchecked")
        private void prepareObservableForReject(final OrderEvent orderEvent) {
            @SuppressWarnings("rawtypes")
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

                    @SuppressWarnings("unchecked")
                    private void
                            setCloseMockObservable(@SuppressWarnings("rawtypes") final ConnectableObservable observable) {
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

        @SuppressWarnings("unchecked")
        private void setMergeMockObservable(@SuppressWarnings("rawtypes") final ConnectableObservable observable) {
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

    public class PositionWithBuyAndSellOrder {

        private final IOrderForTest buyOrder = IOrderForTest.buyOrderEURUSD();
        private final IOrderForTest sellOrder = IOrderForTest.sellOrderEURUSD();

        @Before
        public void setUp() {
            buyOrder.setState(IOrder.State.FILLED);
            sellOrder.setState(IOrder.State.FILLED);

            position.addOrder(buyOrder);
            position.addOrder(sellOrder);
        }

        public class RemoveTPOK {

            private final TestSubscriber<OrderEvent> mergeSubscriber = new TestSubscriber<>();
            private final Runnable mergePositionCall = () -> orderUtil
                    .mergePositionOrders(mergeOrderLabel,
                                         instrumentEURUSD,
                                         restoreSLTPPolicyMock)
                    .subscribe(mergeSubscriber);
            private final double noTPPrice = platformSettings.noTPPrice();

            @Before
            public void setUp() {
                final OrderEvent removeTPForBuy = new OrderEvent(buyOrder, OrderEventType.TP_CHANGE_OK);
                final OrderEvent removeTPForSell = new OrderEvent(sellOrder, OrderEventType.TP_CHANGE_OK);

                final ConnectableObservable<OrderEvent> orderEventObsForBuy =
                        Observable.just(removeTPForBuy).replay();
                final ConnectableObservable<OrderEvent> orderEventObsForSell =
                        Observable.just(removeTPForSell).replay();

                orderEventObsForBuy.connect();
                orderEventObsForSell.connect();

                when(orderChangeUtilMock.setTakeProfitPrice(buyOrder, noTPPrice)).thenReturn(orderEventObsForBuy);
                when(orderChangeUtilMock.setTakeProfitPrice(sellOrder, noTPPrice)).thenReturn(orderEventObsForSell);
            }

            @Test
            public void testRemoveTPsHaveBeenCalled() {
                mergePositionCall.run();

                verify(orderChangeUtilMock).setTakeProfitPrice(buyOrder, noTPPrice);
                verify(orderChangeUtilMock).setTakeProfitPrice(sellOrder, noTPPrice);
            }

            public class RemoveSLOK {

                private final double noSLPrice = platformSettings.noSLPrice();

                @Before
                public void setUp() {
                    final OrderEvent removeSLForBuy = new OrderEvent(buyOrder, OrderEventType.SL_CHANGE_OK);
                    final OrderEvent removeSLForSell = new OrderEvent(sellOrder, OrderEventType.SL_CHANGE_OK);

                    final ConnectableObservable<OrderEvent> orderEventObsForBuy =
                            Observable.just(removeSLForBuy).replay();
                    final ConnectableObservable<OrderEvent> orderEventObsForSell =
                            Observable.just(removeSLForSell).replay();

                    orderEventObsForBuy.connect();
                    orderEventObsForSell.connect();

                    when(orderChangeUtilMock.setStopLossPrice(buyOrder, noSLPrice))
                            .thenReturn(orderEventObsForBuy);
                    when(orderChangeUtilMock.setStopLossPrice(sellOrder, noSLPrice))
                            .thenReturn(orderEventObsForSell);
                }

                @Test
                public void testRemoveSLsHaveBeenCalled() {
                    mergePositionCall.run();

                    verify(orderChangeUtilMock).setStopLossPrice(buyOrder, noSLPrice);
                    verify(orderChangeUtilMock).setStopLossPrice(sellOrder, noSLPrice);
                }

                public class MergeOK {

                    @Before
                    public void setUp() {
                        final OrderEvent mergeEvent = new OrderEvent(mergeOrder, OrderEventType.MERGE_OK);

                        final ConnectableObservable<OrderEvent> mergeObs = Observable.just(mergeEvent).replay();
                        mergeObs.connect();

                        when(orderCreateUtilMock.mergeOrders(eq(mergeOrderLabel), toMergeOrdersCaptor.capture()))
                                .thenReturn(mergeObs);
                    }

                    @Test
                    public void testMergeHasBeenCalled() {
                        mergePositionCall.run();

                        verify(orderCreateUtilMock).mergeOrders(eq(mergeOrderLabel), toMergeOrdersCaptor.capture());
                    }

                    public class RestoreSLOK {

                        private final double restoreSL = 1.10321;
                        private final double restoreTP = 1.10321;

                        @Before
                        public void setUp() {
                            when(restoreSLTPPolicyMock.restoreSL(any())).thenReturn(restoreSL);
                            when(restoreSLTPPolicyMock.restoreTP(any())).thenReturn(restoreTP);

                            final OrderEvent restoreSLEvent = new OrderEvent(mergeOrder, OrderEventType.SL_CHANGE_OK);

                            final ConnectableObservable<OrderEvent> restoreSLObs =
                                    Observable.just(restoreSLEvent).replay();

                            restoreSLObs.connect();

                            when(orderChangeUtilMock.setStopLossPrice(mergeOrder, restoreSL))
                                    .thenReturn(restoreSLObs);
                        }

                        @Test
                        public void testSetSLOnMergeOrderHasBeenCalled() {
                            mergePositionCall.run();

                            verify(orderChangeUtilMock).setStopLossPrice(mergeOrder, restoreSL);
                        }

                        public class RestoreTPOK {

                            @Before
                            public void setUp() {
                                final OrderEvent restoreTPEvent =
                                        new OrderEvent(mergeOrder, OrderEventType.TP_CHANGE_OK);

                                final ConnectableObservable<OrderEvent> restoreTPObs =
                                        Observable.just(restoreTPEvent).replay();

                                restoreTPObs.connect();

                                when(orderChangeUtilMock.setTakeProfitPrice(mergeOrder, restoreTP))
                                        .thenReturn(restoreTPObs);
                            }

                            @Test
                            public void testSetTPOnMergeOrderHasBeenCalled() {
                                mergePositionCall.run();

                                verify(orderChangeUtilMock).setTakeProfitPrice(mergeOrder, restoreTP);
                            }
                        }
                    }
                }
            }
        }

        public class ClosePositionOK {

            private final TestSubscriber<OrderEvent> closeSubscriber = new TestSubscriber<>();
            private final Runnable closePositionCall =
                    () -> orderUtil.closePosition(instrumentEURUSD).subscribe(closeSubscriber);

            @Before
            public void setUp() {
                final OrderEvent closeBuyEvent = new OrderEvent(buyOrder, OrderEventType.CLOSE_OK);
                final OrderEvent closeSellEvent = new OrderEvent(sellOrder, OrderEventType.CLOSE_OK);

                final ConnectableObservable<OrderEvent> orderEventObsForBuy =
                        Observable.just(closeBuyEvent).replay();
                final ConnectableObservable<OrderEvent> orderEventObsForSell =
                        Observable.just(closeSellEvent).replay();

                orderEventObsForBuy.connect();
                orderEventObsForSell.connect();

                when(orderChangeUtilMock.close(buyOrder)).thenReturn(orderEventObsForBuy);
                when(orderChangeUtilMock.close(sellOrder)).thenReturn(orderEventObsForSell);

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
        }
    }
}
