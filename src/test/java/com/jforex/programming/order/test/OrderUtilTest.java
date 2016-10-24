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
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.task.BasicTask;
import com.jforex.programming.order.task.CloseTask;
import com.jforex.programming.order.task.MergeTask;
import com.jforex.programming.order.task.params.CloseParams;
import com.jforex.programming.order.task.params.ClosePositionParams;
import com.jforex.programming.order.task.params.MergeParams;
import com.jforex.programming.order.task.params.SetSLParams;
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
    private MergeParams mergepositionParamsMock;
    @Mock
    private Function<Instrument, MergeParams> mergePositionParamsFactory;
    @Mock
    private Function<Instrument, ClosePositionParams> closePositionParamsFactory;
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
    public void mergeOrdersWithpositionParamsDelegatesToMergeTask() {
        when(orderMergeTaskMock.merge(toMergeOrders, mergepositionParamsMock))
            .thenReturn(orderEventObservable);

        final Observable<OrderEvent> actualObservable = orderUtil.mergeOrders(toMergeOrders, mergepositionParamsMock);

        verify(orderMergeTaskMock).merge(toMergeOrders, mergepositionParamsMock);
        assertThat(actualObservable, equalTo(orderEventObservable));
    }

    @Test
    public void closeDelegatesToOrderTask() {
        when(basicTaskMock.close(orderForTest))
            .thenReturn(orderEventObservable);

        final Observable<OrderEvent> actualObservable = orderUtil.close(orderForTest);

        verify(basicTaskMock).close(orderForTest);
        assertThat(actualObservable, equalTo(orderEventObservable));
    }

    @Test
    public void closeWithParamsDelegatesToBasicTask() {
        final CloseParams closeParams = CloseParams
            .newBuilder(orderForTest)
            .build();

        when(basicTaskMock.close(closeParams))
            .thenReturn(orderEventObservable);

        final Observable<OrderEvent> actualObservable =
                orderUtil.close(closeParams);

        verify(basicTaskMock).close(closeParams);
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
    public void setSLDelegatesToOrderTask() {
        final double newSL = 1.1234;
        when(basicTaskMock.setStopLossPrice(orderForTest, newSL))
            .thenReturn(orderEventObservable);

        final Observable<OrderEvent> actualObservable = orderUtil.setStopLossPrice(orderForTest, newSL);

        verify(basicTaskMock).setStopLossPrice(orderForTest, newSL);
        assertThat(actualObservable, equalTo(orderEventObservable));
    }

    @Test
    public void setSLWithParamsDelegatesToBasicTask() {
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
        when(orderMergeTaskMock.mergePosition(instrumentEURUSD, mergepositionParamsMock))
            .thenReturn(orderEventObservable);

        final Observable<OrderEvent> actualObservable =
                orderUtil.mergePosition(instrumentEURUSD, mergepositionParamsMock);

        verify(orderMergeTaskMock).mergePosition(instrumentEURUSD, mergepositionParamsMock);
        assertThat(actualObservable, equalTo(orderEventObservable));
    }

    @Test
    public void mergeAllPositionsDelegatesToMergeTask() {
        when(orderMergeTaskMock.mergeAllPositions(mergePositionParamsFactory))
            .thenReturn(orderEventObservable);

        final Observable<OrderEvent> actualObservable = orderUtil.mergeAllPositions(mergePositionParamsFactory);

        verify(orderMergeTaskMock).mergeAllPositions(mergePositionParamsFactory);
        assertThat(actualObservable, equalTo(orderEventObservable));
    }

    @Test
    public void closePositionDelegatesToCloseTask() {
        final ClosePositionParams positionParams = mock(ClosePositionParams.class);

        when(orderCloseTaskMock.close(positionParams))
            .thenReturn(orderEventObservable);

        final Observable<OrderEvent> actualObservable = orderUtil.closePosition(positionParams);

        verify(orderCloseTaskMock).close(positionParams);
        assertThat(actualObservable, equalTo(orderEventObservable));
    }

    @Test
    public void closeAllPositionsDelegatesToCloseTask() {
        when(orderCloseTaskMock.closeAllPositions(closePositionParamsFactory))
            .thenReturn(orderEventObservable);

        final Observable<OrderEvent> actualObservable = orderUtil.closeAllPositions(closePositionParamsFactory);

        verify(orderCloseTaskMock).closeAllPositions(closePositionParamsFactory);
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
