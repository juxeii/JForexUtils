package com.jforex.programming.position.test;

import java.util.Set;
import java.util.function.Function;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import com.dukascopy.api.IOrder;
import com.google.common.collect.Sets;
import com.jforex.programming.order.OrderUtilCompletable;
import com.jforex.programming.order.command.MergeCommand;
import com.jforex.programming.position.Position;
import com.jforex.programming.position.PositionFactory;
import com.jforex.programming.position.PositionUtil;
import com.jforex.programming.test.common.InstrumentUtilForTest;

import de.bechte.junit.runners.context.HierarchicalContextRunner;
import rx.Completable;
import rx.functions.Action0;

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
    private MergeCommand mergeCommandMock;

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
    }

    public class MergePositionTests {

        private Completable mergePositionCompletable;
        private final Action0 completedActionMock = mock(Action0.class);

        @Before
        public void setUp() {
            mergePositionCompletable = positionUtil.mergePosition(instrumentEURUSD, mergeCommandFactory);
        }

        private void setUtilCompletableResult(final Completable completable) {
            when(orderUtilCompletableMock.mergeOrders(mergeCommandMock))
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
            when(mergeCommandFactory.apply(toMergeOrders)).thenReturn(mergeCommandMock);
            setUtilCompletableResult(emptyCompletable());

            mergePositionCompletable.subscribe(completedActionMock);

            verify(orderUtilCompletableMock).mergeOrders(mergeCommandMock);
            verify(completedActionMock).call();
        }
    }

    public class MergeAllPositionsTests {

        private Completable mergeAllPositionsCompletable;
        private final Action0 completedActionMock = mock(Action0.class);

        @Before
        public void setUp() {
            mergeAllPositionsCompletable = positionUtil.mergeAllPositions(mergeCommandFactory);
        }

        private void expectPositions(final Set<Position> positions) {
            when(positionFactoryMock.allPositions()).thenReturn(positions);
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
            verify(positionUtil, never()).mergePosition(any(), eq(mergeCommandFactory));
            verify(completedActionMock).call();
        }

        public class TwoPositionsPresent {

            @Before
            public void setUp() {
                expectPositions(Sets.newHashSet(positionEURUSDMock, positionAUDUSDMock));
            }

            public class PositionEURUSDCompletes {

                @Before
                public void setUp() {
                    when(positionUtil.mergePosition(instrumentEURUSD, mergeCommandFactory))
                        .thenReturn(emptyCompletable());
                }

                private void verifyMergeOrdersIsCalledForBothPositions() {
                    verify(positionUtil).mergePosition(instrumentEURUSD, mergeCommandFactory);
                    verify(positionUtil).mergePosition(instrumentAUDUSD, mergeCommandFactory);
                }

                public class SubscribeAndPositionAUDUSDCompletes {

                    @Before
                    public void setUp() {
                        when(positionUtil.mergePosition(instrumentAUDUSD, mergeCommandFactory))
                            .thenReturn(emptyCompletable());

                        mergeAllPositionsCompletable.subscribe(completedActionMock);
                    }

                    @Test
                    public void mergeOrdersIsCalledForBothPositions() {
                        verifyMergeOrdersIsCalledForBothPositions();
                    }

                    @Test
                    public void mergePositionCompleted() {
                        verify(completedActionMock).call();
                    }
                }

                public class SubscribeAndPositionAUDUSDDoesNotComplete {

                    @Before
                    public void setUp() {
                        when(positionUtil.mergePosition(instrumentAUDUSD, mergeCommandFactory))
                            .thenReturn(neverCompletable());

                        mergeAllPositionsCompletable.subscribe(completedActionMock);
                    }

                    @Test
                    public void mergeOrdersIsCalledForBothPositions() {
                        verifyMergeOrdersIsCalledForBothPositions();
                    }

                    @Test
                    public void mergePositionNotCompleted() {
                        verify(completedActionMock, never()).call();
                    }
                }
            }
        }
    }
}
