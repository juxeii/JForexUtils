package com.jforex.programming.position.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.Collection;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import com.dukascopy.api.IOrder;
import com.google.common.collect.Sets;
import com.jforex.programming.order.OrderTask;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.position.Position;
import com.jforex.programming.position.PositionFactory;
import com.jforex.programming.position.PositionTask;
import com.jforex.programming.test.common.InstrumentUtilForTest;

import de.bechte.junit.runners.context.HierarchicalContextRunner;
import io.reactivex.Observable;
import io.reactivex.observers.TestObserver;

@RunWith(HierarchicalContextRunner.class)
public class PositionTaskTest extends InstrumentUtilForTest {

    private PositionTask positionTask;

    @Mock
    private OrderTask orderTaskMock;
    @Mock
    private PositionFactory positionFactoryMock;
    @Mock
    private Position positionMock;
    private final String mergeOrderLabel = "mergeOrderLabel";
    private TestObserver<OrderEvent> testSubscriber;

    @Before
    public void setUp() {
        setUpMocks();

        positionTask = new PositionTask(orderTaskMock, positionFactoryMock);
    }

    private void setUpMocks() {
        when(positionFactoryMock.forInstrument(instrumentEURUSD))
            .thenReturn(positionMock);
    }

    private void expectFilledOrders(final Set<IOrder> filledOrders) {
        when(positionMock.filled()).thenReturn(filledOrders);
    }

    private void expectFilledOrOpenedOrders(final Set<IOrder> filledOrOpenedOrders) {
        when(positionMock.filledOrOpened()).thenReturn(filledOrOpenedOrders);
    }

    @Test
    public void positionOrdersIsCorrect() {
        assertThat(positionTask.positionOrders(instrumentEURUSD), equalTo(positionMock));
    }

    public class CancelSLTests {

        private Observable<OrderEvent> cancelSLObservable;

        @Before
        public void setUp() {
            cancelSLObservable = positionTask.cancelStopLossPrice(instrumentEURUSD);
        }

        @Test
        public void observableIsDeferredWithNoInteractionsToMocks() {
            verifyZeroInteractions(orderTaskMock);
            verifyZeroInteractions(positionFactoryMock);
        }

        @Test
        public void completesImmediatelyWhenNoOrdersInPosition() {
            when(orderTaskMock.cancelStopLossPrice(buyOrderEURUSD))
                .thenReturn(emptyObservable());

            testSubscriber = cancelSLObservable.test();

            testSubscriber.assertComplete();
            verifyZeroInteractions(orderTaskMock);
        }

        public class TwoOrdersForCancelSL {

            @Before
            public void setUp() {
                expectFilledOrOpenedOrders(Sets.newHashSet(buyOrderEURUSD, sellOrderEURUSD));
            }

            private void verifyBothCancelCalls() {
                verify(orderTaskMock).cancelStopLossPrice(buyOrderEURUSD);
                verify(orderTaskMock).cancelStopLossPrice(sellOrderEURUSD);
            }

            private void setUpCancelSLObservables(final Observable<OrderEvent> buyObservable,
                                                  final Observable<OrderEvent> sellObservable) {
                when(orderTaskMock.cancelStopLossPrice(buyOrderEURUSD))
                    .thenReturn(buyObservable);
                when(orderTaskMock.cancelStopLossPrice(sellOrderEURUSD))
                    .thenReturn(sellObservable);
            }

            @Test
            public void cancelCallsAreMerged() {
                setUpCancelSLObservables(neverObservable(), neverObservable());

                testSubscriber = cancelSLObservable.test();

                verifyBothCancelCalls();
            }

            @Test
            public void completesWhenBothOrdersAreClosed() {
                setUpCancelSLObservables(emptyObservable(), emptyObservable());

                testSubscriber = cancelSLObservable.test();

                testSubscriber.assertComplete();
                verifyBothCancelCalls();
            }
        }
    }

    public class CancelTPTests {

        private Observable<OrderEvent> cancelTPObservable;

        @Before
        public void setUp() {
            cancelTPObservable = positionTask.cancelTakeProfitPrice(instrumentEURUSD);
        }

        @Test
        public void observableIsDeferredWithNoInteractionsToMocks() {
            verifyZeroInteractions(orderTaskMock);
            verifyZeroInteractions(positionFactoryMock);
        }

        @Test
        public void completesImmediatelyWhenNoOrdersInPosition() {
            when(orderTaskMock.cancelTakeProfitPrice(buyOrderEURUSD))
                .thenReturn(emptyObservable());

            testSubscriber = cancelTPObservable.test();

            testSubscriber.assertComplete();
            verifyZeroInteractions(orderTaskMock);
        }

        public class TwoOrdersForCancelTP {

            @Before
            public void setUp() {
                expectFilledOrOpenedOrders(Sets.newHashSet(buyOrderEURUSD, sellOrderEURUSD));
            }

            private void verifyBothCancelCalls() {
                verify(orderTaskMock).cancelTakeProfitPrice(buyOrderEURUSD);
                verify(orderTaskMock).cancelTakeProfitPrice(sellOrderEURUSD);
            }

            private void setUpCancelTPObservables(final Observable<OrderEvent> buyObservable,
                                                  final Observable<OrderEvent> sellObservable) {
                when(orderTaskMock.cancelTakeProfitPrice(buyOrderEURUSD))
                    .thenReturn(buyObservable);
                when(orderTaskMock.cancelTakeProfitPrice(sellOrderEURUSD))
                    .thenReturn(sellObservable);
            }

            @Test
            public void cancelCallsAreMerged() {
                setUpCancelTPObservables(neverObservable(), neverObservable());

                testSubscriber = cancelTPObservable.test();

                verifyBothCancelCalls();
            }

            @Test
            public void completesWhenBothOrdersAreClosed() {
                setUpCancelTPObservables(emptyObservable(), emptyObservable());

                testSubscriber = cancelTPObservable.test();

                testSubscriber.assertComplete();
                verifyBothCancelCalls();
            }
        }
    }

    public class MergePositionTests {

        private Observable<OrderEvent> mergeObservable;

        @Before
        public void setUp() {
            mergeObservable = positionTask.merge(instrumentEURUSD, mergeOrderLabel);
        }

        private void setUpOrderUtilMergeObservables(final Collection<IOrder> toMergeOrders,
                                                    final Observable<OrderEvent> observable) {
            when(orderTaskMock.mergeOrders(mergeOrderLabel, toMergeOrders))
                .thenReturn(observable);
        }

        @Test
        public void observableIsDeferredWithNoInteractionsToMocks() {
            verifyZeroInteractions(orderTaskMock);
            verifyZeroInteractions(positionFactoryMock);
        }

        public class OnSubscribe {

            public void prepareToMergeOrdersAndSubscribe(final Set<IOrder> toMergeOrders) {
                expectFilledOrders(toMergeOrders);
                setUpOrderUtilMergeObservables(toMergeOrders, emptyObservable());

                testSubscriber = mergeObservable.test();
            }

            @Test
            public void completesImmediatelyWhenNoOrdersForMerge() {
                prepareToMergeOrdersAndSubscribe(Sets.newHashSet());

                testSubscriber.assertComplete();
                verifyZeroInteractions(orderTaskMock);
            }

            @Test
            public void completesImmediatelyWhenOnlyOneOrderForMerge() {
                prepareToMergeOrdersAndSubscribe(Sets.newHashSet(buyOrderEURUSD));

                testSubscriber.assertComplete();
                verifyZeroInteractions(orderTaskMock);
            }

            @Test
            public void callOnOrderUtilWhenEnoughOrdersForMerge() {
                final Set<IOrder> toMergeOrders = Sets.newHashSet(buyOrderEURUSD, sellOrderEURUSD);
                prepareToMergeOrdersAndSubscribe(toMergeOrders);

                testSubscriber.assertComplete();
                verify(orderTaskMock).mergeOrders(mergeOrderLabel, toMergeOrders);
            }
        }
    }

    public class ClosePositionTests {

        private Observable<OrderEvent> closeObservable;

        @Before
        public void setUp() {
            closeObservable = positionTask.close(instrumentEURUSD, mergeOrderLabel);
        }

        @Test
        public void observableIsDeferredWithNoInteractionsToMocks() {
            verifyZeroInteractions(orderTaskMock);
            verifyZeroInteractions(positionFactoryMock);
        }

        @Test
        public void observableCompletesImmediatelyWhenNoOrdersToClose() {
            expectFilledOrders(Sets.newHashSet());
            expectFilledOrOpenedOrders(Sets.newHashSet());

            testSubscriber = closeObservable.test();

            testSubscriber.assertNoValues();
            testSubscriber.assertComplete();
        }

        public class PositionHasOneFilledOrder {

            @Before
            public void setUp() {
                expectFilledOrders(Sets.newHashSet(buyOrderEURUSD));
                expectFilledOrOpenedOrders(Sets.newHashSet(buyOrderEURUSD));

                when(orderTaskMock.close(buyOrderEURUSD)).thenReturn(emptyObservable());

                testSubscriber = closeObservable.test();
            }

            @Test
            public void noMergeCall() {
                verify(orderTaskMock, never()).mergeOrders(any(), any());
            }

            @Test
            public void oneCloseCall() {
                verify(orderTaskMock).close(buyOrderEURUSD);
            }

            @Test
            public void subscriberCompletes() {
                testSubscriber.assertComplete();
            }
        }

        public class PositionHasOneOpenedOrder {

            @Before
            public void setUp() {
                expectFilledOrders(Sets.newHashSet());
                expectFilledOrOpenedOrders(Sets.newHashSet(buyOrderEURUSD));

                when(orderTaskMock.close(buyOrderEURUSD)).thenReturn(emptyObservable());

                testSubscriber = closeObservable.test();
            }

            @Test
            public void noMergeCall() {
                verify(orderTaskMock, never()).mergeOrders(any(), any());
            }

            @Test
            public void oneCloseCall() {
                verify(orderTaskMock).close(buyOrderEURUSD);
            }

            @Test
            public void subscriberCompletes() {
                testSubscriber.assertComplete();
            }
        }

        public class PositionHasTwoFilledOrders {

            private final Set<IOrder> toMergeOrders = Sets.newHashSet(buyOrderEURUSD, sellOrderEURUSD);

            @Before
            public void setUp() {
                expectFilledOrders(toMergeOrders);
            }

            @Test
            public void mergeIsCalled() {
                when(orderTaskMock.mergeOrders(mergeOrderLabel, toMergeOrders))
                    .thenReturn(emptyObservable());

                testSubscriber = closeObservable.test();

                verify(orderTaskMock).mergeOrders(mergeOrderLabel, toMergeOrders);
            }

            public class MergeNeverCompletes {

                @Before
                public void setUp() {
                    when(orderTaskMock.mergeOrders(mergeOrderLabel, toMergeOrders))
                        .thenReturn(neverObservable());

                    testSubscriber = closeObservable.test();
                }

                @Test
                public void subscriberNotCompleted() {
                    testSubscriber.assertNotComplete();
                }
            }

            public class MergeEmitsError {

                @Before
                public void setUp() {
                    when(orderTaskMock.mergeOrders(mergeOrderLabel, toMergeOrders))
                        .thenReturn(errorObservable());

                    expectFilledOrOpenedOrders(toMergeOrders);

                    testSubscriber = closeObservable.test();
                }

                @Test
                public void noCloseCalls() {
                    verify(orderTaskMock, never()).close(any());
                }

                @Test
                public void subscriberErrors() {
                    testSubscriber.assertError(jfException);
                }
            }

            public class MergeCompletes {

                @Before
                public void setUp() {
                    when(orderTaskMock.mergeOrders(mergeOrderLabel, toMergeOrders))
                        .thenReturn(emptyObservable());
                }

                public class NoFilledOrOpenedOrders {

                    @Before
                    public void setUp() {
                        expectFilledOrOpenedOrders(Sets.newHashSet());

                        testSubscriber = closeObservable.test();
                    }

                    @Test
                    public void subscriberCompleted() {
                        testSubscriber.assertComplete();
                    }

                    @Test
                    public void noCloseCall() {
                        verify(orderTaskMock, never()).close(any());
                    }
                }

                public class TwoFilledOrOpenedOrders {

                    private final Set<IOrder> toCloseOrders = toMergeOrders;

                    @Before
                    public void setUp() {
                        expectFilledOrOpenedOrders(toCloseOrders);
                    }

                    public class CloseCallDoNotComplete {

                        @Before
                        public void setUp() {
                            when(orderTaskMock.close(buyOrderEURUSD)).thenReturn(neverObservable());
                            when(orderTaskMock.close(sellOrderEURUSD)).thenReturn(neverObservable());

                            testSubscriber = closeObservable.test();
                        }

                        @Test
                        public void subscriberNotCompleted() {
                            testSubscriber.assertNotComplete();
                        }

                        @Test
                        public void closeCallsAreNotConcatenated() {
                            verify(orderTaskMock).close(buyOrderEURUSD);
                            verify(orderTaskMock).close(sellOrderEURUSD);
                        }
                    }

                    public class CloseCallsSucceed {

                        @Before
                        public void setUp() {
                            when(orderTaskMock.close(any())).thenReturn(emptyObservable());

                            testSubscriber = closeObservable.test();
                        }

                        @Test
                        public void subscriberCompleted() {
                            testSubscriber.assertComplete();
                        }

                        @Test
                        public void twoCloseCalls() {
                            verify(orderTaskMock).close(buyOrderEURUSD);
                            verify(orderTaskMock).close(sellOrderEURUSD);
                        }
                    }
                }
            }
        }
    }
}
