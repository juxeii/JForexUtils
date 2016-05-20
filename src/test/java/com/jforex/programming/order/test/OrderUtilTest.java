package com.jforex.programming.order.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;

import com.dukascopy.api.IOrder;
import com.google.common.collect.Sets;
import com.jforex.programming.order.OrderChangeUtil;
import com.jforex.programming.order.OrderParams;
import com.jforex.programming.order.OrderUtil;
import com.jforex.programming.order.call.OrderSupplierCall;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.position.Position;
import com.jforex.programming.position.PositionChangeTask;
import com.jforex.programming.position.PositionCreateTask;
import com.jforex.programming.position.PositionFactory;
import com.jforex.programming.position.RestoreSLTPPolicy;
import com.jforex.programming.test.common.InstrumentUtilForTest;
import com.jforex.programming.test.common.OrderParamsForTest;
import com.jforex.programming.test.fakes.IOrderForTest;

import rx.Observable;
import rx.observers.TestSubscriber;

public class OrderUtilTest extends InstrumentUtilForTest {

    private OrderUtil orderUtil;

    @Mock
    private PositionCreateTask positionCreateTaskMock;
    @Mock
    private PositionChangeTask positionChangeTaskMock;
    @Mock
    private OrderChangeUtil orderChangeUtilMock;
    @Mock
    private PositionFactory positionFactoryMock;
    @Mock
    private RestoreSLTPPolicy restoreSLTPPolicyMock;
    @Mock
    private Position positionMock;
    @Captor
    private ArgumentCaptor<OrderSupplierCall> orderCallCaptor;
    @Captor
    private ArgumentCaptor<Set<IOrder>> toMergeOrdersCaptor;
    private final TestSubscriber<OrderEvent> orderEventSubscriber = new TestSubscriber<>();
    private final IOrderForTest orderToChange = IOrderForTest.buyOrderEURUSD();
    private final Set<IOrder> toMergeOrders = Sets.newHashSet();
    private final String mergeOrderLabel = "MergeLabel";

    @Before
    public void setUp() {
        initCommonTestFramework();

        orderUtil = new OrderUtil(positionCreateTaskMock,
                                  positionChangeTaskMock,
                                  orderChangeUtilMock,
                                  positionFactoryMock);
    }

    @Test
    public void testPositionQueryReturnsCorrectPosition() {
        when(positionFactoryMock.forInstrument(instrumentEURUSD))
                .thenReturn(positionMock);

        final Position position = orderUtil.position(instrumentEURUSD);

        assertThat(position, equalTo(positionMock));
    }

    @Test
    public void testSubmitCallsOnPositionUtil() {
        final OrderParams orderParams = OrderParamsForTest.paramsBuyEURUSD();
        when(positionCreateTaskMock.submitOrder(orderParams))
                .thenReturn(Observable.empty());

        orderUtil.submitOrder(orderParams)
                .subscribe(orderEventSubscriber);

        verify(positionCreateTaskMock)
                .submitOrder(orderParams);
        orderEventSubscriber.assertCompleted();
    }

    @Test
    public void testMergeCallsOnPositionUtil() {
        when(positionCreateTaskMock.mergeOrders(mergeOrderLabel, toMergeOrders))
                .thenReturn(Observable.empty());

        orderUtil.mergeOrders(mergeOrderLabel, toMergeOrders)
                .subscribe(orderEventSubscriber);

        verify(positionCreateTaskMock)
                .mergeOrders(mergeOrderLabel, toMergeOrders);
        orderEventSubscriber.assertCompleted();
    }

    @Test
    public void testMergePositionCallsOnPositionUtil() {
        when(positionCreateTaskMock.mergePositionOrders(mergeOrderLabel, instrumentEURUSD, restoreSLTPPolicyMock))
                .thenReturn(Observable.empty());

        orderUtil.mergePositionOrders(mergeOrderLabel, instrumentEURUSD, restoreSLTPPolicyMock)
                .subscribe(orderEventSubscriber);

        verify(positionCreateTaskMock)
                .mergePositionOrders(mergeOrderLabel, instrumentEURUSD, restoreSLTPPolicyMock);
        orderEventSubscriber.assertCompleted();
    }

    @Test
    public void testClosePositionCallsOnPositionUtil() {
        when(positionChangeTaskMock.closePosition(instrumentEURUSD))
                .thenReturn(Observable.empty().toCompletable());

        orderUtil.closePosition(instrumentEURUSD)
                .subscribe(orderEventSubscriber);

        verify(positionChangeTaskMock)
                .closePosition(instrumentEURUSD);
        orderEventSubscriber.assertCompleted();
    }

    @Test
    public void testCloseCallsOnChangeUtil() {
        when(orderChangeUtilMock.close(orderToChange))
                .thenReturn(Observable.empty());

        orderUtil.close(orderToChange)
                .subscribe(orderEventSubscriber);

        verify(orderChangeUtilMock).close(orderToChange);
        orderEventSubscriber.assertCompleted();
    }

    @Test
    public void testSetLabelCallsOnChangeUtil() {
        final String newLabel = "NewLabel";
        when(orderChangeUtilMock.setLabel(orderToChange, newLabel))
                .thenReturn(Observable.empty());

        orderUtil.setLabel(orderToChange, newLabel)
                .subscribe(orderEventSubscriber);

        verify(orderChangeUtilMock)
                .setLabel(orderToChange, newLabel);
        orderEventSubscriber.assertCompleted();
    }

    @Test
    public void testSetGTTCallsOnChangeUtil() {
        final long newGTT = 123456L;
        when(orderChangeUtilMock.setGoodTillTime(orderToChange, newGTT))
                .thenReturn(Observable.empty());

        orderUtil.setGoodTillTime(orderToChange, newGTT)
                .subscribe(orderEventSubscriber);

        verify(orderChangeUtilMock).setGoodTillTime(orderToChange, newGTT);
        orderEventSubscriber.assertCompleted();
    }

    @Test
    public void testSetOpenPriceCallsOnChangeUtil() {
        final double newOpenPrice = 1.12122;
        when(orderChangeUtilMock.setOpenPrice(orderToChange, newOpenPrice))
                .thenReturn(Observable.empty());

        orderUtil.setOpenPrice(orderToChange, newOpenPrice)
                .subscribe(orderEventSubscriber);

        verify(orderChangeUtilMock)
                .setOpenPrice(orderToChange, newOpenPrice);
        orderEventSubscriber.assertCompleted();
    }

    @Test
    public void testSetRequestedAmountCallsOnChangeUtil() {
        final double newRequestedAmount = 0.12;
        when(orderChangeUtilMock.setRequestedAmount(orderToChange, newRequestedAmount))
                .thenReturn(Observable.empty());

        orderUtil.setRequestedAmount(orderToChange, newRequestedAmount)
                .subscribe(orderEventSubscriber);

        verify(orderChangeUtilMock)
                .setRequestedAmount(orderToChange, newRequestedAmount);
        orderEventSubscriber.assertCompleted();
    }

    @Test
    public void testSetStopLossPriceCallsOnChangeUtil() {
        final double newSL = 1.10987;
        when(orderChangeUtilMock.setStopLossPrice(orderToChange, newSL))
                .thenReturn(Observable.empty());

        orderUtil.setStopLossPrice(orderToChange, newSL)
                .subscribe(orderEventSubscriber);

        verify(orderChangeUtilMock)
                .setStopLossPrice(orderToChange, newSL);
        orderEventSubscriber.assertCompleted();
    }

    @Test
    public void testSetTakeProfitPriceCallsOnChangeUtil() {
        final double newTP = 1.11001;
        when(orderChangeUtilMock.setTakeProfitPrice(orderToChange, newTP))
                .thenReturn(Observable.empty());

        orderUtil.setTakeProfitPrice(orderToChange, newTP)
                .subscribe(orderEventSubscriber);

        verify(orderChangeUtilMock)
                .setTakeProfitPrice(orderToChange, newTP);
        orderEventSubscriber.assertCompleted();
    }
}
