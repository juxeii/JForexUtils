package com.jforex.programming.order.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.Collection;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import com.dukascopy.api.IOrder;
import com.google.common.collect.Sets;
import com.jforex.programming.order.MergeCommand;
import com.jforex.programming.order.OrderTask;
import com.jforex.programming.order.OrderUtil;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.position.ClosePositionCommand;
import com.jforex.programming.position.PositionOrders;
import com.jforex.programming.position.PositionTask;
import com.jforex.programming.test.common.InstrumentUtilForTest;

import de.bechte.junit.runners.context.HierarchicalContextRunner;
import io.reactivex.Observable;

@RunWith(HierarchicalContextRunner.class)
public class OrderUtilTest extends InstrumentUtilForTest {

    private OrderUtil orderUtil;

    @Mock private OrderTask orderTaskMock;
    @Mock private PositionTask positionTaskMock;
    private final IOrder orderForTest = buyOrderEURUSD;
    private Observable<OrderEvent> orderEventObservable;

    @Before
    public void setUp() {
        orderUtil = new OrderUtil(orderTaskMock, positionTaskMock);
    }

    @Test
    public void submitOrderDelegatesToOrderTask() {
        when(orderTaskMock.submitOrder(buyParamsEURUSD))
            .thenReturn(orderEventObservable);

        final Observable<OrderEvent> actualObservable = orderUtil.submitOrder(buyParamsEURUSD);

        verify(orderTaskMock).submitOrder(buyParamsEURUSD);
        assertThat(actualObservable, equalTo(orderEventObservable));
    }

    @Test
    public void mergeOrdersDelegatesToOrderTask() {
        final MergeCommand command = mock(MergeCommand.class);
        final Collection<IOrder> toMergeOrders = Sets.newHashSet(buyOrderEURUSD, sellOrderEURUSD);
        when(orderTaskMock.mergeOrders(toMergeOrders, command))
            .thenReturn(orderEventObservable);

        final Observable<OrderEvent> actualObservable = orderUtil.mergeOrders(toMergeOrders, command);

        verify(orderTaskMock).mergeOrders(toMergeOrders, command);
        assertThat(actualObservable, equalTo(orderEventObservable));
    }

    @Test
    public void closeDelegatesToOrderTask() {
        when(orderTaskMock.close(orderForTest))
            .thenReturn(orderEventObservable);

        final Observable<OrderEvent> actualObservable = orderUtil.close(orderForTest);

        verify(orderTaskMock).close(orderForTest);
        assertThat(actualObservable, equalTo(orderEventObservable));
    }

    @Test
    public void setLabelDelegatesToOrderTask() {
        final String newLabel = "newLabel";
        when(orderTaskMock.setLabel(orderForTest, newLabel))
            .thenReturn(orderEventObservable);

        final Observable<OrderEvent> actualObservable = orderUtil.setLabel(orderForTest, newLabel);

        verify(orderTaskMock).setLabel(orderForTest, newLabel);
        assertThat(actualObservable, equalTo(orderEventObservable));
    }

    @Test
    public void setGTTDelegatesToOrderTask() {
        final long newGTT = 1L;
        when(orderTaskMock.setGoodTillTime(orderForTest, newGTT))
            .thenReturn(orderEventObservable);

        final Observable<OrderEvent> actualObservable = orderUtil.setGoodTillTime(orderForTest, newGTT);

        verify(orderTaskMock).setGoodTillTime(orderForTest, newGTT);
        assertThat(actualObservable, equalTo(orderEventObservable));
    }

    @Test
    public void setRequestedAmountDelegatesToOrderTask() {
        final double newRequestedAmount = 0.12;
        when(orderTaskMock.setRequestedAmount(orderForTest, newRequestedAmount))
            .thenReturn(orderEventObservable);

        final Observable<OrderEvent> actualObservable = orderUtil.setRequestedAmount(orderForTest, newRequestedAmount);

        verify(orderTaskMock).setRequestedAmount(orderForTest, newRequestedAmount);
        assertThat(actualObservable, equalTo(orderEventObservable));
    }

    @Test
    public void setOpenPriceDelegatesToOrderTask() {
        final double newOpenPrice = 1.1234;
        when(orderTaskMock.setOpenPrice(orderForTest, newOpenPrice))
            .thenReturn(orderEventObservable);

        final Observable<OrderEvent> actualObservable = orderUtil.setOpenPrice(orderForTest, newOpenPrice);

        verify(orderTaskMock).setOpenPrice(orderForTest, newOpenPrice);
        assertThat(actualObservable, equalTo(orderEventObservable));
    }

    @Test
    public void setSLDelegatesToOrderTask() {
        final double newSL = 1.1234;
        when(orderTaskMock.setStopLossPrice(orderForTest, newSL))
            .thenReturn(orderEventObservable);

        final Observable<OrderEvent> actualObservable = orderUtil.setStopLossPrice(orderForTest, newSL);

        verify(orderTaskMock).setStopLossPrice(orderForTest, newSL);
        assertThat(actualObservable, equalTo(orderEventObservable));
    }

    @Test
    public void setTPDelegatesToOrderTask() {
        final double newTP = 1.1234;
        when(orderTaskMock.setTakeProfitPrice(orderForTest, newTP))
            .thenReturn(orderEventObservable);

        final Observable<OrderEvent> actualObservable = orderUtil.setTakeProfitPrice(orderForTest, newTP);

        verify(orderTaskMock).setTakeProfitPrice(orderForTest, newTP);
        assertThat(actualObservable, equalTo(orderEventObservable));
    }

    @Test
    public void mergePositionDelegatesToPositionTask() {
        final MergeCommand command = mock(MergeCommand.class);

        when(positionTaskMock.merge(instrumentEURUSD, command))
            .thenReturn(orderEventObservable);

        final Observable<OrderEvent> actualObservable = orderUtil.mergePosition(instrumentEURUSD, command);

        verify(positionTaskMock).merge(instrumentEURUSD, command);
        assertThat(actualObservable, equalTo(orderEventObservable));
    }

    @Test
    public void closePositionDelegatesToPositionTask() {
        final ClosePositionCommand command = mock(ClosePositionCommand.class);

        when(positionTaskMock.close(command))
            .thenReturn(orderEventObservable);

        final Observable<OrderEvent> actualObservable = orderUtil.closePosition(command);

        verify(positionTaskMock).close(command);
        assertThat(actualObservable, equalTo(orderEventObservable));
    }

    @Test
    public void positionOrdersDelegatesToPositionTask() {
        final PositionOrders positionOrders = mock(PositionOrders.class);
        when(positionTaskMock.positionOrders(instrumentEURUSD))
            .thenReturn(positionOrders);

        final PositionOrders actualPositionOrders = orderUtil.positionOrders(instrumentEURUSD);

        verify(positionTaskMock).positionOrders(instrumentEURUSD);
        assertThat(actualPositionOrders, equalTo(positionOrders));
    }
}
