package com.jforex.programming.position.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import org.aeonbits.owner.ConfigFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import com.google.common.collect.Sets;
import com.jforex.programming.misc.ConcurrentUtil;
import com.jforex.programming.order.OrderParams;
import com.jforex.programming.order.OrderUtil;
import com.jforex.programming.order.call.OrderCallRejectException;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.event.OrderEventType;
import com.jforex.programming.position.Position;
import com.jforex.programming.position.RestoreSLTPPolicy;
import com.jforex.programming.settings.PlatformSettings;
import com.jforex.programming.test.common.InstrumentUtilForTest;
import com.jforex.programming.test.common.OrderParamsForTest;
import com.jforex.programming.test.fakes.IOrderForTest;

import com.dukascopy.api.IOrder;
import com.dukascopy.api.JFException;

import de.bechte.junit.runners.context.HierarchicalContextRunner;
import rx.Observable;
import rx.observers.TestSubscriber;
import rx.subjects.PublishSubject;
import rx.subjects.Subject;

@RunWith(HierarchicalContextRunner.class)
public class PositionTest extends InstrumentUtilForTest {

    private Position position;

    private final static PlatformSettings platformSettings = ConfigFactory.create(PlatformSettings.class);
    @Mock
    private RestoreSLTPPolicy restoreSLTPPolicyMock;
    @Mock
    private OrderUtil orderUtilMock;
    @Mock
    private ConcurrentUtil concurrentUtilMock;
    private final Subject<OrderEvent, OrderEvent> orderEventSubject = PublishSubject.create();
    private final Subject<Long, Long> retryTimerSubject = PublishSubject.create();
    private final OrderParams orderParamsBuy = OrderParamsForTest.paramsBuyEURUSD();
    private final OrderParams orderParamsSell = OrderParamsForTest.paramsSellEURUSD();
    private final IOrderForTest buyOrder = IOrderForTest.buyOrderEURUSD();
    private final IOrderForTest sellOrder = IOrderForTest.sellOrderEURUSD();
    private final String mergeLabel = "MergeLabel";
    private final Set<IOrder> toMergeOrders = Sets.newHashSet(buyOrder, sellOrder);
    private final IOrderForTest mergeOrder = IOrderForTest.buyOrderEURUSD();
    private final double restoreSL = 1.12345;
    private final double restoreTP = 1.12543;
    private final double noSLPrice = platformSettings.noSLPrice();
    private final double noTPPrice = platformSettings.noTPPrice();

    @Before
    public void setUp() throws JFException {
        initCommonTestFramework();

        mergeOrder.setLabel(mergeLabel);
        setUpMocks();

        position = new Position(instrumentEURUSD,
                                orderUtilMock,
                                orderEventSubject,
                                restoreSLTPPolicyMock,
                                concurrentUtilMock);
    }

    private void setUpMocks() {
        when(restoreSLTPPolicyMock.restoreSL(any())).thenReturn(restoreSL);
        when(restoreSLTPPolicyMock.restoreTP(any())).thenReturn(restoreTP);
        when(concurrentUtilMock.timerObservable(1500L, TimeUnit.MILLISECONDS)).thenReturn(retryTimerSubject);
    }

    private Subject<OrderEvent, OrderEvent> setUpSubmit(final OrderParams orderParams) {
        return setUpObservable(() -> orderUtilMock.submitOrder(orderParams));
    }

    private Subject<OrderEvent, OrderEvent> setUpClose(final IOrder order) {
        return setUpObservable(() -> orderUtilMock.close(order));
    }

    private Subject<OrderEvent, OrderEvent> setUpSL(final IOrder order,
                                                    final double newSL) {
        return setUpObservable(() -> orderUtilMock.setStopLossPrice(order, newSL));
    }

    private Subject<OrderEvent, OrderEvent> setUpTP(final IOrder order,
                                                    final double newTP) {
        return setUpObservable(() -> orderUtilMock.setTakeProfitPrice(order, newTP));
    }

    private Subject<OrderEvent, OrderEvent> setUpMerge(final String mergeLabel,
                                                       final Set<IOrder> toMergeOrders) {
        return setUpObservable(() -> orderUtilMock.mergeOrders(eq(mergeLabel), any()));
    }

    private Subject<OrderEvent, OrderEvent> setUpObservable(final Supplier<Observable<OrderEvent>> orderEventSupplier) {
        final Subject<OrderEvent, OrderEvent> orderEventSubject = PublishSubject.create();
        when(orderEventSupplier.get()).thenReturn(orderEventSubject);
        return orderEventSubject;
    }

    private boolean positionHasOrder(final IOrder orderToFind) {
        return position.filter(order -> order.getLabel().equals(orderToFind.getLabel())).size() == 1;
    }

    private boolean isRepositoryEmpty() {
        return position.filter(order -> true).isEmpty();
    }

    private void sendOrderEvent(final Subject<OrderEvent, OrderEvent> subject,
                                final IOrder order,
                                final OrderEventType orderEventType) {
        final OrderEvent orderEvent = new OrderEvent(order, orderEventType);
        subject.onNext(orderEvent);
    }

    private void sendRejectEvent(final Subject<OrderEvent, OrderEvent> subject,
                                 final IOrder order,
                                 final OrderEventType rejectEventType) {
        final OrderEvent rejectEvent = new OrderEvent(order, rejectEventType);
        final OrderCallRejectException rejectException = new OrderCallRejectException("", rejectEvent);
        subject.onError(rejectException);
    }

    private void assertCompletableSubscriber(final TestSubscriber<Long> completableSubscriber) {
        completableSubscriber.assertNoErrors();
        completableSubscriber.assertCompleted();
    }

    @Test
    public void testCloseOnEmptyPositionDoesNotCallOnOrderUtil() {
        position.close();

        verifyZeroInteractions(orderUtilMock);
    }

    @Test
    public void testCloseOnEmptyPositionPublishCloseTaskEvent() {
        final TestSubscriber<Long> completableSubscriber = new TestSubscriber<>();
        position.close().subscribe(completableSubscriber);

        assertCompletableSubscriber(completableSubscriber);
    }

    @Test
    public void testMergeOnEmptyPositionDoesNotCallOnOrderUtil() {
        position.merge(mergeLabel);

        verifyZeroInteractions(orderUtilMock);
    }

    @Test
    public void testMergeOnEmptyPositionPublishMergeTaskEvent() {
        final TestSubscriber<Long> completableSubscriber = new TestSubscriber<>();
        position.merge(mergeLabel).subscribe(completableSubscriber);

        assertCompletableSubscriber(completableSubscriber);
    }

    public class Submit {

        protected Subject<OrderEvent, OrderEvent> buySubmitSubject;
        protected final TestSubscriber<OrderEvent> buySubmitSubscriber = new TestSubscriber<>();
        protected final TestSubscriber<Long> completableBuySubscriber = new TestSubscriber<>();

        @Before
        public void setUp() {
            buyOrder.setState(IOrder.State.CREATED);
            buySubmitSubject = setUpSubmit(orderParamsBuy);

            position.submit(orderParamsBuy).subscribe(completableBuySubscriber);
        }

        @Test
        public void testSubmitIsCalledOnOrderUtil() {
            verify(orderUtilMock).submitOrder(orderParamsBuy);
        }

        @Test
        public void testOnSubmitJFExceptionNoRetryIsDone() {
            buySubmitSubject.onError(jfException);

            verify(orderUtilMock).submitOrder(orderParamsBuy);
        }

        @Test
        public void testCompletableBuyNotYetDone() {
            completableBuySubscriber.assertNotCompleted();
        }

        public class SubmitRejectMessage {

            @Before
            public void setUp() {
                buyOrder.setState(IOrder.State.CANCELED);
                sendRejectEvent(buySubmitSubject, buyOrder, OrderEventType.SUBMIT_REJECTED);
            }

            @Test
            public void testPositionHasNoOrder() {
                assertTrue(isRepositoryEmpty());
            }

            @Test
            public void testNoRetryIsDone() {
                verify(orderUtilMock).submitOrder(orderParamsBuy);
            }

            @Test
            public void testCompletableBuyDone() {
                assertCompletableSubscriber(completableBuySubscriber);
            }
        }

        public class FillRejectMessage {

            @Before
            public void setUp() {
                buyOrder.setState(IOrder.State.CANCELED);
                sendRejectEvent(buySubmitSubject, buyOrder, OrderEventType.FILL_REJECTED);
            }

            @Test
            public void testPositionHasNoOrder() {
                assertTrue(isRepositoryEmpty());
            }

            @Test
            public void testNoRetryIsDone() {
                verify(orderUtilMock).submitOrder(orderParamsBuy);
            }

            @Test
            public void testCompletableBuyDone() {
                assertCompletableSubscriber(completableBuySubscriber);
            }
        }

        public class FillMessage {

            @Before
            public void setUp() {
                buyOrder.setState(IOrder.State.FILLED);
                sendOrderEvent(buySubmitSubject, buyOrder, OrderEventType.FULL_FILL_OK);
                buySubmitSubject.onCompleted();
            }

            @Test
            public void testPositionHasBuyOrder() {
                assertTrue(positionHasOrder(buyOrder));
            }

            @Test
            public void testMergeDoesNotCallOnOrderUtilSinceNothingToMerge() {
                position.merge(mergeLabel);

                verify(orderUtilMock, never()).mergeOrders(mergeLabel, toMergeOrders);
            }

            @Test
            public void testCompletableBuyDone() {
                assertCompletableSubscriber(completableBuySubscriber);
            }

            public class CloseOnSL {

                @Before
                public void setUp() {
                    buyOrder.setState(IOrder.State.CLOSED);

                    sendOrderEvent(orderEventSubject, buyOrder, OrderEventType.CLOSED_BY_SL);
                }

                @Test
                public void testPositionHasNoOrder() {
                    assertTrue(isRepositoryEmpty());
                }
            }

            public class CloseOnTP {

                @Before
                public void setUp() {
                    buyOrder.setState(IOrder.State.CLOSED);

                    sendOrderEvent(orderEventSubject, buyOrder, OrderEventType.CLOSED_BY_TP);
                }

                @Test
                public void testPositionHasNoOrder() {
                    assertTrue(isRepositoryEmpty());
                }
            }

            public class Close {

                protected Subject<OrderEvent, OrderEvent> buyCloseSubject;
                protected final TestSubscriber<Long> completableCloseSubscriber =
                        new TestSubscriber<>();

                @Before
                public void setUp() {
                    buyOrder.setState(IOrder.State.FILLED);
                    buyCloseSubject = setUpClose(buyOrder);

                    position.close().subscribe(completableCloseSubscriber);

                    buyOrder.setState(IOrder.State.CLOSED);
                    sendOrderEvent(buyCloseSubject, buyOrder, OrderEventType.CLOSE_OK);
                    buyCloseSubject.onCompleted();
                }

                @Test
                public void testCloseIsCalledOnOrderUtil() {
                    verify(orderUtilMock).close(buyOrder);
                }

                @Test
                public void testPositionHasNoOrder() {
                    assertTrue(isRepositoryEmpty());
                }

                @Test
                public void testCompletableCloseDone() {
                    assertCompletableSubscriber(completableCloseSubscriber);
                }
            }

            public class SecondSubmit {

                protected Subject<OrderEvent, OrderEvent> sellSubmitSubject;
                protected final TestSubscriber<OrderEvent> sellSubmitSubscriber = new TestSubscriber<>();

                @Before
                public void setUp() {
                    sellOrder.setState(IOrder.State.CREATED);
                    sellSubmitSubject = setUpSubmit(orderParamsSell);

                    position.submit(orderParamsSell);
                }

                @Test
                public void testSubmitIsCalledOnOrderUtil() {
                    verify(orderUtilMock).submitOrder(orderParamsSell);
                }

                public class SecondFillMessage {

                    @Before
                    public void setUp() {
                        sellOrder.setState(IOrder.State.FILLED);
                        sendOrderEvent(sellSubmitSubject, sellOrder, OrderEventType.FULL_FILL_OK);
                        sellSubmitSubject.onCompleted();
                    }

                    @Test
                    public void testPositionHasBuyOrder() {
                        assertTrue(positionHasOrder(sellOrder));
                    }

                    @Test
                    public void testPositionHasBuyAndSellOrder() {
                        assertTrue(positionHasOrder(buyOrder));
                        assertTrue(positionHasOrder(sellOrder));
                    }

                    public class MergeCall {

                        protected Subject<OrderEvent, OrderEvent> buyRemoveTPSubject;
                        protected Subject<OrderEvent, OrderEvent> sellRemoveTPSubject;
                        protected Subject<OrderEvent, OrderEvent> buyRemoveSLSubject;
                        protected Subject<OrderEvent, OrderEvent> sellRemoveSLSubject;
                        protected Subject<OrderEvent, OrderEvent> mergeSubject;
                        protected Subject<OrderEvent, OrderEvent> restoreSLSubject;
                        protected Subject<OrderEvent, OrderEvent> restoreTPSubject;

                        protected final TestSubscriber<Long> completableMergeSubscriber =
                                new TestSubscriber<>();

                        @Before
                        public void setUp() {
                            buyRemoveTPSubject = setUpTP(buyOrder, noTPPrice);
                            sellRemoveTPSubject = setUpTP(sellOrder, noTPPrice);
                            buyRemoveSLSubject = setUpSL(buyOrder, noSLPrice);
                            sellRemoveSLSubject = setUpSL(sellOrder, noSLPrice);
                            mergeSubject = setUpMerge(mergeLabel, toMergeOrders);
                            restoreSLSubject = setUpSL(mergeOrder, restoreSL);
                            restoreTPSubject = setUpTP(mergeOrder, restoreTP);

                            position.merge(mergeLabel).subscribe(completableMergeSubscriber);
                        }

//                        @Test
//                        public void testRemoveTPIsCalled() {
//                            verify(orderUtilMock).setTakeProfitPrice(buyOrder, noTPPrice);
//                            verify(orderUtilMock).setTakeProfitPrice(sellOrder, noTPPrice);
//                        }

                        @Test
                        public void testOnRemoveTPJFExceptionNoRetryIsDone() {
                            sellRemoveTPSubject.onError(jfException);

                            // verify(orderUtilMock).setTakeProfitPrice(sellOrder,
                            // noTPPrice);
                        }

                        @Test
                        public void testCompletableMergeNotYetDone() {
                            completableMergeSubscriber.assertNotCompleted();
                        }

                        public class RemovedTPOnSellRejected {

                            @Before
                            public void setUp() {
                                sendRejectEvent(sellRemoveTPSubject, sellOrder, OrderEventType.CHANGE_TP_REJECTED);

                                retryTimerSubject.onNext(1L);
                            }

//                            @Test
//                            public void testRetryCallOnSellOrderIsDone() {
//                                verify(orderUtilMock, times(2))
//                                        .setTakeProfitPrice(sellOrder, noTPPrice);
//                            }

                            @Test
                            public void testCompletableMergeNotYetDone() {
                                completableMergeSubscriber.assertNotCompleted();
                            }
                        }

                        public class RemovedTPs {

                            @Before
                            public void setUp() {
                                buyOrder.setTakeProfitPrice(noTPPrice);
                                sellOrder.setTakeProfitPrice(noTPPrice);

                                sendOrderEvent(buyRemoveTPSubject, buyOrder, OrderEventType.TP_CHANGE_OK);
                                sendOrderEvent(sellRemoveTPSubject, sellOrder, OrderEventType.TP_CHANGE_OK);
                                buyRemoveTPSubject.onCompleted();
                                sellRemoveTPSubject.onCompleted();
                            }

//                            @Test
//                            public void testRemoveSLIsCalled() {
//                                verify(orderUtilMock).setStopLossPrice(buyOrder, noSLPrice);
//                                verify(orderUtilMock).setStopLossPrice(sellOrder, noSLPrice);
//                            }

                            @Test
                            public void testOnRemoveSLJFExceptionNoRetryIsDone() {
                                sellRemoveSLSubject.onError(jfException);

                                // verify(orderUtilMock).setStopLossPrice(sellOrder,
                                // noSLPrice);
                            }

                            @Test
                            public void testCompletableMergeNotYetDone() {
                                completableMergeSubscriber.assertNotCompleted();
                            }

                            public class RemovedSLs {

                                @Before
                                public void setUp() {
                                    buyOrder.setStopLossPrice(noSLPrice);
                                    sellOrder.setStopLossPrice(noSLPrice);

                                    sendOrderEvent(buyRemoveSLSubject, buyOrder, OrderEventType.SL_CHANGE_OK);
                                    sendOrderEvent(sellRemoveSLSubject, sellOrder, OrderEventType.SL_CHANGE_OK);
                                    buyRemoveSLSubject.onCompleted();
                                    sellRemoveSLSubject.onCompleted();
                                }

                                @Test
                                public void testMergeIsCalled() {
                                    verify(orderUtilMock).mergeOrders(eq(mergeLabel), any());
                                }

                                @Test
                                public void testOnMergeJFExceptionNoRetryIsDone() {
                                    mergeSubject.onError(jfException);

                                    verify(orderUtilMock).mergeOrders(eq(mergeLabel), any());
                                }

                                @Test
                                public void testCompletableMergeNotYetDone() {
                                    completableMergeSubscriber.assertNotCompleted();
                                }

                                public class AfterMergeRejectMessage {

                                    @Before
                                    public void setUp() {
                                        mergeOrder.setState(IOrder.State.CANCELED);

                                        sendRejectEvent(mergeSubject, mergeOrder, OrderEventType.MERGE_REJECTED);
                                        retryTimerSubject.onNext(1L);
                                    }

                                    @Test
                                    public void testPositionHasBuyAndSellOrder() {
                                        assertTrue(positionHasOrder(buyOrder));
                                        assertTrue(positionHasOrder(sellOrder));
                                    }

                                    @Test
                                    public void testRetryCallIsDone() {
                                        // verify(orderUtilMock,
                                        // times(2)).mergeOrders(eq(mergeLabel),
                                        // any());
                                    }

                                    @Test
                                    public void testCompletableMergeNotYetDone() {
                                        completableMergeSubscriber.assertNotCompleted();
                                    }
                                }

                                public class AfterMergeCloseOKMessage {

                                    @Before
                                    public void setUp() {
                                        mergeOrder.setState(IOrder.State.CLOSED);

                                        sendOrderEvent(orderEventSubject, buyOrder, OrderEventType.CLOSED_BY_MERGE);
                                        sendOrderEvent(orderEventSubject, sellOrder, OrderEventType.CLOSED_BY_MERGE);
                                        sendOrderEvent(orderEventSubject, mergeOrder, OrderEventType.MERGE_CLOSE_OK);
                                        mergeSubject.onCompleted();
                                    }

                                    @Test
                                    public void testPositionHasNoOrders() {
                                        assertTrue(isRepositoryEmpty());
                                    }

                                    @Test
                                    public void testCompletableMergeDone() {
                                        assertCompletableSubscriber(completableMergeSubscriber);
                                    }
                                }

                                public class AfterMergeMessage {

                                    @Before
                                    public void setUp() {
                                        buyOrder.setState(IOrder.State.CLOSED);
                                        sellOrder.setState(IOrder.State.CLOSED);
                                        mergeOrder.setState(IOrder.State.FILLED);
                                        mergeOrder.setStopLossPrice(noSLPrice);
                                        mergeOrder.setTakeProfitPrice(noTPPrice);

                                        sendOrderEvent(orderEventSubject, buyOrder, OrderEventType.CLOSED_BY_MERGE);
                                        sendOrderEvent(orderEventSubject, sellOrder, OrderEventType.CLOSED_BY_MERGE);
                                        sendOrderEvent(mergeSubject, mergeOrder, OrderEventType.MERGE_OK);
                                        mergeSubject.onCompleted();
                                    }

                                    @Test
                                    public void testRestoreSLIsCalled() {
                                        verify(orderUtilMock).setStopLossPrice(mergeOrder, restoreSL);
                                    }

                                    @Test
                                    public void testOnRestoreSLJFExceptionNoRetryIsDone() {
                                        restoreSLSubject.onError(jfException);

                                        verify(orderUtilMock).setStopLossPrice(mergeOrder, restoreSL);
                                    }

                                    @Test
                                    public void testCompletableMergeNotYetDone() {
                                        completableMergeSubscriber.assertNotCompleted();
                                    }

                                    @Test
                                    public void testPositionHasMergeOrder() {
                                        assertTrue(positionHasOrder(mergeOrder));
                                    }

                                    public class RestoredSL {

                                        @Before
                                        public void setUp() {
                                            mergeOrder.setStopLossPrice(restoreSL);

                                            sendOrderEvent(restoreSLSubject, mergeOrder, OrderEventType.SL_CHANGE_OK);
                                            restoreSLSubject.onCompleted();
                                        }

                                        @Test
                                        public void testRestoreTPIsCalled() {
                                            verify(orderUtilMock).setTakeProfitPrice(mergeOrder, restoreTP);
                                        }

                                        @Test
                                        public void testOnRestoreTPJFExceptionNoRetryIsDone() {
                                            restoreTPSubject.onError(jfException);

                                            verify(orderUtilMock)
                                                    .setTakeProfitPrice(mergeOrder, restoreTP);
                                        }

                                        @Test
                                        public void testCompletableMergeNotYetDone() {
                                            completableMergeSubscriber.assertNotCompleted();
                                        }

                                        public class RestoredTPMessage {

                                            @Before
                                            public void setUp() {
                                                sendOrderEvent(restoreTPSubject, mergeOrder,
                                                               OrderEventType.TP_CHANGE_OK);
                                                restoreTPSubject.onCompleted();
                                            }

                                            @Test
                                            public void testCompletableMergeDone() {
                                                assertCompletableSubscriber(completableMergeSubscriber);
                                            }
                                        }

                                        public class RestoredTPReject {

                                            @Before
                                            public void setUp() {
                                                sendRejectEvent(restoreTPSubject, mergeOrder,
                                                                OrderEventType.CHANGE_TP_REJECTED);

                                                retryTimerSubject.onNext(1L);
                                            }

                                            @Test
                                            public void testRetryCallOnMergeOrderIsDone() {
                                                verify(orderUtilMock, times(2))
                                                        .setTakeProfitPrice(mergeOrder, restoreTP);
                                            }

                                            @Test
                                            public void testCompletableMergeNotYetDone() {
                                                completableMergeSubscriber.assertNotCompleted();
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    public class ClosePosition {

                        protected Subject<OrderEvent, OrderEvent> buyCloseSubject;
                        protected Subject<OrderEvent, OrderEvent> sellCloseSubject;

                        protected final TestSubscriber<Long> completableCloseSubscriber =
                                new TestSubscriber<>();

                        @Before
                        public void setUp() {
                            buyOrder.setState(IOrder.State.FILLED);
                            sellOrder.setState(IOrder.State.FILLED);
                            buyCloseSubject = setUpClose(buyOrder);
                            sellCloseSubject = setUpClose(sellOrder);

                            position.close().subscribe(completableCloseSubscriber);

                            buyOrder.setState(IOrder.State.CLOSED);
                            sendOrderEvent(buyCloseSubject, buyOrder, OrderEventType.CLOSE_OK);
                            buyCloseSubject.onCompleted();
                        }

                        @Test
                        public void testCloseIsCalledOnOrderUtil() {
                            verify(orderUtilMock).close(buyOrder);
                            verify(orderUtilMock).close(sellOrder);
                        }

                        @Test
                        public void testPositionHasNowOnlySellOrder() {
                            assertFalse(positionHasOrder(buyOrder));
                            assertTrue(positionHasOrder(sellOrder));
                        }

                        @Test
                        public void testOnCloseJFExceptionNoRetryIsDone() {
                            buyCloseSubject.onError(jfException);
                            sellCloseSubject.onError(jfException);

                            verify(orderUtilMock).close(buyOrder);
                        }

                        @Test
                        public void testCompletableNotYetDone() {
                            completableCloseSubscriber.assertNotCompleted();
                        }

                        public class AfterSecondClosePosition {

                            @Before
                            public void setUp() {
                                position.close();
                            }

                            @Test
                            public void testCloseIsNotCalledOnOrderUtilSinceAllOrdersAreMarkedAsActive() {
                                verify(orderUtilMock, times(1)).close(sellOrder);
                            }

                            @Test
                            public void testCompletableNotYetDone() {
                                completableCloseSubscriber.assertNotCompleted();
                            }
                        }

                        public class CloseSellOrderReject {

                            protected Subject<OrderEvent, OrderEvent> sellCloseSubject2;

                            @Before
                            public void setUp() {
                                sellCloseSubject2 = setUpClose(sellOrder);
                                sendRejectEvent(sellCloseSubject, sellOrder, OrderEventType.CLOSE_REJECTED);
                                retryTimerSubject.onNext(1L);
                            }

                            @Test
                            public void testPositionHasOnlySellOrder() {
                                assertFalse(positionHasOrder(buyOrder));
                                assertTrue(positionHasOrder(sellOrder));
                            }

                            @Test
                            public void testRetryCallOnSellOrderIsDone() {
                                verify(orderUtilMock, times(2)).close(sellOrder);
                            }

                            @Test
                            public void testCompletableNotYetDone() {
                                completableCloseSubscriber.assertNotCompleted();
                            }

                            public class CloseSellOrder {

                                @Before
                                public void setUp() {
                                    sellOrder.setState(IOrder.State.CLOSED);

                                    sendOrderEvent(sellCloseSubject2, sellOrder, OrderEventType.CLOSE_OK);
                                    sellCloseSubject2.onCompleted();
                                }

                                @Test
                                public void testPositionHasNoOrder() {
                                    assertTrue(isRepositoryEmpty());
                                }
                            }
                        }

                        public class CloseSellOrder {

                            @Before
                            public void setUp() {
                                sellOrder.setState(IOrder.State.CLOSED);

                                sendOrderEvent(sellCloseSubject, sellOrder, OrderEventType.CLOSE_OK);
                                sellCloseSubject.onCompleted();
                            }

                            @Test
                            public void testPositionHasNoOrder() {
                                assertTrue(isRepositoryEmpty());
                            }

                            @Test
                            public void testCompletableDone() {
                                assertCompletableSubscriber(completableCloseSubscriber);
                            }
                        }
                    }
                }
            }
        }
    }
}
