package com.jforex.programming.order.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import com.dukascopy.api.IOrder;
import com.dukascopy.api.Instrument;
import com.google.common.collect.Sets;
import com.jforex.programming.order.OrderBasicTask;
import com.jforex.programming.order.OrderCloseTask;
import com.jforex.programming.order.OrderMergeTask;
import com.jforex.programming.order.OrderUtil;
import com.jforex.programming.order.command.ClosePositionCommand;
import com.jforex.programming.order.command.MergeCommand;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.position.PositionOrders;
import com.jforex.programming.position.PositionUtil;
import com.jforex.programming.test.common.InstrumentUtilForTest;

import de.bechte.junit.runners.context.HierarchicalContextRunner;
import io.reactivex.Observable;
import io.reactivex.functions.Function;

@RunWith(HierarchicalContextRunner.class)
public class OrderUtilTest extends InstrumentUtilForTest {

    private OrderUtil orderUtil;

    @Mock
    private OrderBasicTask orderBasicTaskMock;
    @Mock
    private OrderMergeTask orderMergeTaskMock;
    @Mock
    private OrderCloseTask orderCloseTaskMock;
    @Mock
    private PositionUtil positionUtilMock;
    @Mock
    private MergeCommand mergeCommandMock;
    private final IOrder orderForTest = buyOrderEURUSD;
    private Observable<OrderEvent> orderEventObservable;
    private final Set<IOrder> toMergeOrders = Sets.newHashSet(buyOrderEURUSD, sellOrderEURUSD);

    @Before
    public void setUp() {
        orderUtil = new OrderUtil(orderBasicTaskMock,
                                  orderMergeTaskMock,
                                  orderCloseTaskMock,
                                  positionUtilMock);
    }

    @Test
    public void submitOrderDelegatesToOrderBasicTask() {
        when(orderBasicTaskMock.submitOrder(buyParamsEURUSD))
            .thenReturn(orderEventObservable);

        final Observable<OrderEvent> actualObservable = orderUtil.submitOrder(buyParamsEURUSD);

        verify(orderBasicTaskMock).submitOrder(buyParamsEURUSD);
        assertThat(actualObservable, equalTo(orderEventObservable));
    }

    @Test
    public void mergeOrdersDelegatesToOrderBasicTask() {
        final String mergeOrderLabel = "mergeOrderLabel";
        when(orderBasicTaskMock.mergeOrders(mergeOrderLabel, toMergeOrders))
            .thenReturn(orderEventObservable);

        final Observable<OrderEvent> actualObservable = orderUtil.mergeOrders(mergeOrderLabel, toMergeOrders);

        verify(orderBasicTaskMock).mergeOrders(mergeOrderLabel, toMergeOrders);
        assertThat(actualObservable, equalTo(orderEventObservable));
    }

    @Test
    public void mergeOrdersWithCommandDelegatesToMergeTask() {
        when(orderMergeTaskMock.merge(toMergeOrders, mergeCommandMock))
            .thenReturn(orderEventObservable);

        final Observable<OrderEvent> actualObservable = orderUtil.mergeOrders(toMergeOrders, mergeCommandMock);

        verify(orderMergeTaskMock).merge(toMergeOrders, mergeCommandMock);
        assertThat(actualObservable, equalTo(orderEventObservable));
    }

    @Test
    public void closeDelegatesToOrderTask() {
        when(orderBasicTaskMock.close(orderForTest))
            .thenReturn(orderEventObservable);

        final Observable<OrderEvent> actualObservable = orderUtil.close(orderForTest);

        verify(orderBasicTaskMock).close(orderForTest);
        assertThat(actualObservable, equalTo(orderEventObservable));
    }

    @Test
    public void setLabelDelegatesToOrderTask() {
        final String newLabel = "newLabel";
        when(orderBasicTaskMock.setLabel(orderForTest, newLabel))
            .thenReturn(orderEventObservable);

        final Observable<OrderEvent> actualObservable = orderUtil.setLabel(orderForTest, newLabel);

        verify(orderBasicTaskMock).setLabel(orderForTest, newLabel);
        assertThat(actualObservable, equalTo(orderEventObservable));
    }

    @Test
    public void setGTTDelegatesToOrderTask() {
        final long newGTT = 1L;
        when(orderBasicTaskMock.setGoodTillTime(orderForTest, newGTT))
            .thenReturn(orderEventObservable);

        final Observable<OrderEvent> actualObservable = orderUtil.setGoodTillTime(orderForTest, newGTT);

        verify(orderBasicTaskMock).setGoodTillTime(orderForTest, newGTT);
        assertThat(actualObservable, equalTo(orderEventObservable));
    }

    @Test
    public void setRequestedAmountDelegatesToOrderTask() {
        final double newRequestedAmount = 0.12;
        when(orderBasicTaskMock.setRequestedAmount(orderForTest, newRequestedAmount))
            .thenReturn(orderEventObservable);

        final Observable<OrderEvent> actualObservable = orderUtil.setRequestedAmount(orderForTest, newRequestedAmount);

        verify(orderBasicTaskMock).setRequestedAmount(orderForTest, newRequestedAmount);
        assertThat(actualObservable, equalTo(orderEventObservable));
    }

    @Test
    public void setOpenPriceDelegatesToOrderTask() {
        final double newOpenPrice = 1.1234;
        when(orderBasicTaskMock.setOpenPrice(orderForTest, newOpenPrice))
            .thenReturn(orderEventObservable);

        final Observable<OrderEvent> actualObservable = orderUtil.setOpenPrice(orderForTest, newOpenPrice);

        verify(orderBasicTaskMock).setOpenPrice(orderForTest, newOpenPrice);
        assertThat(actualObservable, equalTo(orderEventObservable));
    }

    @Test
    public void setSLDelegatesToOrderTask() {
        final double newSL = 1.1234;
        when(orderBasicTaskMock.setStopLossPrice(orderForTest, newSL))
            .thenReturn(orderEventObservable);

        final Observable<OrderEvent> actualObservable = orderUtil.setStopLossPrice(orderForTest, newSL);

        verify(orderBasicTaskMock).setStopLossPrice(orderForTest, newSL);
        assertThat(actualObservable, equalTo(orderEventObservable));
    }

    @Test
    public void setTPDelegatesToOrderTask() {
        final double newTP = 1.1234;
        when(orderBasicTaskMock.setTakeProfitPrice(orderForTest, newTP))
            .thenReturn(orderEventObservable);

        final Observable<OrderEvent> actualObservable = orderUtil.setTakeProfitPrice(orderForTest, newTP);

        verify(orderBasicTaskMock).setTakeProfitPrice(orderForTest, newTP);
        assertThat(actualObservable, equalTo(orderEventObservable));
    }

    @Test
    public void mergePositionDelegatesToMergeTask() {
        when(orderMergeTaskMock.mergePosition(instrumentEURUSD, mergeCommandMock))
            .thenReturn(orderEventObservable);

        final Observable<OrderEvent> actualObservable = orderUtil.mergePosition(instrumentEURUSD, mergeCommandMock);

        verify(orderMergeTaskMock).mergePosition(instrumentEURUSD, mergeCommandMock);
        assertThat(actualObservable, equalTo(orderEventObservable));
    }

    @Test
    public void mergeAllPositionsDelegatesToMergeTask() {
        final Function<Instrument, MergeCommand> commandFactory = i -> null;

        when(orderMergeTaskMock.mergeAll(commandFactory))
            .thenReturn(orderEventObservable);

        final Observable<OrderEvent> actualObservable = orderUtil.mergeAllPositions(commandFactory);

        verify(orderMergeTaskMock).mergeAll(commandFactory);
        assertThat(actualObservable, equalTo(orderEventObservable));
    }

    @Test
    public void closePositionDelegatesToCloseTask() {
        final ClosePositionCommand command = mock(ClosePositionCommand.class);

        when(orderCloseTaskMock.close(command))
            .thenReturn(orderEventObservable);

        final Observable<OrderEvent> actualObservable = orderUtil.closePosition(command);

        verify(orderCloseTaskMock).close(command);
        assertThat(actualObservable, equalTo(orderEventObservable));
    }

    @Test
    public void closeAllPositionsDelegatesToCloseTask() {
        final Function<Instrument, ClosePositionCommand> commandFactory = i -> null;

        when(orderCloseTaskMock.closeAll(commandFactory))
            .thenReturn(orderEventObservable);

        final Observable<OrderEvent> actualObservable = orderUtil.closeAllPositions(commandFactory);

        verify(orderCloseTaskMock).closeAll(commandFactory);
        assertThat(actualObservable, equalTo(orderEventObservable));
    }

    @Test
    public void positionOrdersDelegatesToPositionTask() {
        final PositionOrders positionOrders = mock(PositionOrders.class);
        when(positionUtilMock.positionOrders(instrumentEURUSD))
            .thenReturn(positionOrders);

        final PositionOrders actualPositionOrders = orderUtil.positionOrders(instrumentEURUSD);

        verify(positionUtilMock).positionOrders(instrumentEURUSD);
        assertThat(actualPositionOrders, equalTo(positionOrders));
    }
}
