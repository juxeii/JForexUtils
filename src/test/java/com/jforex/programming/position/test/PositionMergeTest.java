package com.jforex.programming.position.test;

import java.util.Collection;
import java.util.Set;
import java.util.function.Function;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import com.dukascopy.api.IOrder;
import com.google.common.collect.Sets;
import com.jforex.programming.order.OrderUtilCompletable;
import com.jforex.programming.order.command.Command;
import com.jforex.programming.order.command.option.MergeOption;
import com.jforex.programming.position.Position;
import com.jforex.programming.position.PositionFactory;
import com.jforex.programming.position.PositionMerge;
import com.jforex.programming.test.common.InstrumentUtilForTest;

import de.bechte.junit.runners.context.HierarchicalContextRunner;
import io.reactivex.Completable;
import io.reactivex.subscribers.TestSubscriber;

@RunWith(HierarchicalContextRunner.class)
public class PositionMergeTest extends InstrumentUtilForTest {

    private PositionMerge positionMerge;

    @Mock
    private OrderUtilCompletable orderUtilCompletableMock;
    @Mock
    private PositionFactory positionFactoryMock;
    @Mock
    private Position positionMock;
    @Mock
    private MergeOption mergeOption;
    @Mock
    private Function<Collection<IOrder>, MergeOption> mergeOptionFactory;
    @Mock
    private Command commandMock;
    private TestSubscriber<Void> testSubscriber;

    @Before
    public void setUp() {
        setUpMocks();

        positionMerge = new PositionMerge(orderUtilCompletableMock, positionFactoryMock);
    }

    private void setUpMocks() {
        when(mergeOptionFactory.apply(any())).thenReturn(mergeOption);
        when(mergeOption.build()).thenReturn(commandMock);
    }

    private void setUpMergePositionCompletables(final Completable firstCompletable,
                                                final Completable... completables) {
        when(orderUtilCompletableMock.forCommandWithOrderMarking(eq(commandMock), any()))
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
            mergePositionCompletable = positionMerge.merge(instrumentEURUSD, mergeOptionFactory);
        }

        private void expectFilledOrders(final Set<IOrder> filledOrders) {
            positionMock = orderUtilForTest.createPositionMock(positionFactoryMock, instrumentEURUSD, filledOrders);
        }

        @Test
        public void completableIsDeferredWithNoInteractionsToMocks() {
            verifyDeferredCompletable();
        }

        public class OnSubscribe {

            public void prepareToMergeOrdersAndSubscribe(final Set<IOrder> toMergeOrders) {
                expectFilledOrders(toMergeOrders);
                setUpMergePositionCompletables(emptyCompletable());

                testSubscriber = mergePositionCompletable.test();
            }

            @Test
            public void onSubscribeCompletesImmediatelyWhenNoOrdersForMerge() {
                prepareToMergeOrdersAndSubscribe(Sets.newHashSet());

                testSubscriber.assertComplete();
                verifyZeroInteractions(orderUtilCompletableMock);
            }

            @Test
            public void onSubscribeCompletesImmediatelyWhenOnlyOneOrderForMerge() {
                prepareToMergeOrdersAndSubscribe(Sets.newHashSet(buyOrderEURUSD));

                verifyZeroInteractions(orderUtilCompletableMock);
                testSubscriber.assertComplete();
            }

            @Test
            public void onSubscribeCallsOnOrderUtilWhenEnoughOrdersForMerge() {
                final Set<IOrder> toMergeOrders = Sets.newHashSet(buyOrderEURUSD, sellOrderEURUSD);
                prepareToMergeOrdersAndSubscribe(toMergeOrders);

                verify(orderUtilCompletableMock)
                    .forCommandWithOrderMarking(eq(commandMock), any());
                testSubscriber.assertComplete();
            }
        }
    }

    public class MergeAllPositionsTests {

        private Completable mergeAllPositionsCompletable;

        @Before
        public void setUp() {
            mergeAllPositionsCompletable = positionMerge.mergeAll(mergeOptionFactory);
        }

        @Test
        public void completableIsDeferredWithNoInteractionsToMocks() {
            verifyDeferredCompletable();
        }

        @Test
        public void onSubscribeWithNoPositionsCompletesImmediately() throws Exception {
            expectPositions(Sets.newHashSet());

            testSubscriber = mergeAllPositionsCompletable.test();

            verifyZeroInteractions(orderUtilCompletableMock);
            testSubscriber.assertComplete();
        }

        public class TwoPositionsPresent {

            @Before
            public void setUp() {
                final Position positionOneMock = orderUtilForTest
                    .createPositionMock(positionFactoryMock,
                                        instrumentEURUSD,
                                        Sets.newHashSet(buyOrderEURUSD, sellOrderEURUSD));
                final Position positionTwoMock = orderUtilForTest
                    .createPositionMock(positionFactoryMock,
                                        instrumentAUDUSD,
                                        Sets.newHashSet(buyOrderAUDUSD, sellOrderAUDUSD));

                expectPositions(Sets.newHashSet(positionOneMock, positionTwoMock));

                orderUtilForTest.createPositionMock(positionFactoryMock, instrumentEURUSD,
                                                    Sets.newHashSet(buyOrderEURUSD, sellOrderEURUSD));
                orderUtilForTest.createPositionMock(positionFactoryMock, instrumentAUDUSD,
                                                    Sets.newHashSet(buyOrderEURUSD, sellOrderEURUSD));
            }

            @Test
            public void testThatMergeCompletablesAreNotConcatenated() {
                setUpMergePositionCompletables(neverCompletable(), neverCompletable());

                testSubscriber = mergeAllPositionsCompletable.test();

                verify(orderUtilCompletableMock, times(2))
                    .forCommandWithOrderMarking(any(), any());
                testSubscriber.assertNotComplete();
            }

            @Test
            public void whenBothPositionsAreMergedTheCallIsCompleted() throws Exception {
                setUpMergePositionCompletables(emptyCompletable(), emptyCompletable());

                testSubscriber = mergeAllPositionsCompletable.test();

                testSubscriber.assertComplete();
            }

            @Test
            public void whenFirstButNotSecondCompletesTheCallIsNotCompleted() {
                setUpMergePositionCompletables(emptyCompletable(), neverCompletable());

                testSubscriber = mergeAllPositionsCompletable.test();

                testSubscriber.assertNotComplete();
            }

            @Test
            public void whenSecondButNotFirstCompletesTheCallIsNotCompleted() {
                setUpMergePositionCompletables(neverCompletable(), emptyCompletable());

                testSubscriber = mergeAllPositionsCompletable.test();

                testSubscriber.assertNotComplete();
            }
        }
    }
}
