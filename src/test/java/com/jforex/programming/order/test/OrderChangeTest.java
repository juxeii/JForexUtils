package com.jforex.programming.order.test;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;

import com.jforex.programming.order.OrderChange;
import com.jforex.programming.order.call.OrderCallExecutor;
import com.jforex.programming.order.call.OrderCallExecutorResult;
import com.jforex.programming.order.call.OrderCallRequest;
import com.jforex.programming.order.call.OrderSupplierCall;
import com.jforex.programming.order.event.OrderEventGateway;
import com.jforex.programming.test.common.InstrumentUtilForTest;
import com.jforex.programming.test.fakes.IOrderForTest;

import com.dukascopy.api.IOrder;
import com.dukascopy.api.JFException;

public class OrderChangeTest extends InstrumentUtilForTest {

    private OrderChange orderChange;

    @Mock
    private OrderCallExecutor orderCallExecutorMock;
    @Mock
    private OrderEventGateway orderEventGatewayMock;
    @Captor
    private ArgumentCaptor<OrderSupplierCall> orderCallCaptor;
    private final IOrderForTest orderUnderTest = IOrderForTest.buyOrderEURUSD();
    private OrderCallExecutorResult orderExecutorResult;
    private final String newLabel = "NewLabel";
    private final long newGTT = 123456L;
    private final double newAmount = 0.12;
    private final double newOpenPrice = 1.12122;
    private final double newSL = 1.10987;
    private final double newTP = 1.11001;

    @Before
    public void setUp() {
        initCommonTestFramework();
        orderExecutorResult = new OrderCallExecutorResult(Optional.of(orderUnderTest),
                                                          Optional.empty());
        setUpMocks();

        orderChange = new OrderChange(orderCallExecutorMock,
                                      orderEventGatewayMock);
    }

    private void setUpMocks() {
        when(orderCallExecutorMock.run(any(OrderSupplierCall.class))).thenReturn(orderExecutorResult);
    }

    private void verifyOrderCallAndOrderRegistration(final IOrder order,
                                                     final OrderCallRequest orderCallRequest) throws JFException {
        verify(orderCallExecutorMock).run(orderCallCaptor.capture());
        orderCallCaptor.getValue().get();
        verify(orderEventGatewayMock).registerOrderRequest(order, orderCallRequest);
    }

    @Test
    public void testCloseIsCorrect() throws JFException {
        orderChange.close(orderUnderTest);

        verifyOrderCallAndOrderRegistration(orderUnderTest,
                                            OrderCallRequest.CLOSE);

        verify(orderUnderTest).close();
    }

    @Test
    public void testCloseIsNotCalledWhenAlreadyClosed() throws JFException {
        orderUnderTest.setState(IOrder.State.CLOSED);

        orderChange.close(orderUnderTest);

        verify(orderUnderTest, never()).close();
        verifyZeroInteractions(orderCallExecutorMock);
    }

    @Test
    public void testChangeLabelIsCorrect() throws JFException {
        orderChange.setLabel(orderUnderTest, newLabel);

        verifyOrderCallAndOrderRegistration(orderUnderTest,
                                            OrderCallRequest.CHANGE_LABEL);

        verify(orderUnderTest).setLabel(newLabel);
    }

    @Test
    public void testSetLabelIsNotCalledWhenAlreadySet() throws JFException {
        final String orderLabel = orderUnderTest.getLabel();

        orderChange.setLabel(orderUnderTest, orderLabel);

        verify(orderUnderTest, never()).setLabel(orderLabel);
        verifyZeroInteractions(orderCallExecutorMock);
    }

    @Test
    public void testChangeGTTIsCorrect() throws JFException {
        orderChange.setGTT(orderUnderTest, newGTT);

        verifyOrderCallAndOrderRegistration(orderUnderTest,
                                            OrderCallRequest.CHANGE_GTT);

        verify(orderUnderTest).setGoodTillTime(newGTT);
    }

    @Test
    public void testSetGTTIsNotCalledWhenAlreadySet() throws JFException {
        final long orderGTT = orderUnderTest.getGoodTillTime();

        orderChange.setGTT(orderUnderTest, orderGTT);

        verify(orderUnderTest, never()).setGoodTillTime(orderGTT);
        verifyZeroInteractions(orderCallExecutorMock);
    }

    @Test
    public void testChangeAmountIsCorrect() throws JFException {
        orderChange.setAmount(orderUnderTest, newAmount);

        verifyOrderCallAndOrderRegistration(orderUnderTest,
                                            OrderCallRequest.CHANGE_AMOUNT);

        verify(orderUnderTest).setRequestedAmount(newAmount);
    }

    @Test
    public void testSetAmountNotCalledWhenAlreadySet() throws JFException {
        final double orderAmount = orderUnderTest.getRequestedAmount();

        orderChange.setAmount(orderUnderTest, orderAmount);

        verify(orderUnderTest, never()).setRequestedAmount(orderAmount);
        verifyZeroInteractions(orderCallExecutorMock);
    }

    @Test
    public void testChangeOpenPriceIsCorrect() throws JFException {
        orderChange.setOpenPrice(orderUnderTest, newOpenPrice);

        verifyOrderCallAndOrderRegistration(orderUnderTest,
                                            OrderCallRequest.CHANGE_OPENPRICE);

        verify(orderUnderTest).setOpenPrice(newOpenPrice);
    }

    @Test
    public void testSetOpenPriceNotCalledWhenAlreadySet() throws JFException {
        final double orderOpenPrice = orderUnderTest.getOpenPrice();

        orderChange.setOpenPrice(orderUnderTest, orderOpenPrice);

        verify(orderUnderTest, never()).setOpenPrice(orderOpenPrice);
        verifyZeroInteractions(orderCallExecutorMock);
    }

    @Test
    public void testChangeSLIsCorrect() throws JFException {
        orderChange.setSL(orderUnderTest, newSL);

        verifyOrderCallAndOrderRegistration(orderUnderTest,
                                            OrderCallRequest.CHANGE_SL);

        verify(orderUnderTest).setStopLossPrice(newSL);
    }

    @Test
    public void testSetSLNotCalledWhenAlreadySet() throws JFException {
        final double orderSL = orderUnderTest.getStopLossPrice();

        orderChange.setSL(orderUnderTest, orderSL);

        verify(orderUnderTest, never()).setStopLossPrice(orderSL);
        verifyZeroInteractions(orderCallExecutorMock);
    }

    @Test
    public void testChangeSLInPipsIsCorrect() throws JFException {
        final double pips = 20.3;
        final double newSLForPips = askEURUSD - pips * instrumentEURUSD.getPipValue();
        orderChange.setSLInPips(orderUnderTest, askEURUSD, pips);

        verifyOrderCallAndOrderRegistration(orderUnderTest,
                                            OrderCallRequest.CHANGE_SL);

        verify(orderUnderTest).setStopLossPrice(newSLForPips);
    }

    @Test
    public void testChangeTPIsCorrect() throws JFException {
        orderChange.setTP(orderUnderTest, newTP);

        verifyOrderCallAndOrderRegistration(orderUnderTest,
                                            OrderCallRequest.CHANGE_TP);

        verify(orderUnderTest).setTakeProfitPrice(newTP);
    }

    @Test
    public void testSetTPNotCalledWhenAlreadySet() throws JFException {
        final double orderTP = orderUnderTest.getTakeProfitPrice();

        orderChange.setTP(orderUnderTest, orderTP);

        verify(orderUnderTest, never()).setTakeProfitPrice(orderTP);
        verifyZeroInteractions(orderCallExecutorMock);
    }

    @Test
    public void testChangeTPInPipsIsCorrect() throws JFException {
        final double pips = 20.3;
        final double newTPForPips = askEURUSD + pips * instrumentEURUSD.getPipValue();
        orderChange.setTPInPips(orderUnderTest, askEURUSD, pips);

        verifyOrderCallAndOrderRegistration(orderUnderTest,
                                            OrderCallRequest.CHANGE_TP);

        verify(orderUnderTest).setTakeProfitPrice(newTPForPips);
    }
}
