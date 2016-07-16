package com.jforex.programming.order.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.google.common.collect.Sets;
import com.jforex.programming.order.OrderParams;
import com.jforex.programming.order.OrderPositionHandler;
import com.jforex.programming.order.OrderUtil;
import com.jforex.programming.order.OrderUtilHandler;
import com.jforex.programming.order.command.MergeCommand;
import com.jforex.programming.order.command.MergePositionCommand;
import com.jforex.programming.order.command.OrderChangeCommand;
import com.jforex.programming.order.command.SubmitCommand;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.position.PositionOrders;
import com.jforex.programming.position.RestoreSLTPPolicy;
import com.jforex.programming.test.common.InstrumentUtilForTest;
import com.jforex.programming.test.common.OrderParamsForTest;
import com.jforex.programming.test.fakes.IOrderForTest;

import com.dukascopy.api.IOrder;

import rx.Observable;
import rx.observers.TestSubscriber;

public class OrderUtilTest extends InstrumentUtilForTest {

    private OrderUtil orderUtil;

    @Mock
    private OrderUtilHandler orderUtilHandlerMock;
    @Mock
    private OrderPositionHandler orderPositionHandlerMock;
    @Mock
    private RestoreSLTPPolicy restoreSLTPPolicyMock;
    @Mock
    private PositionOrders positionOrdersMock;
    private final TestSubscriber<OrderEvent> orderEventSubscriber = new TestSubscriber<>();
    private final IOrderForTest orderToChange = IOrderForTest.buyOrderEURUSD();
    private final Set<IOrder> toMergeOrders = Sets.newHashSet(IOrderForTest.buyOrderEURUSD(),
                                                              IOrderForTest.sellOrderEURUSD());
    private final String mergeOrderLabel = "MergeLabel";

    @Before
    public void setUp() {
        initCommonTestFramework();

        orderUtil = new OrderUtil(engineMock,
                                  orderPositionHandlerMock,
                                  orderUtilHandlerMock);
    }

    @Test
    public void testPositionOrdersReturnsCorrectInstance() {
        when(orderPositionHandlerMock.positionOrders(instrumentEURUSD))
                .thenReturn(positionOrdersMock);

        final PositionOrders positionOrders = orderUtil.positionOrders(instrumentEURUSD);

        assertThat(positionOrders, equalTo(positionOrdersMock));
    }

    @Test
    public void testSubmitCallsOnPositionUtil() {
        final OrderParams orderParams = OrderParamsForTest.paramsBuyEURUSD();
        when(orderPositionHandlerMock.submitOrder(any(SubmitCommand.class)))
                .thenReturn(Observable.empty());

        orderUtil.submitOrder(orderParams)
                .subscribe(orderEventSubscriber);

        verify(orderPositionHandlerMock)
                .submitOrder(any(SubmitCommand.class));
        orderEventSubscriber.assertCompleted();
    }

    @Test
    public void testMergeCallsOnPositionUtil() {
        when(orderPositionHandlerMock.mergeOrders(any(MergeCommand.class)))
                .thenReturn(Observable.empty());

        orderUtil.mergeOrders(mergeOrderLabel, toMergeOrders)
                .subscribe(orderEventSubscriber);

        verify(orderPositionHandlerMock)
                .mergeOrders(any(MergeCommand.class));
        orderEventSubscriber.assertCompleted();
    }

    @Test
    public void testMergePositionCallsOnPositionUtil() {
        when(orderPositionHandlerMock.positionOrders(instrumentEURUSD))
                .thenReturn(positionOrdersMock);
        when(positionOrdersMock.filled())
                .thenReturn(toMergeOrders);
        when(orderPositionHandlerMock.mergePositionOrders(any(MergePositionCommand.class)))
                .thenReturn(Observable.empty());

        orderUtil.mergePositionOrders(mergeOrderLabel, instrumentEURUSD, restoreSLTPPolicyMock)
                .subscribe(orderEventSubscriber);

        verify(orderPositionHandlerMock)
                .mergePositionOrders(any(MergePositionCommand.class));
        orderEventSubscriber.assertCompleted();
    }

    @Test
    public void testClosePositionCallsOnPositionUtil() {
        when(orderPositionHandlerMock.closePosition(instrumentEURUSD))
                .thenReturn(Observable.empty().toCompletable());

        orderUtil.closePosition(instrumentEURUSD)
                .subscribe(orderEventSubscriber);

        verify(orderPositionHandlerMock)
                .closePosition(instrumentEURUSD);
        orderEventSubscriber.assertCompleted();
    }

    @Test
    public void testCloseCallsOnChangeUtil() {
        when(orderUtilHandlerMock.observable(any(OrderChangeCommand.class)))
                .thenReturn(Observable.empty());

        orderUtil.close(orderToChange)
                .subscribe(orderEventSubscriber);

        verify(orderUtilHandlerMock).observable(any(OrderChangeCommand.class));
        orderEventSubscriber.assertCompleted();
    }

    @Test
    public void testSetLabelCallsOnChangeUtil() {
        final String newLabel = "NewLabel";
        when(orderUtilHandlerMock.observable(any(OrderChangeCommand.class)))
                .thenReturn(Observable.empty());

        orderUtil.setLabel(orderToChange, newLabel)
                .subscribe(orderEventSubscriber);

        verify(orderUtilHandlerMock)
                .observable(any(OrderChangeCommand.class));
        orderEventSubscriber.assertCompleted();
    }

    @Test
    public void testSetGTTCallsOnChangeUtil() {
        final long newGTT = 123456L;
        when(orderUtilHandlerMock.observable(any(OrderChangeCommand.class)))
                .thenReturn(Observable.empty());

        orderUtil.setGoodTillTime(orderToChange, newGTT)
                .subscribe(orderEventSubscriber);

        verify(orderUtilHandlerMock).observable(any(OrderChangeCommand.class));
        orderEventSubscriber.assertCompleted();
    }

    @Test
    public void testSetOpenPriceCallsOnChangeUtil() {
        final double newOpenPrice = 1.12122;
        when(orderUtilHandlerMock.observable(any(OrderChangeCommand.class)))
                .thenReturn(Observable.empty());

        orderUtil.setOpenPrice(orderToChange, newOpenPrice)
                .subscribe(orderEventSubscriber);

        verify(orderUtilHandlerMock)
                .observable(any(OrderChangeCommand.class));
        orderEventSubscriber.assertCompleted();
    }

    @Test
    public void testSetRequestedAmountCallsOnChangeUtil() {
        final double newRequestedAmount = 0.12;
        when(orderUtilHandlerMock.observable(any(OrderChangeCommand.class)))
                .thenReturn(Observable.empty());

        orderUtil.setRequestedAmount(orderToChange, newRequestedAmount)
                .subscribe(orderEventSubscriber);

        verify(orderUtilHandlerMock)
                .observable(any(OrderChangeCommand.class));
        orderEventSubscriber.assertCompleted();
    }

    @Test
    public void testSetStopLossPriceCallsOnChangeUtil() {
        final double newSL = 1.10987;
        when(orderUtilHandlerMock.observable(any(OrderChangeCommand.class)))
                .thenReturn(Observable.empty());

        orderUtil.setStopLossPrice(orderToChange, newSL)
                .subscribe(orderEventSubscriber);

        verify(orderUtilHandlerMock)
                .observable(any(OrderChangeCommand.class));
        orderEventSubscriber.assertCompleted();
    }

    @Test
    public void testSetTakeProfitPriceCallsOnChangeUtil() {
        final double newTP = 1.11001;
        when(orderUtilHandlerMock.observable(any(OrderChangeCommand.class)))
                .thenReturn(Observable.empty());

        orderUtil.setTakeProfitPrice(orderToChange, newTP)
                .subscribe(orderEventSubscriber);

        verify(orderUtilHandlerMock)
                .observable(any(OrderChangeCommand.class));
        orderEventSubscriber.assertCompleted();
    }
}
