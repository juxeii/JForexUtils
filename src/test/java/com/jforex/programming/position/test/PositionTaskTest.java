package com.jforex.programming.position.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.OrderTask;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.position.Position;
import com.jforex.programming.position.PositionFactory;
import com.jforex.programming.position.PositionTask;
import com.jforex.programming.test.common.InstrumentUtilForTest;

import de.bechte.junit.runners.context.HierarchicalContextRunner;
import io.reactivex.functions.Action;
import io.reactivex.observers.TestObserver;

@RunWith(HierarchicalContextRunner.class)
public class PositionTaskTest extends InstrumentUtilForTest {

    private PositionTask positionTask;

    @Mock private OrderTask orderTaskMock;
    @Mock private PositionFactory positionFactoryMock;
    @Mock private Position positionMock;
    @Mock private Action actionMock;
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

//    public class ClosePositionTests {
//
//        private ClosePositionCommand command;
//        private Observable<OrderEvent> closeObservable;
//
//        @Before
//        public void setUp() {
//            command = ClosePositionCommand
//                .with(instrumentEURUSD, mergeOrderLabel)
//                .closeFilled((obs, order) -> obs
//                    .doOnSubscribe(d -> logger.info("Starting to close filled orders of position " + instrumentEURUSD))
//                    .doOnComplete(() -> logger.info("Closed filled orders of position " + instrumentEURUSD)))
//                .withMergeCommand()
//                .withCancelSLAndTP(null)
//                .withCancelSL(null)
//                .withCancelTP(null)
//                .withExecutionMode(null)
//                .done()
//                .build();
//
//            closeObservable = positionTask
//                .close(command)
//                .doOnSubscribe(d -> logger.info("Start close position task for " + instrumentEURUSD))
//                .doOnTerminate(() -> logger.info("Close position task for " + instrumentEURUSD + " done"));
//        }
//
//        @Test
//        public void observableIsDeferredWithNoInteractionsToMocks() {
//            verifyZeroInteractions(orderTaskMock);
//            verifyZeroInteractions(positionFactoryMock);
//        }
//
//        @Test
//        public void observableCompletesImmediatelyWhenNoOrdersToClose() {
//            expectFilledOrders(Sets.newHashSet());
//            expectFilledOrOpenedOrders(Sets.newHashSet());
//
//            testSubscriber = closeObservable.test();
//
//            testSubscriber.assertNoValues();
//            testSubscriber.assertComplete();
//        }
//
//        public class PositionHasOneFilledOrder {
//
//            @Before
//            public void setUp() {
//                expectFilledOrders(Sets.newHashSet(buyOrderEURUSD));
//                expectFilledOrOpenedOrders(Sets.newHashSet(buyOrderEURUSD));
//
//                when(orderTaskMock.close(buyOrderEURUSD)).thenReturn(emptyObservable());
//
//                testSubscriber = closeObservable.test();
//            }
//
//            @Test
//            public void noMergeCall() {
//                verify(orderTaskMock, never()).mergeOrders(any(), any());
//            }
//
//            @Test
//            public void oneCloseCall() {
//                verify(orderTaskMock).close(buyOrderEURUSD);
//            }
//
//            @Test
//            public void subscriberCompletes() {
//                testSubscriber.assertComplete();
//            }
//        }
//
//        public class PositionHasOneOpenedOrder {
//
//            @Before
//            public void setUp() {
//                expectFilledOrders(Sets.newHashSet());
//                expectFilledOrOpenedOrders(Sets.newHashSet(buyOrderEURUSD));
//
//                when(orderTaskMock.close(buyOrderEURUSD)).thenReturn(emptyObservable());
//
//                testSubscriber = closeObservable.test();
//            }
//
//            @Test
//            public void noMergeCall() {
//                verify(orderTaskMock, never()).mergeOrders(any(), any());
//            }
//
//            @Test
//            public void oneCloseCall() {
//                verify(orderTaskMock).close(buyOrderEURUSD);
//            }
//
//            @Test
//            public void subscriberCompletes() {
//                testSubscriber.assertComplete();
//            }
//        }
//
//        public class PositionHasTwoFilledOrders {
//
//            private final Set<IOrder> toMergeOrders = Sets.newHashSet(buyOrderEURUSD, sellOrderEURUSD);
//
//            @Before
//            public void setUp() {
//                expectFilledOrders(toMergeOrders);
//            }
//
//            @Test
//            public void mergeIsCalled() {
//                when(orderTaskMock.mergeOrders(mergeOrderLabel, toMergeOrders))
//                    .thenReturn(emptyObservable());
//
//                testSubscriber = closeObservable.test();
//
//                verify(orderTaskMock).mergeOrders(mergeOrderLabel, toMergeOrders);
//            }
//
//            public class MergeNeverCompletes {
//
//                @Before
//                public void setUp() {
//                    when(orderTaskMock.mergeOrders(mergeOrderLabel, toMergeOrders))
//                        .thenReturn(neverObservable());
//
//                    testSubscriber = closeObservable.test();
//                }
//
//                @Test
//                public void subscriberNotCompleted() {
//                    testSubscriber.assertNotComplete();
//                }
//            }
//
//            public class MergeEmitsError {
//
//                @Before
//                public void setUp() {
//                    when(orderTaskMock.mergeOrders(mergeOrderLabel, toMergeOrders))
//                        .thenReturn(errorObservable());
//
//                    expectFilledOrOpenedOrders(toMergeOrders);
//
//                    testSubscriber = closeObservable.test();
//                }
//
//                @Test
//                public void noCloseCalls() {
//                    verify(orderTaskMock, never()).close(any());
//                }
//
//                @Test
//                public void subscriberErrors() {
//                    testSubscriber.assertError(jfException);
//                }
//            }
//
//            public class MergeCompletes {
//
//                @Before
//                public void setUp() {
//                    when(orderTaskMock.mergeOrders(mergeOrderLabel, toMergeOrders))
//                        .thenReturn(emptyObservable());
//                }
//
//                public class NoFilledOrOpenedOrders {
//
//                    @Before
//                    public void setUp() {
//                        expectFilledOrOpenedOrders(Sets.newHashSet());
//
//                        testSubscriber = closeObservable.test();
//                    }
//
//                    @Test
//                    public void subscriberCompleted() {
//                        testSubscriber.assertComplete();
//                    }
//
//                    @Test
//                    public void noCloseCall() {
//                        verify(orderTaskMock, never()).close(any());
//                    }
//                }
//
//                public class TwoFilledOrOpenedOrders {
//
//                    private final Set<IOrder> toCloseOrders = toMergeOrders;
//
//                    @Before
//                    public void setUp() {
//                        expectFilledOrOpenedOrders(toCloseOrders);
//                    }
//
//                    public class CloseCallDoNotComplete {
//
//                        @Before
//                        public void setUp() {
//                            when(orderTaskMock.close(buyOrderEURUSD)).thenReturn(neverObservable());
//                            when(orderTaskMock.close(sellOrderEURUSD)).thenReturn(neverObservable());
//
//                            testSubscriber = closeObservable.test();
//                        }
//
//                        @Test
//                        public void subscriberNotCompleted() {
//                            testSubscriber.assertNotComplete();
//                        }
//
//                        @Test
//                        public void closeCallsAreNotConcatenated() {
//                            verify(orderTaskMock).close(buyOrderEURUSD);
//                            verify(orderTaskMock).close(sellOrderEURUSD);
//                        }
//                    }
//
//                    public class CloseCallsSucceed {
//
//                        @Before
//                        public void setUp() {
//                            when(orderTaskMock.close(any())).thenReturn(emptyObservable());
//
//                            testSubscriber = closeObservable.test();
//                        }
//
//                        @Test
//                        public void subscriberCompleted() {
//                            testSubscriber.assertComplete();
//                        }
//
//                        @Test
//                        public void twoCloseCalls() {
//                            verify(orderTaskMock).close(buyOrderEURUSD);
//                            verify(orderTaskMock).close(sellOrderEURUSD);
//                        }
//                    }
//                }
//            }
//        }
//    }

//    public class MergePositionWithCommandTests {
//
//        private MergeCommand command;
//        private Observable<OrderEvent> mergePositonObservable;
//
//        @Before
//        public void setUp() {
//            command = MergeCommand
//                .with(mergeOrderLabel)
//                .withCancelSLAndTP(obs -> obs
//                    .doOnSubscribe(d -> logger.info("Starting to cancel SL and TP for position " + instrumentEURUSD))
//                    .doOnComplete(() -> logger.info("Cancel SL and TP for position " + instrumentEURUSD + " done.")))
//                .withCancelSL((obs, order) -> obs
//                    .doOnSubscribe(d -> logger.info("Starting to cancel SL for order " + order.getLabel()))
//                    .doOnComplete(() -> logger.info("Cancel SL for order " + order.getLabel() + " finished.")))
//                .withCancelTP((obs, order) -> obs
//                    .doOnSubscribe(d -> logger.info("Starting to cancel TP for order " + order.getLabel()))
//                    .doOnComplete(() -> logger.info("Cancel TP for order " + order.getLabel() + " finished.")))
//                .withExecutionMode(MergeExecutionMode.ConcatSLAndTP)
//                .withMerge(obs -> obs
//                    .doOnSubscribe(d -> logger.info("Starting to merge instrument " +
//                            instrumentEURUSD + " with label " + mergeOrderLabel))
//                    .doOnComplete(() -> logger.info("Merging instrument " +
//                            instrumentEURUSD + " with label " + mergeOrderLabel + " done.")))
//                .build();
//
//            mergePositonObservable = positionTask
//                .merge(instrumentEURUSD, command)
//                .doOnSubscribe(d -> logger.info("Starting to merge position " + instrumentEURUSD))
//                .doOnComplete(() -> logger.info("Merging position " + instrumentEURUSD + " done."));
//        }
//
//        @Test
//        public void observableIsDeferredWithNoInteractionsToMocks() {
//            verifyZeroInteractions(orderTaskMock);
//            verifyZeroInteractions(positionFactoryMock);
//        }
//
//        @Test
//        public void completesImmediatelyWhenNoPositionOrder() {
//            expectFilledOrders(Sets.newHashSet());
//
//            testSubscriber = mergePositonObservable.test();
//
//            testSubscriber.assertNoValues();
//            testSubscriber.assertComplete();
//        }
//
//        @Test
//        public void completesImmediatelyWhenOnePositionOrder() {
//            expectFilledOrders(Sets.newHashSet(buyOrderEURUSD));
//
//            testSubscriber = mergePositonObservable.test();
//
//            testSubscriber.assertNoValues();
//            testSubscriber.assertComplete();
//        }
//
//        public class PositionHasTwoFilledOrders {
//
//            private final Set<IOrder> toMergeOrders = Sets.newHashSet(buyOrderEURUSD, sellOrderEURUSD);
//
//            @Before
//            public void setUp() {
//                when(orderTaskMock.setStopLossPrice(buyOrderEURUSD, noSL))
//                    .thenReturn(emptyObservable());
//                when(orderTaskMock.setStopLossPrice(sellOrderEURUSD, noSL))
//                    .thenReturn(emptyObservable());
//
//                when(orderTaskMock.setTakeProfitPrice(buyOrderEURUSD, noTP))
//                    .thenReturn(emptyObservable());
//                when(orderTaskMock.setTakeProfitPrice(sellOrderEURUSD, noTP))
//                    .thenReturn(emptyObservable());
//
//                when(orderTaskMock.mergeOrders(toMergeOrders, command))
//                    .thenReturn(emptyObservable());
//
//                expectFilledOrders(toMergeOrders);
//            }
//
//            @Test
//            public void callsMergeWhenTwoPositionOrders() {
//                testSubscriber = mergePositonObservable.test();
//
//                testSubscriber.assertNoValues();
//                testSubscriber.assertComplete();
//            }
//        }
//    }
}
