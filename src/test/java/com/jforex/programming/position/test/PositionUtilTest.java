package com.jforex.programming.position.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Set;
import java.util.function.Function;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import com.dukascopy.api.IOrder;
import com.dukascopy.api.Instrument;
import com.google.common.collect.Sets;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.position.Position;
import com.jforex.programming.position.PositionDirection;
import com.jforex.programming.position.PositionFactory;
import com.jforex.programming.position.PositionUtil;
import com.jforex.programming.test.common.InstrumentUtilForTest;

import de.bechte.junit.runners.context.HierarchicalContextRunner;
import io.reactivex.Observable;
import io.reactivex.functions.Action;

@RunWith(HierarchicalContextRunner.class)
public class PositionUtilTest extends InstrumentUtilForTest {

    private PositionUtil positionUtil;

    @Mock
    private PositionFactory positionFactoryMock;
    @Mock
    private Position positionEURUSDMock;
    @Mock
    private Position positionAUDUSDMock;
    @Mock
    private Action actionMock;
    private final Set<IOrder> testOrders = Sets.newHashSet(buyOrderEURUSD, sellOrderEURUSD);

    @Before
    public void setUp() {
        setUpMocks();

        positionUtil = new PositionUtil(positionFactoryMock);
    }

    private void setUpMocks() {
        when(positionFactoryMock.forInstrument(instrumentEURUSD))
            .thenReturn(positionEURUSDMock);

        when(positionEURUSDMock.instrument()).thenReturn(instrumentEURUSD);
        when(positionAUDUSDMock.instrument()).thenReturn(instrumentAUDUSD);
    }

    @Test
    public void createIsCorrect() {
        positionUtil.create(instrumentEURUSD);

        verify(positionFactoryMock).forInstrument(instrumentEURUSD);
    }

    @Test
    public void positionOrdersIsCorrect() {
        assertThat(positionUtil.positionOrders(instrumentEURUSD), equalTo(positionEURUSDMock));
    }

    @Test
    public void filledOrdersAreCorrect() {
        when(positionEURUSDMock.filled()).thenReturn(testOrders);

        assertThat(positionUtil.filledOrders(instrumentEURUSD), equalTo(testOrders));
    }

    @Test
    public void filledOrOpenedOrdersAreCorrect() {
        when(positionEURUSDMock.filledOrOpened()).thenReturn(testOrders);

        assertThat(positionUtil.filledOrOpenedOrders(instrumentEURUSD), equalTo(testOrders));
    }

    @Test
    public void openedOrdersAreCorrect() {
        when(positionEURUSDMock.opened()).thenReturn(testOrders);

        assertThat(positionUtil.openedOrders(instrumentEURUSD), equalTo(testOrders));
    }

    @Test
    public void directionForSignedAmountIsLONGForPositiveAmount() {
        assertThat(PositionUtil.directionForSignedAmount(0.12), equalTo(PositionDirection.LONG));
    }

    @Test
    public void directionForSignedAmountIsSHORTForNegativeAmount() {
        assertThat(PositionUtil.directionForSignedAmount(-0.12), equalTo(PositionDirection.SHORT));
    }

    @Test
    public void directionForSignedAmountIsFLATForZeroAmount() {
        assertThat(PositionUtil.directionForSignedAmount(0.0), equalTo(PositionDirection.FLAT));
    }

    public class ObservablesFromFactoryTests {

        private final Observable<OrderEvent> observableForEURUSD = emptyObservable();
        private final Observable<OrderEvent> observableForAUDUSD = neverObservable();

        private final Function<Instrument, Observable<OrderEvent>> paramsFactory =
                instrument -> instrument == instrumentEURUSD
                        ? observableForEURUSD
                        : observableForAUDUSD;

        @Test
        public void returnsEmptyListForNoPositions() {
            when(positionFactoryMock.all()).thenReturn(Sets.newHashSet());

            final List<Observable<OrderEvent>> observables = positionUtil.observablesFromFactory(paramsFactory);

            assertTrue(observables.isEmpty());
        }

        @Test
        public void returnsCorrectObservablesList() {
            when(positionFactoryMock.all()).thenReturn(Sets.newHashSet(positionEURUSDMock, positionAUDUSDMock));

            final List<Observable<OrderEvent>> observables = positionUtil.observablesFromFactory(paramsFactory);

            assertThat(observables.size(), equalTo(2));
            assertTrue(observables.contains(observableForEURUSD));
            assertTrue(observables.contains(observableForAUDUSD));
        }
    }
}
