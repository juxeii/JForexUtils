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
import com.jforex.programming.order.command.Command;
import com.jforex.programming.order.command.CommandUtil;
import com.jforex.programming.order.command.option.CloseOption;
import com.jforex.programming.order.command.option.MergeOption;
import com.jforex.programming.position.Position;
import com.jforex.programming.position.PositionClose;
import com.jforex.programming.position.PositionFactory;
import com.jforex.programming.position.PositionMerge;
import com.jforex.programming.test.common.InstrumentUtilForTest;

import de.bechte.junit.runners.context.HierarchicalContextRunner;
import io.reactivex.Completable;
import io.reactivex.subscribers.TestSubscriber;

@RunWith(HierarchicalContextRunner.class)
public class PositionCloseTest extends InstrumentUtilForTest {

    private PositionClose positionClose;

    @Mock
    private PositionMerge positionMergeMock;
    @Mock
    private PositionFactory positionFactoryMock;
    @Mock
    private CommandUtil commandUtilMock;
    @Mock
    private MergeOption mergeOption;
    @Mock
    private CloseOption closeOption;
    @Mock
    private Function<Collection<IOrder>, MergeOption> mergeOptionFactory;
    @Mock
    private Function<IOrder, CloseOption> closeOptionFactory;
    @Mock
    private Command mergeCommandMock;
    @Mock
    private Command closeCommandMock;
    private TestSubscriber<Void> testSubscriber;

    @Before
    public void setUp() {
        setUpMocks();

        positionClose = new PositionClose(positionMergeMock,
                                          positionFactoryMock,
                                          commandUtilMock);
    }

    private void setUpMocks() {
        when(mergeOptionFactory.apply(any())).thenReturn(mergeOption);
        when(closeOptionFactory.apply(any())).thenReturn(closeOption);

        when(mergeOption.build()).thenReturn(mergeCommandMock);
        when(closeOption.build()).thenReturn(closeCommandMock);
    }

    private void setUpMergeCompletables(final Completable firstCompletable,
                                        final Completable... completables) {
        when(positionMergeMock.merge(any(), eq(mergeOptionFactory)))
            .thenReturn(firstCompletable, completables);
    }

    private void setUpCommandUtilCompletables(final Completable firstCompletable,
                                              final Completable... completables) {
        when(commandUtilMock.mergeFromOption(any(), eq(closeOptionFactory)))
            .thenReturn(firstCompletable, completables);
    }

    private void verifyDeferredCompletable() {
        verifyZeroInteractions(positionMergeMock);
        verifyZeroInteractions(positionFactoryMock);
        verifyZeroInteractions(commandUtilMock);
    }

    private void expectPositions(final Set<Position> positions) {
        when(positionFactoryMock.all()).thenReturn(positions);
    }

    public class ClosePositionTests {

        private Completable closePositionCompletable;

        @Before
        public void setUp() {
            closePositionCompletable = positionClose.close(instrumentEURUSD,
                                                           mergeOptionFactory,
                                                           closeOptionFactory);
        }

        private void expectFilledOrOpenedOrders(final Set<IOrder> filledOrders) {
            orderUtilForTest.createPositionMock(positionFactoryMock, instrumentEURUSD, filledOrders);
        }

        @Test
        public void completableIsDeferredWithNoInteractionsToMocks() {
            verifyDeferredCompletable();
        }

        @Test
        public void onSubscribeWithNoOrdersToCloseCompletesImmediately() throws Exception {
            final Set<IOrder> filledOrOpenedOrders = Sets.newHashSet();
            expectFilledOrOpenedOrders(filledOrOpenedOrders);
            setUpMergeCompletables(emptyCompletable());
            setUpCommandUtilCompletables(emptyCompletable());

            testSubscriber = closePositionCompletable.test();

            testSubscriber.assertComplete();
        }

        public class SubscribeSetup {

            private final Set<IOrder> filledOrOpenedOrders = Sets.newHashSet(buyOrderEURUSD, sellOrderEURUSD);

            @Before
            public void setUp() {
                expectFilledOrOpenedOrders(filledOrOpenedOrders);
            }

            @Test
            public void positionMergeCompletableIsCalled() {
                setUpMergeCompletables(emptyCompletable());
                setUpCommandUtilCompletables(emptyCompletable());

                testSubscriber = closePositionCompletable.test();

                testSubscriber.assertComplete();
                verify(positionMergeMock).merge(instrumentEURUSD, mergeOptionFactory);
                verify(commandUtilMock).mergeFromOption(filledOrOpenedOrders, closeOptionFactory);
            }

            @Test
            public void noQueryForFilledOrOpenedOrdersYet() {
                setUpMergeCompletables(neverCompletable());

                testSubscriber = closePositionCompletable.test();

                testSubscriber.assertNotComplete();
                verifyZeroInteractions(positionFactoryMock);
            }
        }
    }

    public class CloseAllPositionsTests {

        private Completable closeAllPositionsCompletable;

        @Before
        public void setUp() {
            closeAllPositionsCompletable = positionClose.closeAll(mergeOptionFactory, closeOptionFactory);
        }

        @Test
        public void completableIsDeferredWithNoInteractionsToMocks() {
            verifyDeferredCompletable();
        }

        @Test
        public void onSubscribeWithNoPositionsCompletesImmediately() throws Exception {
            expectPositions(Sets.newHashSet());

            testSubscriber = closeAllPositionsCompletable.test();

            verifyZeroInteractions(positionMergeMock);
            verifyZeroInteractions(commandUtilMock);
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

                orderUtilForTest.createPositionMock(positionFactoryMock,
                                                    instrumentEURUSD,
                                                    Sets.newHashSet(buyOrderEURUSD, sellOrderEURUSD));
                orderUtilForTest.createPositionMock(positionFactoryMock,
                                                    instrumentAUDUSD,
                                                    Sets.newHashSet(buyOrderEURUSD, sellOrderEURUSD));
            }

            @Test
            public void testThatCloseCompletablesAreNotConcatenated() {
                setUpMergeCompletables(neverCompletable(), neverCompletable());
                setUpCommandUtilCompletables(neverCompletable(), neverCompletable());

                testSubscriber = closeAllPositionsCompletable.test();

                testSubscriber.assertNotComplete();
                verify(positionMergeMock, times(2)).merge(any(), eq(mergeOptionFactory));
            }

            @Test
            public void whenBothPositionsAreClosedTheCallIsCompleted() throws Exception {
                setUpMergeCompletables(emptyCompletable(), emptyCompletable());
                setUpCommandUtilCompletables(emptyCompletable(), emptyCompletable());

                testSubscriber = closeAllPositionsCompletable.test();

                testSubscriber.assertComplete();
            }

            @Test
            public void whenFirstButNotSecondCompletesTheCallIsNotCompleted() {
                setUpMergeCompletables(emptyCompletable(), neverCompletable());

                testSubscriber = closeAllPositionsCompletable.test();

                testSubscriber.assertNotComplete();
            }

            @Test
            public void whenSecondButNotFirstCompletesTheCallIsNotCompleted() {
                setUpMergeCompletables(neverCompletable(), emptyCompletable());

                testSubscriber = closeAllPositionsCompletable.test();

                testSubscriber.assertNotComplete();
            }
        }
    }
}
