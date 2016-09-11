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
import com.jforex.programming.order.command.CloseCommand;
import com.jforex.programming.order.command.MergeCommand;
import com.jforex.programming.position.Position;
import com.jforex.programming.position.PositionClose;
import com.jforex.programming.position.PositionFactory;
import com.jforex.programming.position.PositionMerge;
import com.jforex.programming.position.PositionOrders;
import com.jforex.programming.position.PositionUtil;
import com.jforex.programming.test.common.InstrumentUtilForTest;

import de.bechte.junit.runners.context.HierarchicalContextRunner;
import rx.Completable;

@RunWith(HierarchicalContextRunner.class)
public class PositionUtilTest extends InstrumentUtilForTest {

    private PositionUtil positionUtil;

    @Mock
    private PositionMerge positionMergeMock;
    @Mock
    private PositionClose positionCloseMock;
    @Mock
    private PositionFactory positionFactoryMock;
    @Mock
    private Completable completableMock;
    @Mock
    private Function<Set<IOrder>, MergeCommand> mergeCommandFactory;
    @Mock
    private Function<IOrder, CloseCommand> closeCommandFactory;

    @Before
    public void setUp() {
        positionUtil = new PositionUtil(positionMergeMock,
                                        positionCloseMock,
                                        positionFactoryMock);
    }

    @Test
    public void positionOrdersReturnsCorrectInstance() {
        final Position positionMock = mock(Position.class);
        when(positionFactoryMock.forInstrument(instrumentEURUSD))
            .thenReturn(positionMock);

        final PositionOrders positionOrders = positionUtil.positionOrders(instrumentEURUSD);

        assertThat(positionOrders, equalTo(positionMock));
    }

    @Test
    public void mergeCallIsInvokingOnPositionMerge() {
        when(positionMergeMock.merge(instrumentEURUSD, mergeCommandFactory))
            .thenReturn(completableMock);

        final Completable completable = positionUtil.merge(instrumentEURUSD, mergeCommandFactory);

        verify(positionMergeMock).merge(instrumentEURUSD, mergeCommandFactory);
        assertThat(completable, equalTo(completableMock));
    }

    @Test
    public void mergeAllCallIsInvokingOnPositionMerge() {
        when(positionMergeMock.mergeAll(mergeCommandFactory))
            .thenReturn(completableMock);

        final Completable completable = positionUtil.mergeAll(mergeCommandFactory);

        verify(positionMergeMock).mergeAll(mergeCommandFactory);
        assertThat(completable, equalTo(completableMock));
    }

    @Test
    public void closeCallIsInvokingOnPositionMerge() {
        when(positionCloseMock.closePosition(instrumentEURUSD,
                                             mergeCommandFactory,
                                             closeCommandFactory))
                                                 .thenReturn(completableMock);

        final Completable completable = positionUtil.close(instrumentEURUSD,
                                                           mergeCommandFactory,
                                                           closeCommandFactory);

        verify(positionCloseMock).closePosition(instrumentEURUSD,
                                                mergeCommandFactory,
                                                closeCommandFactory);
        assertThat(completable, equalTo(completableMock));
    }

    @Test
    public void closeAllCallIsInvokingOnPositionMerge() {
        when(positionCloseMock.closeAllPositions(mergeCommandFactory, closeCommandFactory))
            .thenReturn(completableMock);

        final Completable completable = positionUtil.closeAll(mergeCommandFactory, closeCommandFactory);

        verify(positionCloseMock).closeAllPositions(mergeCommandFactory, closeCommandFactory);
        assertThat(completable, equalTo(completableMock));
    }
}
