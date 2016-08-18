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
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.position.Position;
import com.jforex.programming.test.common.InstrumentUtilForTest;

import de.bechte.junit.runners.context.HierarchicalContextRunner;
import rx.observers.TestSubscriber;

@RunWith(HierarchicalContextRunner.class)
public class OrderUtilTest extends InstrumentUtilForTest {

    private OrderUtil orderUtil;

    @Mock
    private OrderUtilImpl orderUtilImplMock;
    @Mock
    private Position positionMock;
    private final TestSubscriber<OrderEvent> orderEventSubscriber = new TestSubscriber<>();
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
    public void submitOrderDelegatesToOrderUtilImpl() {
        when(orderUtilImplMock.submitOrder(buyParamsEURUSD))
            .thenReturn(emptyObservable())
            .thenReturn(jfExceptionObservable());

        orderUtil
            .submitOrder(buyParamsEURUSD)
            .subscribe(orderEventSubscriber);
        orderUtil
            .submitOrder(buyParamsEURUSD)
            .subscribe(orderEventSubscriber);

        verify(orderUtilImplMock, times(2)).submitOrder(buyParamsEURUSD);
    }

    @Test
    public void submitAndMergePositionDelegatesToOrderUtilImpl() {
        orderUtil.submitAndMergePosition(mergeOrderLabel, buyParamsEURUSD);

        verify(orderUtilImplMock).submitAndMergePosition(mergeOrderLabel, buyParamsEURUSD);
    }

    @Test
    public void submitAndMergePositionToParamsDelegatesToOrderUtilImpl() {
        orderUtil.submitAndMergePositionToParams(mergeOrderLabel, buyParamsEURUSD);

        verify(orderUtilImplMock).submitAndMergePositionToParams(mergeOrderLabel, buyParamsEURUSD);
    }

    @Test
    public void mergeOrdersDelegatesToOrderUtilImpl() {
        when(orderUtilImplMock.mergeOrders(mergeOrderLabel, toMergeOrders))
            .thenReturn(emptyObservable())
            .thenReturn(jfExceptionObservable());

        orderUtil
            .mergeOrders(mergeOrderLabel, toMergeOrders)
            .subscribe(orderEventSubscriber);
        orderUtil
            .mergeOrders(mergeOrderLabel, toMergeOrders)
            .subscribe(orderEventSubscriber);

        verify(orderUtilImplMock, times(2)).mergeOrders(mergeOrderLabel, toMergeOrders);
    }

    @Test
    public void mergePositionOrdersDelegatesToOrderUtilImpl() {
        when(orderUtilImplMock.mergePositionOrders(mergeOrderLabel, instrumentEURUSD))
            .thenReturn(emptyObservable())
            .thenReturn(jfExceptionObservable());

        orderUtil
            .mergePositionOrders(mergeOrderLabel, instrumentEURUSD)
            .subscribe(orderEventSubscriber);
        orderUtil
            .mergePositionOrders(mergeOrderLabel, instrumentEURUSD)
            .subscribe(orderEventSubscriber);

        verify(orderUtilImplMock, times(2)).mergePositionOrders(mergeOrderLabel, instrumentEURUSD);
    }

    @Test
    public void closePositionDelegatesToOrderUtilImpl() {
        when(orderUtilImplMock.closePosition(instrumentEURUSD))
            .thenReturn(emptyObservable())
            .thenReturn(jfExceptionObservable());

        orderUtil
            .closePosition(instrumentEURUSD)
            .subscribe(orderEventSubscriber);
        orderUtil
            .closePosition(instrumentEURUSD)
            .subscribe(orderEventSubscriber);

        verify(orderUtilImplMock, times(2)).closePosition(instrumentEURUSD);
    }

    @Test
    public void closeDelegatesToOrderUtilImpl() {
        when(orderUtilImplMock.close(buyOrderEURUSD))
            .thenReturn(neverObservable());

        orderUtil.close(buyOrderEURUSD);

        verify(orderUtilImplMock).close(buyOrderEURUSD);
    }

    @Test
    public void setLabelDelegatesToOrderUtilImpl() {
        final String newLabel = "newLabel";
        when(orderUtilImplMock.setLabel(buyOrderEURUSD, newLabel))
            .thenReturn(emptyObservable())
            .thenReturn(jfExceptionObservable());

        orderUtil
            .setLabel(buyOrderEURUSD, newLabel)
            .subscribe(orderEventSubscriber);
        orderUtil
            .setLabel(buyOrderEURUSD, newLabel)
            .subscribe(orderEventSubscriber);

        verify(orderUtilImplMock, times(2)).setLabel(buyOrderEURUSD, newLabel);
    }

    @Test
    public void setGTTDelegatesToOrderUtilImpl() {
        final long newGTT = 1L;
        when(orderUtilImplMock.setGoodTillTime(buyOrderEURUSD, newGTT))
            .thenReturn(neverObservable());

        orderUtil.setGoodTillTime(buyOrderEURUSD, newGTT);

        verify(orderUtilImplMock).setGoodTillTime(buyOrderEURUSD, newGTT);
    }

    @Test
    public void setOpenPriceDelegatesToOrderUtilImpl() {
        final double newOpenPrice = 1.1234;
        when(orderUtilImplMock.setOpenPrice(buyOrderEURUSD, newOpenPrice))
            .thenReturn(neverObservable());

        orderUtil.setOpenPrice(buyOrderEURUSD, newOpenPrice);

        verify(orderUtilImplMock).setOpenPrice(buyOrderEURUSD, newOpenPrice);
    }

    @Test
    public void setAmountDelegatesToOrderUtilImpl() {
        final double newAmount = 0.12;
        when(orderUtilImplMock.setRequestedAmount(buyOrderEURUSD, newAmount))
            .thenReturn(neverObservable());

        orderUtil.setRequestedAmount(buyOrderEURUSD, newAmount);

        verify(orderUtilImplMock).setRequestedAmount(buyOrderEURUSD, newAmount);
    }

    @Test
    public void setSLDelegatesToOrderUtilImpl() {
        final double newSL = 1.1234;
        when(orderUtilImplMock.setStopLossPrice(buyOrderEURUSD, newSL))
            .thenReturn(neverObservable());

        orderUtil.setStopLossPrice(buyOrderEURUSD, newSL);

        verify(orderUtilImplMock).setStopLossPrice(buyOrderEURUSD, newSL);
    }

    @Test
    public void setTPDelegatesToOrderUtilImpl() {
        final double newTP = 1.1234;
        when(orderUtilImplMock.setTakeProfitPrice(buyOrderEURUSD, newTP))
            .thenReturn(neverObservable());

        orderUtil.setTakeProfitPrice(buyOrderEURUSD, newTP);

        verify(orderUtilImplMock).setTakeProfitPrice(buyOrderEURUSD, newTP);
    }
}
