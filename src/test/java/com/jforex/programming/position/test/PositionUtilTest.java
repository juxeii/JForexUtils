package com.jforex.programming.position.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.Collection;
import java.util.function.Function;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.command.option.CloseOption;
import com.jforex.programming.order.command.option.MergeOption;
import com.jforex.programming.position.Position;
import com.jforex.programming.position.PositionClose;
import com.jforex.programming.position.PositionFactory;
import com.jforex.programming.position.PositionMerge;
import com.jforex.programming.position.PositionOrders;
import com.jforex.programming.position.PositionUtil;
import com.jforex.programming.test.common.InstrumentUtilForTest;

import de.bechte.junit.runners.context.HierarchicalContextRunner;
import io.reactivex.Completable;

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
    private Function<Collection<IOrder>, MergeOption> mergeOption;
    @Mock
    private Function<IOrder, CloseOption> closeOption;

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
        when(positionMergeMock.merge(instrumentEURUSD, mergeOption))
            .thenReturn(completableMock);

        final Completable completable = positionUtil.merge(instrumentEURUSD, mergeOption);

        verify(positionMergeMock).merge(instrumentEURUSD, mergeOption);
        assertThat(completable, equalTo(completableMock));
    }

    @Test
    public void mergeAllCallIsInvokingOnPositionMerge() {
        when(positionMergeMock.mergeAll(mergeOption))
            .thenReturn(completableMock);

        final Completable completable = positionUtil.mergeAll(mergeOption);

        verify(positionMergeMock).mergeAll(mergeOption);
        assertThat(completable, equalTo(completableMock));
    }

    @Test
    public void closeCallIsInvokingOnPositionMerge() {
        when(positionCloseMock.close(instrumentEURUSD,
                                     mergeOption,
                                     closeOption))
                                         .thenReturn(completableMock);

        final Completable completable = positionUtil.close(instrumentEURUSD,
                                                           mergeOption,
                                                           closeOption);

        verify(positionCloseMock).close(instrumentEURUSD,
                                        mergeOption,
                                        closeOption);
        assertThat(completable, equalTo(completableMock));
    }

    @Test
    public void closeAllCallIsInvokingOnPositionMerge() {
        when(positionCloseMock.closeAll(mergeOption, closeOption))
            .thenReturn(completableMock);

        final Completable completable = positionUtil.closeAll(mergeOption, closeOption);

        verify(positionCloseMock).closeAll(mergeOption, closeOption);
        assertThat(completable, equalTo(completableMock));
    }
}
