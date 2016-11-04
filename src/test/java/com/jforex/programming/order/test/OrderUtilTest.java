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
        orderEventObservable = Observable.just(submitEvent);
        when(basicTaskMock.submitOrder(buyParamsEURUSD))
            .thenReturn(orderEventObservable);

        orderUtil
            .submitOrder(buyParamsEURUSD)
            .start();

        verify(basicTaskMock).submitOrder(buyParamsEURUSD);
    }

    @Test
    public void mergeOrdersDelegatesToOrderBasicTask() {
        final String mergeOrderLabel = "mergeOrderLabel";
        orderEventObservable = Observable.just(mergeEvent);
        when(basicTaskMock.mergeOrders(mergeOrderLabel, toMergeOrders))
            .thenReturn(orderEventObservable);

        orderUtil
            .mergeOrders(mergeOrderLabel, toMergeOrders)
            .start();

        verify(basicTaskMock).mergeOrders(mergeOrderLabel, toMergeOrders);
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
        orderEventObservable = Observable.just(closeEvent);
        when(basicTaskMock.close(orderForTest))
            .thenReturn(orderEventObservable);

        orderUtil
            .close(orderForTest)
            .start();

        verify(basicTaskMock).close(orderForTest);
    }

    @Test
    public void closeWithParamsDelegatesToBasicTask() {
        final CloseParams closeParams = CloseParams
            .newBuilder(orderForTest)
            .build();

        orderEventObservable = Observable.just(closeEvent);
        when(basicTaskMock.close(closeParams))
            .thenReturn(orderEventObservable);

        orderUtil
            .close(closeParams)
            .start();

        verify(basicTaskMock).close(closeParams);
    }

    @Test
    public void setLabelDelegatesToOrderTask() {
        final String newLabel = "newLabel";
        orderEventObservable = Observable.just(changedLabelEvent);
        when(basicTaskMock.setLabel(orderForTest, newLabel))
            .thenReturn(orderEventObservable);

        orderUtil
            .setLabel(orderForTest, newLabel)
            .start();

        verify(basicTaskMock).setLabel(orderForTest, newLabel);
    }

    @Test
    public void setGTTDelegatesToOrderTask() {
        final long newGTT = 1L;
        orderEventObservable = Observable.just(changedGTTEvent);
        when(basicTaskMock.setGoodTillTime(orderForTest, newGTT))
            .thenReturn(orderEventObservable);

        orderUtil
            .setGoodTillTime(orderForTest, newGTT)
            .start();

        verify(basicTaskMock).setGoodTillTime(orderForTest, newGTT);
    }

    @Test
    public void setRequestedAmountDelegatesToOrderTask() {
        final double newRequestedAmount = 0.12;
        orderEventObservable = Observable.just(changedAmountEvent);
        when(basicTaskMock.setRequestedAmount(orderForTest, newRequestedAmount))
            .thenReturn(orderEventObservable);

        orderUtil
            .setRequestedAmount(orderForTest, newRequestedAmount)
            .start();

        verify(basicTaskMock).setRequestedAmount(orderForTest, newRequestedAmount);
    }

    @Test
    public void setOpenPriceDelegatesToOrderTask() {
        final double newOpenPrice = 1.1234;
        orderEventObservable = Observable.just(changedOpenPriceEvent);
        when(basicTaskMock.setOpenPrice(orderForTest, newOpenPrice))
            .thenReturn(orderEventObservable);

        orderUtil
            .setOpenPrice(orderForTest, newOpenPrice)
            .start();

        verify(basicTaskMock).setOpenPrice(orderForTest, newOpenPrice);
    }

    @Test
    public void setSLDelegatesToOrderTask() {
        final double newSL = 1.1234;
        orderEventObservable = Observable.just(changedSLEvent);
        when(basicTaskMock.setStopLossPrice(orderForTest, newSL))
            .thenReturn(orderEventObservable);

        orderUtil
            .setStopLossPrice(orderForTest, newSL)
            .start();

        verify(basicTaskMock).setStopLossPrice(orderForTest, newSL);
    }

    @Test
    public void setSLForPipsDelegatesToBasicTask() {
        final double pips = 12.3;
        orderEventObservable = Observable.just(changedSLEvent);
        when(basicTaskMock.setStopLossForPips(orderForTest, pips))
            .thenReturn(orderEventObservable);

        orderUtil
            .setStopLossForPips(orderForTest, pips)
            .start();

        verify(basicTaskMock).setStopLossForPips(orderForTest, pips);
    }

    @Test
    public void setSLWithParamsDelegatesToBasicTask() {
        final double newSL = 1.1234;
        orderEventObservable = Observable.just(changedSLEvent);
        final SetSLParams setSLParams = SetSLParams
            .newBuilder(orderForTest, newSL)
            .build();

        when(basicTaskMock.setStopLossPrice(setSLParams))
            .thenReturn(orderEventObservable);

        orderUtil
            .setStopLossPrice(setSLParams)
            .start();

        verify(basicTaskMock).setStopLossPrice(setSLParams);
    }

    @Test
    public void setTPDelegatesToOrderTask() {
        final double newTP = 1.1234;
        orderEventObservable = Observable.just(changedTPEvent);
        when(basicTaskMock.setTakeProfitPrice(orderForTest, newTP))
            .thenReturn(orderEventObservable);

        orderUtil
            .setTakeProfitPrice(orderForTest, newTP)
            .start();

        verify(basicTaskMock).setTakeProfitPrice(orderForTest, newTP);
    }

    @Test
    public void setTPForPipsDelegatesToBasicTask() {
        final double pips = 12.3;
        orderEventObservable = Observable.just(changedTPEvent);
        when(basicTaskMock.setTakeProfitForPips(orderForTest, pips))
            .thenReturn(orderEventObservable);

        orderUtil
            .setTakeProfitForPips(orderForTest, pips)
            .start();

        verify(basicTaskMock).setTakeProfitForPips(orderForTest, pips);
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
