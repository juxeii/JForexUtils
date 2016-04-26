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

import com.google.common.collect.Sets;
import com.jforex.programming.order.OrderCreate;
import com.jforex.programming.order.OrderCreateResult;
import com.jforex.programming.order.OrderParams;
import com.jforex.programming.order.call.OrderCallExecutor;
import com.jforex.programming.order.call.OrderCallExecutorResult;
import com.jforex.programming.order.call.OrderCallRequest;
import com.jforex.programming.order.call.OrderSupplierCall;
import com.jforex.programming.order.event.OrderEventGateway;
import com.jforex.programming.test.common.InstrumentUtilForTest;
import com.jforex.programming.test.common.OrderParamsForTest;
import com.jforex.programming.test.fakes.IOrderForTest;

import com.dukascopy.api.IOrder;
import com.dukascopy.api.JFException;

public class OrderCreateTest extends InstrumentUtilForTest {

    private OrderCreate orderCreate;

    @Mock
    private OrderCallExecutor orderCallExecutorMock;
    @Mock
    private OrderEventGateway orderEventGatewayMock;
    @Captor
    private ArgumentCaptor<OrderSupplierCall> orderCallCaptor;
    private final OrderParams orderParams = OrderParamsForTest.paramsBuyEURUSD();
    private final IOrderForTest orderUnderTest = IOrderForTest.buyOrderEURUSD();
    private final IOrderForTest orderToMergeA = IOrderForTest.buyOrderEURUSD();
    private final IOrderForTest orderToMergeB = IOrderForTest.orderAUDUSD();
    private OrderCallExecutorResult orderExecutorResult;
    private final Set<IOrder> ordersToMerge = Sets.newConcurrentHashSet();
    private String mergeLabel;

    @Before
    public void setUp() {
        initCommonTestFramework();
        mergeLabel = uss.ORDER_MERGE_LABEL_PREFIX() + orderParams.label();
        ordersToMerge.add(orderToMergeA);
        ordersToMerge.add(orderToMergeB);
        orderExecutorResult = new OrderCallExecutorResult(Optional.of(orderUnderTest),
                                                          Optional.empty());
        setUpMocks();

        orderCreate = new OrderCreate(engineMock,
                                      orderCallExecutorMock,
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
    public void testSubmitIsCorrect() throws JFException {
        final OrderCreateResult orderCreateResult = orderCreate.submit(orderParams);

        verifyOrderCallAndOrderRegistration(orderCreateResult.orderOpt().get(),
                                            OrderCallRequest.SUBMIT);

        engineForTest.verifySubmit(orderParams, 1);
    }

    @Test
    public void testMergeIsCorrect() throws JFException {
        final OrderCreateResult orderCreateResult = orderCreate.merge(mergeLabel, ordersToMerge);

        verifyOrderCallAndOrderRegistration(orderCreateResult.orderOpt().get(),
                                            OrderCallRequest.MERGE);

        verify(engineMock).mergeOrders(mergeLabel, ordersToMerge);
    }
}
