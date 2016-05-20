package com.jforex.programming.position.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import com.dukascopy.api.IOrder;
import com.google.common.collect.Sets;
import com.jforex.programming.order.call.OrderCallRejectException;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.event.OrderEventType;
import com.jforex.programming.test.fakes.IOrderForTest;

import rx.Observable;
import rx.observables.ConnectableObservable;
import rx.observers.TestSubscriber;

public class PositionCreateTaskTest {

    public class ClosePositionSetup {

        private final TestSubscriber<OrderEvent> closeSubscriber = new TestSubscriber<>();
        private final Runnable closePositionCall =
                () -> positionChangeTask.closePosition(instrumentEURUSD).subscribe(closeSubscriber);

        @Before
        public void setUp() {
            position.addOrder(buyOrder);
            position.addOrder(sellOrder);
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

            }
        }
    }

    public class MergeSetup {

        private final Set<IOrder> toMergeOrders = Sets.newHashSet(buyOrder, sellOrder);
        private final TestSubscriber<OrderEvent> mergeSubscriber = new TestSubscriber<>();
        private final Runnable mergeCall =
                () -> positionChangeTask.mergeOrders(mergeOrderLabel, toMergeOrders).subscribe(mergeSubscriber);

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
            private final Runnable mergePositionCall = () -> positionChangeTask
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
                    () -> positionChangeTask.closePosition(instrumentEURUSD).subscribe(closeSubscriber);

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
