package com.jforex.programming.position.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.Set;

import org.aeonbits.owner.ConfigFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;

import com.dukascopy.api.IOrder;
import com.dukascopy.api.JFException;
import com.jforex.programming.order.OrderParams;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.event.OrderEventType;
import com.jforex.programming.position.Position;
import com.jforex.programming.position.PositionTask;
import com.jforex.programming.position.RestoreSLTPPolicy;
import com.jforex.programming.settings.PlatformSettings;
import com.jforex.programming.test.common.InstrumentUtilForTest;
import com.jforex.programming.test.common.OrderParamsForTest;
import com.jforex.programming.test.fakes.IOrderForTest;

import de.bechte.junit.runners.context.HierarchicalContextRunner;
import rx.Completable;
import rx.Observable;
import rx.observers.TestSubscriber;
import rx.subjects.PublishSubject;
import rx.subjects.Subject;

@RunWith(HierarchicalContextRunner.class)
public class PositionTest extends InstrumentUtilForTest {

    private Position position;

    @Mock
    private RestoreSLTPPolicy restoreSLTPPolicyMock;
    @Mock
    private PositionTask positionTaskMock;
    @Captor
    private ArgumentCaptor<Set<IOrder>> toMergeOrdersCaptor;
    private final Subject<OrderEvent, OrderEvent> orderEventSubject = PublishSubject.create();
    private final IOrderForTest buyOrder = IOrderForTest.buyOrderEURUSD();
    private final IOrderForTest sellOrder = IOrderForTest.sellOrderEURUSD();

    private final static PlatformSettings platformSettings = ConfigFactory.create(PlatformSettings.class);

    @Before
    public void setUp() throws JFException {
        initCommonTestFramework();

        position = new Position(instrumentEURUSD,
                                positionTaskMock,
                                orderEventSubject);
    }

    private void sendOrderEvent(final IOrder order,
                                final OrderEventType orderEventType) {
        final OrderEvent orderEvent = new OrderEvent(order, orderEventType);
        orderEventSubject.onNext(orderEvent);
    }

    private boolean positionHasOrder(final IOrder orderToFind) {
        return position.filter(order -> order.getLabel().equals(orderToFind.getLabel())).size() == 1;
    }

    private boolean isRepositoryEmpty() {
        return position.filter(order -> true).isEmpty();
    }

    private void assertSubscriberCompleted(final TestSubscriber<?> subscriber) {
        subscriber.assertNoErrors();
        subscriber.assertCompleted();
    }

    private void assertSubscriberNotYetCompleted(final TestSubscriber<?> subscriber) {
        subscriber.assertNoErrors();
        subscriber.assertNotCompleted();
    }

    @Test
    public void testCloseOnEmptyPositionDoesNotCallOnPositionTask() {
        final TestSubscriber<OrderEvent> closeSubscriber = new TestSubscriber<>();

        position.close().subscribe(closeSubscriber);

        verifyZeroInteractions(positionTaskMock);
        assertSubscriberCompleted(closeSubscriber);
    }

    @Test
    public void testMergeOnEmptyPositionDoesNotCallOnPositionTask() {
        final TestSubscriber<OrderEvent> mergeSubscriber = new TestSubscriber<>();

        position.merge("TestLabel", restoreSLTPPolicyMock).subscribe(mergeSubscriber);

        verifyZeroInteractions(positionTaskMock);
        assertSubscriberCompleted(mergeSubscriber);
    }

    public class BuySubmitOK {

        private final String mergeLabel = "MergeLabel";
        protected final OrderParams orderParamsSell = OrderParamsForTest.paramsSellEURUSD();
        private final Runnable buySubmitCall =
                () -> position.addOrder(buyOrder);

        @Before
        public void setUp() {
            buyOrder.setState(IOrder.State.FILLED);

            buySubmitCall.run();
        }

        @Test
        public void testPositionHasBuyOrder() {
            assertTrue(positionHasOrder(buyOrder));
        }

        @Test
        public void testMergeCallIsIgnored() {
            final TestSubscriber<OrderEvent> mergeSubscriber = new TestSubscriber<>();

            position.merge(mergeLabel, restoreSLTPPolicyMock).subscribe(mergeSubscriber);

            verify(positionTaskMock, never()).mergeObservable(eq(mergeLabel), any());
            assertSubscriberCompleted(mergeSubscriber);
        }

        public class CloseOnSL {

            @Before
            public void setUp() {
                buyOrder.setState(IOrder.State.CLOSED);

                sendOrderEvent(buyOrder, OrderEventType.CLOSED_BY_SL);
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

                sendOrderEvent(buyOrder, OrderEventType.CLOSED_BY_TP);
            }

            @Test
            public void testPositionHasNoOrder() {
                assertTrue(isRepositoryEmpty());
            }
        }

        public class SellSubmitOK {

            @Before
            public void setUp() {
                sellOrder.setState(IOrder.State.FILLED);

                position.addOrder(sellOrder);
            }

            @Test
            public void testPositionHasBuyAndSellOrder() {
                assertTrue(positionHasOrder(buyOrder));
                assertTrue(positionHasOrder(sellOrder));
            }

            public class MergeSequenceSetup {

                private final double noSLPrice = platformSettings.noSLPrice();
                private final double noTPPrice = platformSettings.noTPPrice();
                protected final TestSubscriber<OrderEvent> mergeSubscriber = new TestSubscriber<>();
                protected Runnable mergeCall =
                        () -> position.merge(mergeLabel, restoreSLTPPolicyMock).subscribe(mergeSubscriber);

                public class RemoveTPFail {

                    @Before
                    public void setUp() {
                        when(positionTaskMock.setTPCompletable(buyOrder, noTPPrice))
                                .thenReturn(Completable.error(jfException));
                        when(positionTaskMock.setTPCompletable(sellOrder, noTPPrice))
                                .thenReturn(Completable.error(jfException));

                        mergeCall.run();
                    }

                    @Test
                    public void testRemoveTPIsCalledPositionTask() {
                        verify(positionTaskMock).setTPCompletable(any(), eq(noTPPrice));
                    }

                    @Test
                    public void testNoRemoveSLAndMergeIsCalledOnPositionTask() {
                        verify(positionTaskMock, never()).setSLCompletable(buyOrder, noSLPrice);
                        verify(positionTaskMock, never()).setSLCompletable(sellOrder, noSLPrice);
                        verify(positionTaskMock, never()).mergeObservable(eq(mergeLabel), any());
                    }

                    @Test
                    public void testMergeSubscriberCompletedWithError() {
                        mergeSubscriber.assertError(JFException.class);
                    }
                }

                public class RemoveTPOK {

                    @Before
                    public void setUp() {
                        when(positionTaskMock.setTPCompletable(buyOrder, noTPPrice))
                                .thenReturn(Completable.complete());
                        when(positionTaskMock.setTPCompletable(sellOrder, noTPPrice))
                                .thenReturn(Completable.complete());
                    }

                    @Test
                    public void testRemoveTPIsCalledPositionTask() {
                        mergeCall.run();

                        verify(positionTaskMock).setTPCompletable(buyOrder, noTPPrice);
                        verify(positionTaskMock).setTPCompletable(sellOrder, noTPPrice);
                    }

                    public class RemoveSLInProgress {

                        @Before
                        public void setUp() {
                            when(positionTaskMock.setSLCompletable(buyOrder, noTPPrice))
                                    .thenReturn(Completable.never());
                            when(positionTaskMock.setSLCompletable(sellOrder, noTPPrice))
                                    .thenReturn(Completable.never());

                            mergeCall.run();
                        }

                        @Test
                        public void testRemoveTPIsCalledPositionTask() {
                            verify(positionTaskMock).setTPCompletable(buyOrder, noTPPrice);
                            verify(positionTaskMock).setTPCompletable(sellOrder, noTPPrice);
                        }

                        @Test
                        public void testMergeSubscriberNotYetCompleted() {
                            assertSubscriberNotYetCompleted(mergeSubscriber);
                        }
                    }

                    public class RemoveSLOK {

                        @Before
                        public void setUp() {
                            when(positionTaskMock.setSLCompletable(buyOrder, noSLPrice))
                                    .thenReturn(Completable.complete());
                            when(positionTaskMock.setSLCompletable(sellOrder, noSLPrice))
                                    .thenReturn(Completable.complete());
                        }

                        @Test
                        public void testRemoveSLIsCalledPositionTask() {
                            mergeCall.run();

                            verify(positionTaskMock).setSLCompletable(buyOrder, noSLPrice);
                            verify(positionTaskMock).setSLCompletable(sellOrder, noSLPrice);
                        }

                        public class MergeCallFail {

                            @Before
                            public void setUp() {
                                when(positionTaskMock.mergeObservable(eq(mergeLabel), any()))
                                        .thenReturn(Observable.error(jfException));

                                mergeCall.run();
                            }

                            @Test
                            public void testPositionHasStillBuyAndSellOrder() {
                                assertTrue(positionHasOrder(buyOrder));
                                assertTrue(positionHasOrder(sellOrder));
                            }

                            @Test
                            public void testMergeSubscriberCompletedWithError() {
                                mergeSubscriber.assertError(JFException.class);
                            }
                        }

                        public class MergeCallOK {

                            private final IOrderForTest mergeOrder = IOrderForTest.buyOrderEURUSD();

                            @Before
                            public void setUp() {
                                buyOrder.setState(IOrder.State.CLOSED);
                                sellOrder.setState(IOrder.State.CLOSED);

                                mergeOrder.setState(IOrder.State.FILLED);
                                mergeOrder.setLabel(mergeLabel);

                                when(positionTaskMock
                                        .mergeObservable(eq(mergeLabel), toMergeOrdersCaptor.capture()))
                                                .thenReturn(Observable.just(mergeOrder));
                            }

                            @Test
                            public void testMergeICalledOnPositionTaskWithCorrectOrders() {
                                mergeCall.run();

                                final Set<IOrder> ordersToMerge = toMergeOrdersCaptor.getValue();
                                verify(positionTaskMock).mergeObservable(mergeLabel, ordersToMerge);
                                assertThat(ordersToMerge.size(), equalTo(2));
                                assertTrue(ordersToMerge.contains(buyOrder));
                                assertTrue(ordersToMerge.contains(sellOrder));
                            }

                            @Test
                            public void testPositionHasOnlyMergeOrder() {
                                mergeCall.run();

                                sendOrderEvent(buyOrder, OrderEventType.CLOSED_BY_MERGE);
                                sendOrderEvent(sellOrder, OrderEventType.CLOSED_BY_MERGE);

                                assertThat(position.orders().size(), equalTo(1));
                                assertTrue(positionHasOrder(mergeOrder));
                            }

                            public class RestoreSLOK {

                                private final double restoreSL = 1.12345;
                                private final double restoreTP = 1.12543;

                                @Before
                                public void setUp() {
                                    when(restoreSLTPPolicyMock.restoreSL(any())).thenReturn(restoreSL);
                                    when(restoreSLTPPolicyMock.restoreTP(any())).thenReturn(restoreTP);

                                    when(positionTaskMock.setSLCompletable(mergeOrder, restoreSL))
                                            .thenReturn(Completable.complete());
                                }

                                @Test
                                public void testRestoreSLIsCalledOnPositionTask() {
                                    mergeCall.run();

                                    verify(positionTaskMock).setSLCompletable(mergeOrder, restoreSL);
                                }

                                @Test
                                public void testCloseSubscriberNotYetCompleted() {
                                    assertSubscriberNotYetCompleted(mergeSubscriber);
                                }

                                public class RestoreTPOK {

                                    @Before
                                    public void setUp() {
                                        when(positionTaskMock.setTPCompletable(mergeOrder, restoreTP))
                                                .thenReturn(Completable.complete());

                                        mergeCall.run();
                                    }

                                    @Test
                                    public void testRestoreTPIsCalledOnPositionTask() {
                                        verify(positionTaskMock).setTPCompletable(mergeOrder, restoreTP);
                                    }

                                    @Test
                                    public void testMergeSubscriberCompleted() {
                                        assertSubscriberCompleted(mergeSubscriber);
                                    }
                                }

                                public class RestoreTPFail {

                                    @Before
                                    public void setUp() {
                                        when(positionTaskMock.setTPCompletable(mergeOrder, restoreTP))
                                                .thenReturn(Completable.error(jfException));

                                        mergeCall.run();
                                    }

                                    @Test
                                    public void testMergeSubscriberCompletedWithError() {
                                        mergeSubscriber.assertError(JFException.class);
                                    }
                                }
                            }
                        }
                    }

                    public class RemoveSLFail {

                        @Before
                        public void setUp() {
                            when(positionTaskMock.setSLCompletable(buyOrder, noTPPrice))
                                    .thenReturn(Completable.error(jfException));
                            when(positionTaskMock.setSLCompletable(sellOrder, noTPPrice))
                                    .thenReturn(Completable.error(jfException));

                            mergeCall.run();
                        }

                        @Test
                        public void testRemoveTPIsCalledPositionTask() {
                            verify(positionTaskMock).setTPCompletable(buyOrder, noTPPrice);
                            verify(positionTaskMock).setTPCompletable(sellOrder, noTPPrice);
                        }

                        @Test
                        public void testNoMergeCallOnPositionTask() {
                            verify(positionTaskMock, never()).mergeObservable(eq(mergeLabel), any());
                        }

                        @Test
                        public void testMergeSubscriberCompletedWithError() {
                            mergeSubscriber.assertError(JFException.class);
                        }
                    }
                }
            }

            public class CloseSetup {

                protected final TestSubscriber<OrderEvent> closeSubscriber = new TestSubscriber<>();
                protected Runnable closeCall = () -> position.close().subscribe(closeSubscriber);

                public class CloseInProcess {

                    @Before
                    public void setUp() {
                        when(positionTaskMock.closeCompletable(buyOrder)).thenReturn(Completable.complete());
                        when(positionTaskMock.closeCompletable(sellOrder)).thenReturn(Completable.never());

                        closeCall.run();

                        buyOrder.setState(IOrder.State.CLOSED);
                    }

                    @Test
                    public void testCloseIsCalledPositionTask() {
                        verify(positionTaskMock).closeCompletable(buyOrder);
                        verify(positionTaskMock).closeCompletable(sellOrder);
                    }

                    @Test
                    public void testCloseSubscriberNotYetCompleted() {
                        assertSubscriberNotYetCompleted(closeSubscriber);
                    }

                    @Test
                    public void testPositionHasOnlySellOrder() {
                        assertTrue(positionHasOrder(sellOrder));
                    }
                }

                public class CloseOK {

                    @Before
                    public void setUp() {
                        when(positionTaskMock.closeCompletable(buyOrder)).thenReturn(Completable.complete());
                        when(positionTaskMock.closeCompletable(sellOrder)).thenReturn(Completable.complete());

                        closeCall.run();

                        buyOrder.setState(IOrder.State.CLOSED);
                        sellOrder.setState(IOrder.State.CLOSED);
                        sendOrderEvent(buyOrder, OrderEventType.CLOSE_OK);
                        sendOrderEvent(sellOrder, OrderEventType.CLOSE_OK);
                    }

                    @Test
                    public void testPositionHasNoOrder() {
                        assertTrue(isRepositoryEmpty());
                    }

                    @Test
                    public void testSubmitSubscriberCompleted() {
                        assertSubscriberCompleted(closeSubscriber);
                    }
                }

                public class CloseFail {

                    @Before
                    public void setUp() {
                        when(positionTaskMock.closeCompletable(buyOrder))
                                .thenReturn(Completable.complete());
                        when(positionTaskMock.closeCompletable(sellOrder))
                                .thenReturn(Completable.error(jfException));

                        closeCall.run();
                    }

                    @Test
                    public void testCloseIsCalledOnOneOrder() {
                        verify(positionTaskMock, atLeast(1)).closeCompletable(any());
                    }

                    @Test
                    public void testPositionHasStillBuyOrder() {
                        assertTrue(positionHasOrder(buyOrder));
                    }

                    @Test
                    public void testCloseSubscriberCompletedWithError() {
                        closeSubscriber.assertError(JFException.class);
                    }
                }
            }
        }
    }
}