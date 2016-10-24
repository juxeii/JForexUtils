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
import com.jforex.programming.order.OrderUtil;
import com.jforex.programming.order.command.CloseParams;
import com.jforex.programming.order.command.ClosePositionCommand;
import com.jforex.programming.order.command.MergeCommand;
import com.jforex.programming.order.command.SetSLParams;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.task.BasicTask;
import com.jforex.programming.order.task.CloseTask;
import com.jforex.programming.order.task.MergeTask;
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
    private BasicTask basicTaskMock;
    @Mock
    private MergeTask orderMergeTaskMock;
    @Mock
    private CloseTask orderCloseTaskMock;
    @Mock
    private PositionUtil positionUtilMock;
    @Mock
    private CloseParams closeParamsMock;
    @Mock
    private MergeCommand mergeCommandMock;
    @Mock
    private Function<Instrument, MergeCommand> mergeCommandFactory;
    @Mock
    private Function<Instrument, ClosePositionCommand> closeCommandFactory;
    private final IOrder orderForTest = buyOrderEURUSD;
    private Observable<OrderEvent> orderEventObservable;
    private final Set<IOrder> toMergeOrders = Sets.newHashSet(buyOrderEURUSD, sellOrderEURUSD);

    @Before
    public void setUp() {
        orderUtil = new OrderUtil(basicTaskMock,
                                  orderMergeTaskMock,
                                  orderCloseTaskMock,
                                  positionUtilMock);
    }

    @Test
    public void submitOrderDelegatesToOrderBasicTask() {
        when(basicTaskMock.submitOrder(buyParamsEURUSD))
            .thenReturn(orderEventObservable);

        final Observable<OrderEvent> actualObservable = orderUtil.submitOrder(buyParamsEURUSD);

        verify(basicTaskMock).submitOrder(buyParamsEURUSD);
        assertThat(actualObservable, equalTo(orderEventObservable));
    }

    @Test
    public void mergeOrdersDelegatesToOrderBasicTask() {
        final String mergeOrderLabel = "mergeOrderLabel";
        when(basicTaskMock.mergeOrders(mergeOrderLabel, toMergeOrders))
            .thenReturn(orderEventObservable);

        final Observable<OrderEvent> actualObservable = orderUtil.mergeOrders(mergeOrderLabel, toMergeOrders);

        verify(basicTaskMock).mergeOrders(mergeOrderLabel, toMergeOrders);
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
    public void closeDelegatesToBasicTask() {
        when(basicTaskMock.close(closeParamsMock))
            .thenReturn(orderEventObservable);

        final Observable<OrderEvent> actualObservable =
                orderUtil.close(closeParamsMock);

        verify(basicTaskMock).close(closeParamsMock);
        assertThat(actualObservable, equalTo(orderEventObservable));
    }

    @Test
    public void setLabelDelegatesToOrderTask() {
        final String newLabel = "newLabel";
        when(basicTaskMock.setLabel(orderForTest, newLabel))
            .thenReturn(orderEventObservable);

        final Observable<OrderEvent> actualObservable = orderUtil.setLabel(orderForTest, newLabel);

        verify(basicTaskMock).setLabel(orderForTest, newLabel);
        assertThat(actualObservable, equalTo(orderEventObservable));
    }

    @Test
    public void setGTTDelegatesToOrderTask() {
        final long newGTT = 1L;
        when(basicTaskMock.setGoodTillTime(orderForTest, newGTT))
            .thenReturn(orderEventObservable);

        final Observable<OrderEvent> actualObservable = orderUtil.setGoodTillTime(orderForTest, newGTT);

        verify(basicTaskMock).setGoodTillTime(orderForTest, newGTT);
        assertThat(actualObservable, equalTo(orderEventObservable));
    }

    @Test
    public void setRequestedAmountDelegatesToOrderTask() {
        final double newRequestedAmount = 0.12;
        when(basicTaskMock.setRequestedAmount(orderForTest, newRequestedAmount))
            .thenReturn(orderEventObservable);

        final Observable<OrderEvent> actualObservable = orderUtil.setRequestedAmount(orderForTest, newRequestedAmount);

        verify(basicTaskMock).setRequestedAmount(orderForTest, newRequestedAmount);
        assertThat(actualObservable, equalTo(orderEventObservable));
    }

    @Test
    public void setOpenPriceDelegatesToOrderTask() {
        final double newOpenPrice = 1.1234;
        when(basicTaskMock.setOpenPrice(orderForTest, newOpenPrice))
            .thenReturn(orderEventObservable);

        final Observable<OrderEvent> actualObservable = orderUtil.setOpenPrice(orderForTest, newOpenPrice);

        verify(basicTaskMock).setOpenPrice(orderForTest, newOpenPrice);
        assertThat(actualObservable, equalTo(orderEventObservable));
    }

    @Test
    public void setSLDelegatesToBasicTask() {
        final double newSL = 1.1234;
        final SetSLParams setSLParams = SetSLParams
            .newBuilder(orderForTest, newSL)
            .build();

        when(basicTaskMock.setStopLossPrice(setSLParams))
            .thenReturn(orderEventObservable);

        final Observable<OrderEvent> actualObservable = orderUtil.setStopLossPrice(setSLParams);

        verify(basicTaskMock).setStopLossPrice(setSLParams);
        assertThat(actualObservable, equalTo(orderEventObservable));
    }

    @Test
    public void setTPDelegatesToOrderTask() {
        final double newTP = 1.1234;
        when(basicTaskMock.setTakeProfitPrice(orderForTest, newTP))
            .thenReturn(orderEventObservable);

        final Observable<OrderEvent> actualObservable = orderUtil.setTakeProfitPrice(orderForTest, newTP);

        verify(basicTaskMock).setTakeProfitPrice(orderForTest, newTP);
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
        when(orderMergeTaskMock.mergeAllPositions(mergeCommandFactory))
            .thenReturn(orderEventObservable);

        final Observable<OrderEvent> actualObservable = orderUtil.mergeAllPositions(mergeCommandFactory);

        verify(orderMergeTaskMock).mergeAllPositions(mergeCommandFactory);
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
        when(orderCloseTaskMock.closeAllPositions(closeCommandFactory))
            .thenReturn(orderEventObservable);

        final Observable<OrderEvent> actualObservable = orderUtil.closeAllPositions(closeCommandFactory);

        verify(orderCloseTaskMock).closeAllPositions(closeCommandFactory);
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
