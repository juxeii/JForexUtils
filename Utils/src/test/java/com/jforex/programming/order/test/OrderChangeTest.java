package com.jforex.programming.order.test;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;

import com.dukascopy.api.IOrder;
import com.dukascopy.api.JFException;
import com.jforex.programming.order.OrderChange;
import com.jforex.programming.order.OrderChangeResult;
import com.jforex.programming.order.call.OrderCallExecutor;
import com.jforex.programming.order.call.OrderCallExecutorResult;
import com.jforex.programming.order.call.OrderCallRequest;
import com.jforex.programming.order.call.OrderCreateCall;
import com.jforex.programming.order.event.OrderEventGateway;
import com.jforex.programming.test.common.InstrumentUtilForTest;
import com.jforex.programming.test.fakes.IOrderForTest;

public class OrderChangeTest extends InstrumentUtilForTest {

    private OrderChange orderChange;

    @Mock private OrderCallExecutor orderCallExecutorMock;
    @Mock private OrderEventGateway orderEventGatewayMock;
    @Captor private ArgumentCaptor<OrderCreateCall> orderCallCaptor;
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
        when(orderCallExecutorMock.run(any(OrderCreateCall.class))).thenReturn(orderExecutorResult);
    }

    private void verifyOrderCallAndOrderRegistration(final IOrder order,
                                                     final OrderCallRequest orderCallRequest) throws JFException {
        verify(orderCallExecutorMock).run(orderCallCaptor.capture());
        orderCallCaptor.getValue().run();
        verify(orderEventGatewayMock).registerOrderRequest(order, orderCallRequest);
    }

    @Test
    public void testCloseIsCorrect() throws JFException {
        final OrderChangeResult orderChangeResult = orderChange.close(orderUnderTest);

        verifyOrderCallAndOrderRegistration(orderChangeResult.order(),
                                            orderChangeResult.callRequest());

        verify(orderUnderTest).close();
    }

    @Test
    public void testChangeLabelIsCorrect() throws JFException {
        final OrderChangeResult orderChangeResult = orderChange.setLabel(orderUnderTest, newLabel);

        verifyOrderCallAndOrderRegistration(orderChangeResult.order(),
                                            orderChangeResult.callRequest());

        verify(orderUnderTest).setLabel(newLabel);
    }

    @Test
    public void testChangeGTTIsCorrect() throws JFException {
        final OrderChangeResult orderChangeResult = orderChange.setGTT(orderUnderTest, newGTT);

        verifyOrderCallAndOrderRegistration(orderChangeResult.order(),
                                            orderChangeResult.callRequest());

        verify(orderUnderTest).setGoodTillTime(newGTT);
    }

    @Test
    public void testChangeAmountIsCorrect() throws JFException {
        final OrderChangeResult orderChangeResult = orderChange.setAmount(orderUnderTest, newAmount);

        verifyOrderCallAndOrderRegistration(orderChangeResult.order(),
                                            orderChangeResult.callRequest());

        verify(orderUnderTest).setRequestedAmount(newAmount);
    }

    @Test
    public void testChangeOpenPriceIsCorrect() throws JFException {
        final OrderChangeResult orderChangeResult = orderChange.setOpenPrice(orderUnderTest, newOpenPrice);

        verifyOrderCallAndOrderRegistration(orderChangeResult.order(),
                                            orderChangeResult.callRequest());

        verify(orderUnderTest).setOpenPrice(newOpenPrice);
    }

    @Test
    public void testChangeSLIsCorrect() throws JFException {
        final OrderChangeResult orderChangeResult = orderChange.setSL(orderUnderTest, newSL);

        verifyOrderCallAndOrderRegistration(orderChangeResult.order(),
                                            orderChangeResult.callRequest());

        verify(orderUnderTest).setStopLossPrice(newSL);
    }

    @Test
    public void testChangeSLInPipsIsCorrect() throws JFException {
        final double pips = 20.3;
        final double newSLForPips = askEURUSD - pips * instrumentEURUSD.getPipValue();
        final OrderChangeResult orderChangeResult = orderChange.setSLInPips(orderUnderTest, askEURUSD, pips);

        verifyOrderCallAndOrderRegistration(orderChangeResult.order(),
                                            orderChangeResult.callRequest());

        verify(orderUnderTest).setStopLossPrice(newSLForPips);
    }

    @Test
    public void testChangeTPIsCorrect() throws JFException {
        final OrderChangeResult orderChangeResult = orderChange.setTP(orderUnderTest, newTP);

        verifyOrderCallAndOrderRegistration(orderChangeResult.order(),
                                            orderChangeResult.callRequest());

        verify(orderUnderTest).setTakeProfitPrice(newTP);
    }

    @Test
    public void testChangeTPInPipsIsCorrect() throws JFException {
        final double pips = 20.3;
        final double newTPForPips = askEURUSD + pips * instrumentEURUSD.getPipValue();
        final OrderChangeResult orderChangeResult = orderChange.setTPInPips(orderUnderTest, askEURUSD, pips);

        verifyOrderCallAndOrderRegistration(orderChangeResult.order(),
                                            orderChangeResult.callRequest());

        verify(orderUnderTest).setTakeProfitPrice(newTPForPips);
    }
}
