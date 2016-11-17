package com.jforex.programming.order.task.test;

import static org.hamcrest.Matchers.hasItems;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.dukascopy.api.IOrder;
import com.google.common.collect.Sets;
import com.jforex.programming.order.task.CloseExecutionMode;
import com.jforex.programming.order.task.OrdersForPositionClose;
import com.jforex.programming.order.task.params.position.ClosePositionParams;
import com.jforex.programming.position.PositionUtil;
import com.jforex.programming.test.common.QuoteProviderForTest;

public class OrdersForPositionCloseTest extends QuoteProviderForTest {

    private OrdersForPositionClose ordersForPositionClose;

    @Mock
    private PositionUtil positionUtilMock;
    @Mock
    private ClosePositionParams closePositionParamsMock;
    private List<IOrder> ordersToClose;

    @Before
    public void setUp() {
        setUpMocks();

        ordersForPositionClose = new OrdersForPositionClose(positionUtilMock);
    }

    private void setUpMocks() {
        when(closePositionParamsMock.instrument()).thenReturn(instrumentEURUSD);
    }

    private void setCloseExecutionMode(final CloseExecutionMode mode) {
        when(closePositionParamsMock.closeExecutionMode())
            .thenReturn(mode);
    }

    private void assertOrders(final IOrder... orders) {
        assertThat(ordersToClose, hasItems(orders));
    }

    private void receiveOrders() {
        ordersToClose = new ArrayList<>(ordersForPositionClose.forMode(closePositionParamsMock));
    }

    @Test
    public void FilledOrdersIsCorrect() {
        final Set<IOrder> filledOrders = Sets.newHashSet(buyOrderEURUSD);
        setCloseExecutionMode(CloseExecutionMode.CloseFilled);
        when(positionUtilMock.filledOrders(instrumentEURUSD)).thenReturn(filledOrders);

        ordersToClose = new ArrayList<>(ordersForPositionClose.filled(instrumentEURUSD));

        assertOrders(buyOrderEURUSD);
    }

    @Test
    public void getIsCorrectForFilledOrders() {
        final Set<IOrder> filledOrders = Sets.newHashSet(buyOrderEURUSD);
        setCloseExecutionMode(CloseExecutionMode.CloseFilled);
        when(positionUtilMock.filledOrders(instrumentEURUSD)).thenReturn(filledOrders);

        receiveOrders();

        assertOrders(buyOrderEURUSD);
    }

    @Test
    public void getIsCorrectForOpenedOrders() {
        final Set<IOrder> openedOrders = Sets.newHashSet(buyOrderEURUSD, sellOrderEURUSD);
        setCloseExecutionMode(CloseExecutionMode.CloseOpened);
        when(positionUtilMock.openedOrders(instrumentEURUSD)).thenReturn(openedOrders);

        receiveOrders();

        assertOrders(buyOrderEURUSD, sellOrderEURUSD);
    }

    @Test
    public void getIsCorrectForFilledOrOpenedOrders() {
        final Set<IOrder> filledOrOpenedOrders = Sets.newHashSet(buyOrderEURUSD, sellOrderEURUSD);
        setCloseExecutionMode(CloseExecutionMode.CloseAll);
        when(positionUtilMock.filledOrOpenedOrders(instrumentEURUSD)).thenReturn(filledOrOpenedOrders);

        receiveOrders();

        assertOrders(buyOrderEURUSD, sellOrderEURUSD);
    }
}
