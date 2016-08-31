package com.jforex.programming.order.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import com.dukascopy.api.IOrder;
import com.google.common.collect.Sets;
import com.jforex.programming.order.OrderUtil;
import com.jforex.programming.order.OrderUtilImpl;
import com.jforex.programming.order.process.ClosePositionProcess;
import com.jforex.programming.order.process.CloseProcess;
import com.jforex.programming.order.process.MergePositionProcess;
import com.jforex.programming.order.process.MergeProcess;
import com.jforex.programming.order.process.SetAmountProcess;
import com.jforex.programming.order.process.SetGTTProcess;
import com.jforex.programming.order.process.SetLabelProcess;
import com.jforex.programming.order.process.SetPriceProcess;
import com.jforex.programming.order.process.SetSLProcess;
import com.jforex.programming.order.process.SetTPProcess;
import com.jforex.programming.order.process.SubmitAndMergePositionProcess;
import com.jforex.programming.order.process.SubmitProcess;
import com.jforex.programming.position.Position;
import com.jforex.programming.test.common.InstrumentUtilForTest;

import de.bechte.junit.runners.context.HierarchicalContextRunner;

@RunWith(HierarchicalContextRunner.class)
public class OrderUtilTest extends InstrumentUtilForTest {

    private OrderUtil orderUtil;

    @Mock
    private OrderUtilImpl orderUtilImplMock;
    @Mock
    private Position positionMock;
    private final String mergeOrderLabel = "MergeLabel";
    private final Set<IOrder> toMergeOrders = Sets.newHashSet(buyOrderEURUSD, sellOrderEURUSD);

    @Before
    public void setUp() {
        orderUtil = new OrderUtil(orderUtilImplMock);
    }

    @Test
    public void positionOrdersReturnsCorrectInstance() {
        when(orderUtilImplMock.positionOrders(instrumentEURUSD))
            .thenReturn(positionMock);

        assertThat(orderUtil.positionOrders(instrumentEURUSD),
                   equalTo(positionMock));
    }

    @Test
    public void startSubmitDelegatesToOrderUtilImpl() {
        when(orderUtilImplMock.submitOrder(buyParamsEURUSD))
            .thenReturn(emptyObservable())
            .thenReturn(jfExceptionObservable());

        final SubmitProcess builder = SubmitProcess
            .forOrderParams(buyParamsEURUSD)
            .build();

        orderUtil.startSubmit(builder);
        orderUtil.startSubmit(builder);

        verify(orderUtilImplMock, times(2)).submitOrder(buyParamsEURUSD);
    }

    @Test
    public void submitAndMergePositionDelegatesToOrderUtilImpl() {
        when(orderUtilImplMock.submitAndMergePosition(mergeOrderLabel, buyParamsEURUSD))
            .thenReturn(emptyObservable())
            .thenReturn(jfExceptionObservable());

        final SubmitAndMergePositionProcess process = SubmitAndMergePositionProcess
            .forParams(buyParamsEURUSD, mergeOrderLabel)
            .build();

        orderUtil.startSubmitAndMergePosition(process);

        verify(orderUtilImplMock).submitAndMergePosition(mergeOrderLabel, buyParamsEURUSD);
    }

    @Test
    public void submitAndMergePositionToParamsDelegatesToOrderUtilImpl() {
        when(orderUtilImplMock.submitAndMergePositionToParams(mergeOrderLabel, buyParamsEURUSD))
            .thenReturn(emptyObservable())
            .thenReturn(jfExceptionObservable());

        final SubmitAndMergePositionProcess process = SubmitAndMergePositionProcess
            .forParams(buyParamsEURUSD, mergeOrderLabel)
            .build();

        orderUtil.startSubmitAndMergePositionToParams(process);

        verify(orderUtilImplMock).submitAndMergePositionToParams(mergeOrderLabel, buyParamsEURUSD);
    }

    @Test
    public void mergeOrdersDelegatesToOrderUtilImpl() {
        when(orderUtilImplMock.mergeOrders(mergeOrderLabel, toMergeOrders))
            .thenReturn(emptyObservable())
            .thenReturn(jfExceptionObservable());

        final MergeProcess builder = MergeProcess
            .forParams(mergeOrderLabel, toMergeOrders)
            .build();

        orderUtil.startMerge(builder);
        orderUtil.startMerge(builder);

        verify(orderUtilImplMock, times(2)).mergeOrders(mergeOrderLabel, toMergeOrders);
    }

    @Test
    public void mergePositionOrdersDelegatesToOrderUtilImpl() {
        when(orderUtilImplMock.mergePositionOrders(mergeOrderLabel, instrumentEURUSD))
            .thenReturn(emptyObservable())
            .thenReturn(jfExceptionObservable());

        final MergePositionProcess process = MergePositionProcess
            .forParams(mergeOrderLabel, instrumentEURUSD)
            .build();

        orderUtil.startPositionMerge(process);
        orderUtil.startPositionMerge(process);

        verify(orderUtilImplMock, times(2)).mergePositionOrders(mergeOrderLabel, instrumentEURUSD);
    }

    @Test
    public void closePositionDelegatesToOrderUtilImpl() {
        when(orderUtilImplMock.closePosition(instrumentEURUSD))
            .thenReturn(emptyObservable())
            .thenReturn(jfExceptionObservable());

        final ClosePositionProcess builder = ClosePositionProcess
            .forInstrument(instrumentEURUSD)
            .build();

        orderUtil.startPositionClose(builder);
        orderUtil.startPositionClose(builder);

        verify(orderUtilImplMock, times(2)).closePosition(instrumentEURUSD);
    }

    @Test
    public void closeDelegatesToOrderUtilImpl() {
        when(orderUtilImplMock.close(buyOrderEURUSD))
            .thenReturn(neverObservable());

        final CloseProcess builder = CloseProcess
            .forOrder(buyOrderEURUSD)
            .build();

        orderUtil.startClose(builder);

        verify(orderUtilImplMock).close(buyOrderEURUSD);
    }

    @Test
    public void setLabelDelegatesToOrderUtilImpl() {
        final String newLabel = "newLabel";
        when(orderUtilImplMock.setLabel(buyOrderEURUSD, newLabel))
            .thenReturn(emptyObservable())
            .thenReturn(jfExceptionObservable());

        final SetLabelProcess builder = SetLabelProcess
            .forParams(buyOrderEURUSD, newLabel)
            .build();

        orderUtil.startLabelChange(builder);
        orderUtil.startLabelChange(builder);

        verify(orderUtilImplMock, times(2)).setLabel(buyOrderEURUSD, newLabel);
    }

    @Test
    public void setGTTDelegatesToOrderUtilImpl() {
        final long newGTT = 1L;
        when(orderUtilImplMock.setGoodTillTime(buyOrderEURUSD, newGTT))
            .thenReturn(neverObservable());

        final SetGTTProcess builder = SetGTTProcess
            .forParams(buyOrderEURUSD, newGTT)
            .build();

        orderUtil.startGTTChange(builder);

        verify(orderUtilImplMock).setGoodTillTime(buyOrderEURUSD, newGTT);
    }

    @Test
    public void setOpenPriceDelegatesToOrderUtilImpl() {
        final double newOpenPrice = 1.1234;
        when(orderUtilImplMock.setOpenPrice(buyOrderEURUSD, newOpenPrice))
            .thenReturn(neverObservable());

        final SetPriceProcess builder = SetPriceProcess
            .forParams(buyOrderEURUSD, newOpenPrice)
            .build();

        orderUtil.startOpenPriceChange(builder);

        verify(orderUtilImplMock).setOpenPrice(buyOrderEURUSD, newOpenPrice);
    }

    @Test
    public void setAmountDelegatesToOrderUtilImpl() {
        final double newAmount = 0.12;
        when(orderUtilImplMock.setRequestedAmount(buyOrderEURUSD, newAmount))
            .thenReturn(neverObservable());

        final SetAmountProcess builder = SetAmountProcess
            .forParams(buyOrderEURUSD, newAmount)
            .build();

        orderUtil.startAmountChange(builder);

        verify(orderUtilImplMock).setRequestedAmount(buyOrderEURUSD, newAmount);
    }

    @Test
    public void setSLDelegatesToOrderUtilImpl() {
        final double newSL = 1.1234;
        when(orderUtilImplMock.setStopLossPrice(buyOrderEURUSD, newSL))
            .thenReturn(neverObservable());

        final SetSLProcess builder = SetSLProcess
            .forParams(buyOrderEURUSD, newSL)
            .build();

        orderUtil.startSLChange(builder);

        verify(orderUtilImplMock).setStopLossPrice(buyOrderEURUSD, newSL);
    }

    @Test
    public void setTPDelegatesToOrderUtilImpl() {
        final double newTP = 1.1234;
        when(orderUtilImplMock.setTakeProfitPrice(buyOrderEURUSD, newTP))
            .thenReturn(neverObservable());

        final SetTPProcess builder = SetTPProcess
            .forParams(buyOrderEURUSD, newTP)
            .build();

        orderUtil.startTPChange(builder);

        verify(orderUtilImplMock).setTakeProfitPrice(buyOrderEURUSD, newTP);
    }
}
