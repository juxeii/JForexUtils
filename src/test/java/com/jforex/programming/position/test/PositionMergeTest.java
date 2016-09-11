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
import com.jforex.programming.position.PositionMerge;
import com.jforex.programming.test.common.InstrumentUtilForTest;

import de.bechte.junit.runners.context.HierarchicalContextRunner;
import rx.Completable;
import rx.functions.Action0;

@RunWith(HierarchicalContextRunner.class)
public class PositionMergeTest extends InstrumentUtilForTest {

    private PositionMerge positionMerge;

    @Mock
    private OrderUtilCompletable orderUtilCompletableMock;
    @Mock
    private PositionFactory positionFactoryMock;
    @Mock
    private Function<Set<IOrder>, MergeCommand> mergeCommandFactory;
    @Mock
    private MergeCommand mergeCommandMock;
    @Mock
    private Action0 completedActionMock;

    @Before
    public void setUp() {
        setUpMocks();

        positionMerge = new PositionMerge(orderUtilCompletableMock, positionFactoryMock);
    }

    private void setUpMocks() {
        when(mergeCommandFactory.apply(any())).thenReturn(mergeCommandMock);
    }

    private void setUpMergePositionCompletables(final Completable firstCompletable,
                                                final Completable... completables) {
        when(orderUtilCompletableMock.mergeOrders(mergeCommandMock))
            .thenReturn(firstCompletable, completables);
    }

    private void verifyDeferredCompletable() {
        verifyZeroInteractions(orderUtilCompletableMock);
        verifyZeroInteractions(positionFactoryMock);
    }

    private void expectPositions(final Set<Position> positions) {
        when(positionFactoryMock.all()).thenReturn(positions);
    }

    public class MergePositionTests {

        private Completable mergePositionCompletable;

        @Before
        public void setUp() {
            mergePositionCompletable = positionMerge.merge(instrumentEURUSD, mergeCommandFactory);
        }

        private void verifyInteractionsForNotEnoughMergeOrders() {
            verifyZeroInteractions(orderUtilCompletableMock);
            verify(completedActionMock).call();
        }

        private void expectFilledOrders(final Set<IOrder> filledOrders) {
            orderUtilForTest.setUpPositionFactory(positionFactoryMock, instrumentEURUSD, filledOrders);
        }

        @Test
        public void completableIsDeferredWithNoInteractionsToMocks() {
            verifyDeferredCompletable();
        }

        @Test
        public void onSubscribeWithNoMergeOrdersCompletesImmediately() {
            expectFilledOrders(Sets.newHashSet());

            mergePositionCompletable.subscribe(completedActionMock);

            verifyInteractionsForNotEnoughMergeOrders();
        }

        @Test
        public void onSubscribeWithOneMergeOrderCompletesImmediately() {
            expectFilledOrders(Sets.newHashSet(buyOrderEURUSD));

            mergePositionCompletable.subscribe(completedActionMock);

            verifyInteractionsForNotEnoughMergeOrders();
        }

        @Test
        public void onSubscribeOrderUtilCompletableIsCalled() {
            final Set<IOrder> toMergeOrders = Sets.newHashSet(buyOrderEURUSD, sellOrderEURUSD);
            expectFilledOrders(toMergeOrders);
            setUpMergePositionCompletables(emptyCompletable());

            mergePositionCompletable.subscribe(completedActionMock);

            verify(orderUtilCompletableMock).mergeOrders(mergeCommandMock);
            verify(completedActionMock).call();
        }
    }

    public class MergeAllPositionsTests {

        private Completable mergeAllPositionsCompletable;

        @Before
        public void setUp() {
            mergeAllPositionsCompletable = positionMerge.mergeAll(mergeCommandFactory);
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

            @Before
            public void setUp() {
                final Position positionOneMock = orderUtilForTest
                    .createPositionMock(instrumentEURUSD, Sets.newHashSet(buyOrderEURUSD, sellOrderEURUSD));
                final Position positionTwoMock = orderUtilForTest
                    .createPositionMock(instrumentAUDUSD, Sets.newHashSet(buyOrderAUDUSD, sellOrderAUDUSD));

                expectPositions(Sets.newHashSet(positionOneMock, positionTwoMock));

                orderUtilForTest.setUpPositionFactory(positionFactoryMock, instrumentEURUSD,
                                                      Sets.newHashSet(buyOrderEURUSD, sellOrderEURUSD));
                orderUtilForTest.setUpPositionFactory(positionFactoryMock, instrumentAUDUSD,
                                                      Sets.newHashSet(buyOrderEURUSD, sellOrderEURUSD));
            }

            @Test
            public void testThatMergeCompletablesAreNotConcatenated() {
                setUpMergePositionCompletables(neverCompletable(), neverCompletable());

                mergeAllPositionsCompletable.subscribe(completedActionMock);

                verify(orderUtilCompletableMock, times(2)).mergeOrders(mergeCommandMock);
                verifyZeroInteractions(completedActionMock);
            }

            @Test
            public void whenBothPositionsAreMergedTheCallIsCompleted() {
                setUpMergePositionCompletables(emptyCompletable(), emptyCompletable());

                mergeAllPositionsCompletable.subscribe(completedActionMock);

                verify(completedActionMock).call();
            }

            @Test
            public void whenFirstButNotSecondCompletesTheCallIsNotCompleted() {
                setUpMergePositionCompletables(emptyCompletable(), neverCompletable());

                mergeAllPositionsCompletable.subscribe(completedActionMock);

                verifyZeroInteractions(completedActionMock);
            }

            @Test
            public void whenSecondButNotFirstCompletesTheCallIsNotCompleted() {
                setUpMergePositionCompletables(neverCompletable(), emptyCompletable());

                mergeAllPositionsCompletable.subscribe(completedActionMock);

                verifyZeroInteractions(completedActionMock);
            }
        }
    }
}
