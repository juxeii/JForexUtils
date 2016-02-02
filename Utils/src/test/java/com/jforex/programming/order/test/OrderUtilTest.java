package com.jforex.programming.order.test;

import static com.jforex.programming.misc.JForexUtil.uss;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;

import com.dukascopy.api.IOrder;
import com.dukascopy.api.JFException;
import com.google.common.collect.Sets;
import com.jforex.programming.order.OrderParams;
import com.jforex.programming.order.OrderUtil;
import com.jforex.programming.order.call.OrderCallExecutor;
import com.jforex.programming.order.call.OrderCallExecutorResult;
import com.jforex.programming.order.call.OrderCallResult;
import com.jforex.programming.order.call.OrderCreateCall;
import com.jforex.programming.order.event.OrderEventGateway;
import com.jforex.programming.test.common.InstrumentUtilForTest;
import com.jforex.programming.test.common.OrderParamsForTest;
import com.jforex.programming.test.fakes.IOrderForTest;

public class OrderUtilTest extends InstrumentUtilForTest {

    private OrderUtil orderUtil;

    @Mock private OrderCallExecutor orderCallExecutorMock;
    @Mock private OrderEventGateway orderEventGatewayMock;
    @Captor private ArgumentCaptor<OrderCreateCall> orderCallCaptor;
    private final OrderParams orderParams = OrderParamsForTest.paramsBuyEURUSD();
    private final IOrderForTest orderUnderTest = IOrderForTest.buyOrderEURUSD();
    private final IOrderForTest orderToMergeA = IOrderForTest.buyOrderEURUSD();
    private final IOrderForTest orderToMergeB = IOrderForTest.orderAUDUSD();
    private OrderCallExecutorResult orderExecutorResult;
    private final Set<IOrder> ordersToMerge = Sets.newConcurrentHashSet();
    private String mergeLabel;
    private final String newLabel = "NewLabel";
    private final long newGTT = 123456L;
    private final double newAmount = 0.12;
    private final double newOpenPrice = 1.12122;
    private final double newSL = 1.10987;
    private final double newTP = 1.11001;

    @Before
    public void setUp() {
        initCommonTestFramework();
        mergeLabel = uss.ORDER_MERGE_LABEL_PREFIX() + orderParams.label();
        ordersToMerge.add(orderToMergeA);
        ordersToMerge.add(orderToMergeB);
        orderExecutorResult = new OrderCallExecutorResult(Optional.of(orderUnderTest),
                                                          Optional.empty());
        setUpMocks();

        orderUtil = new OrderUtil(engineMock,
                                  orderCallExecutorMock,
                                  orderEventGatewayMock);
    }

    private void setUpMocks() {
        when(orderCallExecutorMock.run(any(OrderCreateCall.class))).thenReturn(orderExecutorResult);
    }

    private void verifyOrderCallAndCallResultRegistration(final OrderCallResult actualCallResult) throws JFException {
        verify(orderCallExecutorMock).run(orderCallCaptor.capture());
        orderCallCaptor.getValue().run();
    }

    @Test
    public void testSubmitIsCorrect() throws JFException {
        final OrderCallResult actualCallResult = orderUtil.submit(orderParams);

        verifyOrderCallAndCallResultRegistration(actualCallResult);

        engineForTest.verifySubmit(orderParams, 1);
    }

    @Test
    public void testMergeIsCorrect() throws JFException {
        final OrderCallResult actualCallResult = orderUtil.merge(mergeLabel, ordersToMerge);

        verifyOrderCallAndCallResultRegistration(actualCallResult);

        verify(engineMock).mergeOrders(mergeLabel, ordersToMerge);
    }

    @Test
    public void testCloseIsCorrect() throws JFException {
        final OrderCallResult actualCallResult = orderUtil.close(orderUnderTest);

        verifyOrderCallAndCallResultRegistration(actualCallResult);

        verify(orderUnderTest).close();
    }

    @Test
    public void testChangeLabelIsCorrect() throws JFException {
        final OrderCallResult actualCallResult = orderUtil.changeLabel(orderUnderTest, newLabel);

        verifyOrderCallAndCallResultRegistration(actualCallResult);

        verify(orderUnderTest).setLabel(newLabel);
    }

    @Test
    public void testChangeGTTIsCorrect() throws JFException {
        final OrderCallResult actualCallResult = orderUtil.changeGTT(orderUnderTest, newGTT);

        verifyOrderCallAndCallResultRegistration(actualCallResult);

        verify(orderUnderTest).setGoodTillTime(newGTT);
    }

    @Test
    public void testChangeAmountIsCorrect() throws JFException {
        final OrderCallResult actualCallResult = orderUtil.changeAmount(orderUnderTest, newAmount);

        verifyOrderCallAndCallResultRegistration(actualCallResult);

        verify(orderUnderTest).setRequestedAmount(newAmount);
    }

    @Test
    public void testChangeOpenPriceIsCorrect() throws JFException {
        final OrderCallResult actualCallResult = orderUtil.changeOpenPrice(orderUnderTest, newOpenPrice);

        verifyOrderCallAndCallResultRegistration(actualCallResult);

        verify(orderUnderTest).setOpenPrice(newOpenPrice);
    }

    @Test
    public void testChangeSLIsCorrect() throws JFException {
        final OrderCallResult actualCallResult = orderUtil.changeSL(orderUnderTest, newSL);

        verifyOrderCallAndCallResultRegistration(actualCallResult);

        verify(orderUnderTest).setStopLossPrice(newSL);
    }

    @Test
    public void testChangeSLInPipsIsCorrect() throws JFException {
        final double pips = 20.3;
        final double newSLForPips = askEURUSD - pips * instrumentEURUSD.getPipValue();
        final OrderCallResult actualCallResult = orderUtil.changeSLInPips(orderUnderTest, askEURUSD, pips);

        verifyOrderCallAndCallResultRegistration(actualCallResult);

        verify(orderUnderTest).setStopLossPrice(newSLForPips);
    }

    @Test
    public void testChangeTPIsCorrect() throws JFException {
        final OrderCallResult actualCallResult = orderUtil.changeTP(orderUnderTest, newTP);

        verifyOrderCallAndCallResultRegistration(actualCallResult);

        verify(orderUnderTest).setTakeProfitPrice(newTP);
    }

    @Test
    public void testChangeTPInPipsIsCorrect() throws JFException {
        final double pips = 20.3;
        final double newTPForPips = askEURUSD + pips * instrumentEURUSD.getPipValue();
        final OrderCallResult actualCallResult = orderUtil.changeTPInPips(orderUnderTest, askEURUSD, pips);

        verifyOrderCallAndCallResultRegistration(actualCallResult);

        verify(orderUnderTest).setTakeProfitPrice(newTPForPips);
    }
}
