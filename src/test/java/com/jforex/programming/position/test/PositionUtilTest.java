package com.jforex.programming.position.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.Set;
import java.util.function.Function;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import com.dukascopy.api.IOrder;
import com.dukascopy.api.Instrument;
import com.google.common.collect.Sets;
import com.jforex.programming.order.OrderUtilCompletable;
import com.jforex.programming.order.command.CloseCommand;
import com.jforex.programming.order.command.MergeCommand;
import com.jforex.programming.position.Position;
import com.jforex.programming.position.PositionFactory;
import com.jforex.programming.position.PositionOrders;
import com.jforex.programming.position.PositionUtil;
import com.jforex.programming.test.common.InstrumentUtilForTest;

import de.bechte.junit.runners.context.HierarchicalContextRunner;
import rx.Completable;
import rx.Subscription;
import rx.functions.Action0;
import rx.functions.Action1;

@RunWith(HierarchicalContextRunner.class)
public class PositionUtilTest extends InstrumentUtilForTest {

    private PositionUtil positionUtil;

    @Mock
    private OrderUtilCompletable orderUtilCompletableMock;
    @Mock
    private PositionFactory positionFactoryMock;
    @Mock
    private Position positionEURUSDMock;
    @Mock
    private Position positionAUDUSDMock;
    @Mock
    private Function<Set<IOrder>, MergeCommand> mergeCommandFactory;
    @Mock
    private Function<IOrder, CloseCommand> closeCommandFactory;
    @Mock
    private Function<Set<IOrder>, MergeCommand> mergeEURUSDCommandFactory;
    @Mock
    private Function<Set<IOrder>, MergeCommand> mergeAUDUSDCommandFactory;
    @Mock
    private MergeCommand mergeEURUSDCommandMock;
    @Mock
    private MergeCommand mergeAUDUSDCommandMock;
    @Mock
    private Function<IOrder, CloseCommand> closeEURUSDCommandFactory;
    @Mock
    private Function<IOrder, CloseCommand> closeAUDUSDCommandFactory;
    @Mock
    private CloseCommand closeEURUSDCommandMock;
    @Mock
    private CloseCommand closeAUDUSDCommandMock;
    @Mock
    private Action0 completedActionMock;
    @Mock
    private Action1<Subscription> onSubscribeEURUSDAction;
    @Mock
    private Action1<Subscription> onSubscribeAUDUSDAction;

    @Before
    public void setUp() {
        setUpMocks();

        positionUtil = spy(new PositionUtil(orderUtilCompletableMock, positionFactoryMock));
    }

    private void setUpMocks() {
        when(positionFactoryMock.forInstrument(instrumentEURUSD)).thenReturn(positionEURUSDMock);
        when(positionFactoryMock.forInstrument(instrumentAUDUSD)).thenReturn(positionAUDUSDMock);

        when(positionEURUSDMock.instrument()).thenReturn(instrumentEURUSD);
        when(positionAUDUSDMock.instrument()).thenReturn(instrumentAUDUSD);
    }

    private void verifyDeferredCompletable() {
        verifyZeroInteractions(orderUtilCompletableMock);
        verifyZeroInteractions(positionFactoryMock);
        verifyZeroInteractions(positionEURUSDMock);
        verifyZeroInteractions(positionAUDUSDMock);
    }

    private void expectPositions(final Set<Position> positions) {
        when(positionFactoryMock.allPositions()).thenReturn(positions);
    }

    @Test
    public void positionOrdersReturnsCorrectInstance() {
        final Position positionMock = mock(Position.class);
        when(positionFactoryMock.forInstrument(instrumentEURUSD))
            .thenReturn(positionMock);

        final PositionOrders positionOrders = positionUtil.positionOrders(instrumentEURUSD);

        assertThat(positionOrders, equalTo(positionMock));
    }

    public class MergePositionTests {

        private Completable mergePositionCompletable;

        @Before
        public void setUp() {
            mergePositionCompletable = positionUtil.mergePosition(instrumentEURUSD, mergeEURUSDCommandFactory);
        }

        private void setUtilCompletableResult(final Completable completable) {
            when(orderUtilCompletableMock.mergeOrders(mergeEURUSDCommandMock))
                .thenReturn(completable);
        }

        private void verifyInteractionsForNotEnoughMergeOrders() {
            verifyZeroInteractions(orderUtilCompletableMock);
            verify(completedActionMock).call();
        }

        private void expectFilledOrders(final Set<IOrder> filledOrders) {
            when(positionEURUSDMock.filled()).thenReturn(filledOrders);
        }

        @Test
        public void completableIsDeferredWithNoInteractionsToMocks() {
            verifyDeferredCompletable();
        }

        @Test
        public void onSubscribeWithNoMergeOrdersCompletesImmediately() {
            when(positionEURUSDMock.filled()).thenReturn(Sets.newHashSet());

            mergePositionCompletable.subscribe(completedActionMock);

            verifyInteractionsForNotEnoughMergeOrders();
        }

        @Test
        public void onSubscribeWithOneMergeOrdersCompletesImmediately() {
            expectFilledOrders(Sets.newHashSet(buyOrderEURUSD));

            mergePositionCompletable.subscribe(completedActionMock);

            verifyInteractionsForNotEnoughMergeOrders();
        }

        @Test
        public void onSubscribeOrderUtilCompletableIsCalled() {
            final Set<IOrder> toMergeOrders = Sets.newHashSet(buyOrderEURUSD, sellOrderEURUSD);
            expectFilledOrders(toMergeOrders);
            when(mergeEURUSDCommandFactory.apply(toMergeOrders)).thenReturn(mergeEURUSDCommandMock);
            setUtilCompletableResult(emptyCompletable());

            mergePositionCompletable.subscribe(completedActionMock);

            verify(orderUtilCompletableMock).mergeOrders(mergeEURUSDCommandMock);
            verify(completedActionMock).call();
        }
    }

    public class MergeAllPositionsTests {

        private Completable mergeAllPositionsCompletable;

        @Before
        public void setUp() {
            mergeAllPositionsCompletable = positionUtil.mergeAllPositions(mergeCommandFactory);
        }

        @Test
        public void completableIsDeferredWithNoInteractionsToMocks() {
            verifyDeferredCompletable();
        }

        @Test
        public void onSubscribeWithNoPositionsCompletesImmediately() {
            expectPositions(Sets.newHashSet());

            mergeAllPositionsCompletable.subscribe(completedActionMock);

            verifyZeroInteractions(orderUtilCompletableMock);
            verify(completedActionMock).call();
        }

        public class TwoPositionsPresent {

            private Completable mergeEURUSDCompletable;
            private Completable mergeAUDUSDCompletable;

            @Before
            public void setUp() {
                expectPositions(Sets.newHashSet(positionEURUSDMock, positionAUDUSDMock));
            }

            private void verifyMergePositionIsCalledForBothPositions() {
                verify(positionUtil).mergePosition(instrumentEURUSD, mergeCommandFactory);
                verify(positionUtil).mergePosition(instrumentAUDUSD, mergeCommandFactory);
            }

            private void completeMergePositionEURUSDSetup() {
                mergeEURUSDCompletable = emptyCompletable().doOnSubscribe(onSubscribeEURUSDAction);
                completeMergePosition(instrumentEURUSD,
                                      mergeEURUSDCompletable);
            }

            private void completeMergePositionAUDUSDSetup() {
                mergeAUDUSDCompletable = emptyCompletable().doOnSubscribe(onSubscribeAUDUSDAction);
                completeMergePosition(instrumentAUDUSD,
                                      mergeAUDUSDCompletable);
            }

            private void completeMergePosition(final Instrument instrument,
                                               final Completable completable) {
                when(positionUtil.mergePosition(instrument, mergeCommandFactory))
                    .thenReturn(completable);
            }

            private void neverCompleteMergePositionEURUSDSetup() {
                mergeEURUSDCompletable = neverCompletable().doOnSubscribe(onSubscribeEURUSDAction);
                neverCompleteMergePosition(instrumentEURUSD,
                                           mergeEURUSDCompletable);
            }

            private void neverCompleteMergePositionAUDUSDSetup() {
                mergeAUDUSDCompletable = neverCompletable().doOnSubscribe(onSubscribeAUDUSDAction);
                neverCompleteMergePosition(instrumentAUDUSD,
                                           mergeAUDUSDCompletable);
            }

            private void neverCompleteMergePosition(final Instrument instrument,
                                                    final Completable completable) {
                when(positionUtil.mergePosition(instrument, mergeCommandFactory))
                    .thenReturn(completable);
            }

            @Test
            public void testThatMergeCompletablesAreMerged() {
                neverCompleteMergePositionEURUSDSetup();
                neverCompleteMergePositionAUDUSDSetup();

                mergeAllPositionsCompletable.subscribe(completedActionMock);

                verify(onSubscribeEURUSDAction).call(any());
                verify(onSubscribeAUDUSDAction).call(any());
            }

            public class MergePositionEURUSDCompletes {

                @Before
                public void setUp() {
                    completeMergePositionEURUSDSetup();
                }

                public class ClosePositionAUDUSDCompletes {

                    @Before
                    public void setUp() {
                        completeMergePositionAUDUSDSetup();

                        mergeAllPositionsCompletable.subscribe(completedActionMock);
                    }

                    @Test
                    public void mergePositionIsCalledForBothPositions() {
                        verifyMergePositionIsCalledForBothPositions();
                    }

                    @Test
                    public void mergePositionCompleted() {
                        verify(completedActionMock).call();
                    }
                }

                public class MergePositionAUDUSDDoesNotComplete {

                    @Before
                    public void setUp() {
                        neverCompleteMergePositionAUDUSDSetup();

                        mergeAllPositionsCompletable.subscribe(completedActionMock);
                    }

                    @Test
                    public void mergePositionIsCalledForBothPositions() {
                        verifyMergePositionIsCalledForBothPositions();
                    }

                    @Test
                    public void mergePositionNotCompleted() {
                        verifyZeroInteractions(completedActionMock);
                    }
                }
            }

            public class MergePositionEURUSDDoesNotComplete {

                @Before
                public void setUp() {
                    neverCompleteMergePositionEURUSDSetup();
                }

                public class MergePositionAUDUSDCompletes {

                    @Before
                    public void setUp() {
                        completeMergePositionAUDUSDSetup();

                        mergeAllPositionsCompletable.subscribe(completedActionMock);
                    }

                    @Test
                    public void mergePositionIsCalledForBothPositions() {
                        verifyMergePositionIsCalledForBothPositions();
                    }

                    @Test
                    public void mergePositionNotCompleted() {
                        verifyZeroInteractions(completedActionMock);
                    }
                }

                public class MergePositionAUDUSDDoesNotComplete {

                    @Before
                    public void setUp() {
                        neverCompleteMergePositionAUDUSDSetup();

                        mergeAllPositionsCompletable.subscribe(completedActionMock);
                    }

                    @Test
                    public void mergePositionIsCalledForBothPositions() {
                        verifyMergePositionIsCalledForBothPositions();
                    }

                    @Test
                    public void mergePositionNotCompleted() {
                        verifyZeroInteractions(completedActionMock);
                    }
                }
            }
        }
    }

    public class ClosePositionTests {

        private Completable closePositionCompletable;

        @Before
        public void setUp() {
            closePositionCompletable = positionUtil.closePosition(instrumentEURUSD,
                                                                  mergeEURUSDCommandFactory,
                                                                  closeEURUSDCommandFactory);
        }

        private void expectFilledOrders(final Set<IOrder> filledOrders) {
            when(positionEURUSDMock.filled()).thenReturn(filledOrders);
        }

        private void expectFilledOrOpenedOrders(final Set<IOrder> filledOrOpenedOrders) {
            when(positionEURUSDMock.filledOrOpened()).thenReturn(filledOrOpenedOrders);
        }

        @Test
        public void completableIsDeferredWithNoInteractionsToMocks() {
            verifyDeferredCompletable();
        }

        public class NoFilledOrdersForPositionAvailable {

            @Before
            public void setUp() {
                expectFilledOrders(Sets.newHashSet());
            }

            public class NoOpenedOrdersForPositionAvailable {

                @Before
                public void setUp() {
                    expectFilledOrOpenedOrders(Sets.newHashSet());

                    closePositionCompletable.subscribe(completedActionMock);
                }

                @Test
                public void closePositionCompletedImmediately() {
                    verify(completedActionMock).call();
                }

                @Test
                public void noMergeCallOnOrderUtilCompletable() {
                    verify(orderUtilCompletableMock, never()).mergeOrders(any());
                }

                @Test
                public void noCloseCommandCall() {
                    verify(closeEURUSDCommandFactory, never()).apply(any());
                }
            }

            public class TwoOpenedOrdersForPosition {

                @Before
                public void setUp() {
                    expectFilledOrOpenedOrders(Sets.newHashSet(buyOrderEURUSD, sellOrderEURUSD));
                    when(closeEURUSDCommandMock.completable()).thenReturn(emptyCompletable());
                    when(closeEURUSDCommandFactory.apply(buyOrderEURUSD)).thenReturn(closeEURUSDCommandMock);
                    when(closeEURUSDCommandFactory.apply(sellOrderEURUSD)).thenReturn(closeEURUSDCommandMock);

                    closePositionCompletable.subscribe(completedActionMock);
                }

                @Test
                public void closePositionCompleted() {
                    verify(completedActionMock).call();
                }

                @Test
                public void noMergeCallOnOrderUtilCompletable() {
                    verify(orderUtilCompletableMock, never()).mergeOrders(any());
                }

                @Test
                public void closeCommandCallsForBothOrders() {
                    verify(closeEURUSDCommandFactory).apply(buyOrderEURUSD);
                    verify(closeEURUSDCommandFactory).apply(sellOrderEURUSD);
                }
            }
        }

        public class OneFilledOrderForPosition {

            @Before
            public void setUp() {
                expectFilledOrders(Sets.newHashSet(buyOrderEURUSD));
            }

            public class NoOpenedOrdersForPositionAvailable {

                @Before
                public void setUp() {
                    expectFilledOrOpenedOrders(Sets.newHashSet(buyOrderEURUSD));
                    when(closeEURUSDCommandMock.completable()).thenReturn(emptyCompletable());
                    when(closeEURUSDCommandFactory.apply(buyOrderEURUSD)).thenReturn(closeEURUSDCommandMock);

                    closePositionCompletable.subscribe(completedActionMock);
                }

                @Test
                public void closePositionCompleted() {
                    verify(completedActionMock).call();
                }

                @Test
                public void noMergeCallOnOrderUtilCompletable() {
                    verify(orderUtilCompletableMock, never()).mergeOrders(any());
                }

                @Test
                public void closeCommandCallForFilledOrder() {
                    verify(closeEURUSDCommandFactory).apply(buyOrderEURUSD);
                }
            }

            public class OneOpenedOrderForPosition {

                @Before
                public void setUp() {
                    expectFilledOrOpenedOrders(Sets.newHashSet(buyOrderEURUSD, sellOrderEURUSD));
                    when(closeEURUSDCommandMock.completable()).thenReturn(emptyCompletable());
                    when(closeEURUSDCommandFactory.apply(buyOrderEURUSD)).thenReturn(closeEURUSDCommandMock);
                    when(closeEURUSDCommandFactory.apply(sellOrderEURUSD)).thenReturn(closeEURUSDCommandMock);

                    closePositionCompletable.subscribe(completedActionMock);
                }

                @Test
                public void closePositionCompleted() {
                    verify(completedActionMock).call();
                }

                @Test
                public void noMergeCallOnOrderUtilCompletable() {
                    verify(orderUtilCompletableMock, never()).mergeOrders(any());
                }

                @Test
                public void closeCommandCallsForBothOrders() {
                    verify(closeEURUSDCommandFactory).apply(buyOrderEURUSD);
                    verify(closeEURUSDCommandFactory).apply(sellOrderEURUSD);
                }
            }
        }

        public class TwoFilledOrderForPosition {

            @Before
            public void setUp() {
                final Set<IOrder> toMergeOrders = Sets.newHashSet(buyOrderEURUSD, buyOrderEURUSD2);
                when(mergeEURUSDCommandFactory.apply(toMergeOrders)).thenReturn(mergeEURUSDCommandMock);
                when(orderUtilCompletableMock.mergeOrders(mergeEURUSDCommandMock))
                    .thenReturn(emptyCompletable());

                expectFilledOrders(Sets.newHashSet(buyOrderEURUSD, buyOrderEURUSD2));
            }

            public class TwoOpenedOrdersForPositionAvailable {

                @Before
                public void setUp() {
                    expectFilledOrOpenedOrders(Sets.newHashSet(buyOrderEURUSD, sellOrderEURUSD));
                    when(closeEURUSDCommandMock.completable()).thenReturn(emptyCompletable());
                    when(closeEURUSDCommandFactory.apply(buyOrderEURUSD)).thenReturn(closeEURUSDCommandMock);
                    when(closeEURUSDCommandFactory.apply(sellOrderEURUSD)).thenReturn(closeEURUSDCommandMock);

                    closePositionCompletable.subscribe(completedActionMock);
                }

                @Test
                public void closePositionCompleted() {
                    verify(completedActionMock).call();
                }

                @Test
                public void mergeCallOnOrderUtilCompletable() {
                    verify(orderUtilCompletableMock).mergeOrders(mergeEURUSDCommandMock);
                }

                @Test
                public void closeCommandCallForBothOrders() {
                    verify(closeEURUSDCommandFactory).apply(buyOrderEURUSD);
                    verify(closeEURUSDCommandFactory).apply(sellOrderEURUSD);
                }
            }

            public class TwoOpenedOrdersWithMergeNotCompleted {

                @Before
                public void setUp() {
                    expectFilledOrOpenedOrders(Sets.newHashSet(buyOrderEURUSD, sellOrderEURUSD));
                    when(closeEURUSDCommandMock.completable()).thenReturn(emptyCompletable());
                    when(closeEURUSDCommandFactory.apply(buyOrderEURUSD)).thenReturn(closeEURUSDCommandMock);
                    when(closeEURUSDCommandFactory.apply(sellOrderEURUSD)).thenReturn(closeEURUSDCommandMock);
                    when(orderUtilCompletableMock.mergeOrders(mergeEURUSDCommandMock))
                        .thenReturn(neverCompletable());

                    closePositionCompletable.subscribe(completedActionMock);
                }

                @Test
                public void closePositionNotCompleted() {
                    verifyZeroInteractions(completedActionMock);
                }

                @Test
                public void mergeCallOnOrderUtilCompletable() {
                    verify(orderUtilCompletableMock).mergeOrders(mergeEURUSDCommandMock);
                }

                @Test
                public void noCloseCommandCalls() {
                    verifyZeroInteractions(closeEURUSDCommandFactory);
                }
            }
        }
    }

    public class CloseAllPositionsTests {

        private Completable closeAllPositionsCompletable;

        @Before
        public void setUp() {
            closeAllPositionsCompletable = positionUtil.closeAllPositions(mergeCommandFactory, closeCommandFactory);
        }

        @Test
        public void completableIsDeferredWithNoInteractionsToMocks() {
            verifyDeferredCompletable();
        }

        @Test
        public void onSubscribeWithNoPositionsCompletesImmediately() {
            expectPositions(Sets.newHashSet());

            closeAllPositionsCompletable.subscribe(completedActionMock);

            verifyZeroInteractions(orderUtilCompletableMock);
            verify(completedActionMock).call();
        }

        public class TwoPositionsPresent {

            private Completable closeEURUSDCompletable;
            private Completable closeAUDUSDCompletable;

            @Before
            public void setUp() {
                expectPositions(Sets.newHashSet(positionEURUSDMock, positionAUDUSDMock));
            }

            private void verifyClosePositionIsCalledForBothPositions() {
                verify(positionUtil).closePosition(instrumentEURUSD, mergeCommandFactory, closeCommandFactory);
                verify(positionUtil).closePosition(instrumentAUDUSD, mergeCommandFactory, closeCommandFactory);
            }

            private void completeClosePositionEURUSDSetup() {
                closeEURUSDCompletable = emptyCompletable().doOnSubscribe(onSubscribeEURUSDAction);
                completeClosePositionWithCommand(instrumentEURUSD,
                                                 closeEURUSDCompletable);
            }

            private void completeClosePositionAUDUSDSetup() {
                closeAUDUSDCompletable = emptyCompletable().doOnSubscribe(onSubscribeAUDUSDAction);
                completeClosePositionWithCommand(instrumentAUDUSD,
                                                 closeAUDUSDCompletable);
            }

            private void completeClosePositionWithCommand(final Instrument instrument,
                                                          final Completable completable) {
                when(positionUtil.closePosition(instrument, mergeCommandFactory, closeCommandFactory))
                    .thenReturn(completable);
            }

            private void neverCompleteClosePositionEURUSDSetup() {
                closeEURUSDCompletable = neverCompletable().doOnSubscribe(onSubscribeEURUSDAction);
                neverCompleteClosePositionWithCommand(instrumentEURUSD,
                                                      closeEURUSDCompletable);
            }

            private void neverCompleteClosePositionAUDUSDSetup() {
                closeAUDUSDCompletable = neverCompletable().doOnSubscribe(onSubscribeAUDUSDAction);
                neverCompleteClosePositionWithCommand(instrumentAUDUSD,
                                                      closeAUDUSDCompletable);
            }

            private void neverCompleteClosePositionWithCommand(final Instrument instrument,
                                                               final Completable completable) {
                when(positionUtil.closePosition(instrument, mergeCommandFactory, closeCommandFactory))
                    .thenReturn(completable);
            }

            @Test
            public void testThatCloseCompletablesAreMerged() {
                neverCompleteClosePositionEURUSDSetup();
                neverCompleteClosePositionAUDUSDSetup();

                closeAllPositionsCompletable.subscribe(completedActionMock);

                verify(onSubscribeEURUSDAction).call(any());
                verify(onSubscribeAUDUSDAction).call(any());
            }

            public class ClosePositionEURUSDCompletes {

                @Before
                public void setUp() {
                    completeClosePositionEURUSDSetup();
                }

                public class ClosePositionAUDUSDCompletes {

                    @Before
                    public void setUp() {
                        completeClosePositionAUDUSDSetup();

                        closeAllPositionsCompletable.subscribe(completedActionMock);
                    }

                    @Test
                    public void closePositionIsCalledForBothPositions() {
                        verifyClosePositionIsCalledForBothPositions();
                    }

                    @Test
                    public void closePositionCompleted() {
                        verify(completedActionMock).call();
                    }
                }

                public class ClosePositionAUDUSDDoesNotComplete {

                    @Before
                    public void setUp() {
                        neverCompleteClosePositionAUDUSDSetup();

                        closeAllPositionsCompletable.subscribe(completedActionMock);
                    }

                    @Test
                    public void closePositionIsCalledForBothPositions() {
                        verifyClosePositionIsCalledForBothPositions();
                    }

                    @Test
                    public void closePositionNotCompleted() {
                        verifyZeroInteractions(completedActionMock);
                    }
                }
            }

            public class ClosePositionEURUSDDoesNotComplete {

                @Before
                public void setUp() {
                    neverCompleteClosePositionEURUSDSetup();
                }

                public class ClosePositionAUDUSDCompletes {

                    @Before
                    public void setUp() {
                        completeClosePositionAUDUSDSetup();

                        closeAllPositionsCompletable.subscribe(completedActionMock);
                    }

                    @Test
                    public void closePositionIsCalledForBothPositions() {
                        verifyClosePositionIsCalledForBothPositions();
                    }

                    @Test
                    public void closePositionNotCompleted() {
                        verifyZeroInteractions(completedActionMock);
                    }
                }

                public class ClosePositionAUDUSDDoesNotComplete {

                    @Before
                    public void setUp() {
                        neverCompleteClosePositionAUDUSDSetup();

                        closeAllPositionsCompletable.subscribe(completedActionMock);
                    }

                    @Test
                    public void closePositionIsCalledForBothPositions() {
                        verifyClosePositionIsCalledForBothPositions();
                    }

                    @Test
                    public void closePositionNotCompleted() {
                        verifyZeroInteractions(completedActionMock);
                    }
                }
            }
        }
    }
}
