package com.jforex.programming.order;

import java.util.Collection;

import com.dukascopy.api.IEngine;
import com.dukascopy.api.IOrder;
import com.jforex.programming.order.call.OrderCallExecutor;
import com.jforex.programming.order.call.OrderCallExecutorResult;
import com.jforex.programming.order.call.OrderCallRequest;
import com.jforex.programming.order.call.OrderSupplierCall;
import com.jforex.programming.order.event.OrderEventGateway;

public class OrderCreate {

    private final IEngine engine;
    private final OrderCallExecutor orderCallExecutor;
    private final OrderEventGateway orderEventGateway;

    public OrderCreate(final IEngine engine,
                       final OrderCallExecutor orderCallExecutor,
                       final OrderEventGateway orderEventGateway) {
        this.engine = engine;
        this.orderCallExecutor = orderCallExecutor;
        this.orderEventGateway = orderEventGateway;
    }

    public OrderCreateResult submit(final OrderParams orderParams) {
        final OrderSupplierCall submitCall = () -> engine.submitOrder(orderParams.label(),
                                                                      orderParams.instrument(),
                                                                      orderParams.orderCommand(),
                                                                      orderParams.amount(),
                                                                      orderParams.price(),
                                                                      orderParams.slippage(),
                                                                      orderParams.stopLossPrice(),
                                                                      orderParams.takeProfitPrice(),
                                                                      orderParams.goodTillTime(),
                                                                      orderParams.comment());
        return createResult(submitCall, OrderCallRequest.SUBMIT);
    }

    public OrderCreateResult merge(final String mergeOrderLabel,
                                   final Collection<IOrder> toMergeOrders) {
        final OrderSupplierCall mergeCall = () -> engine.mergeOrders(mergeOrderLabel, toMergeOrders);
        return createResult(mergeCall, OrderCallRequest.MERGE);
    }

    private OrderCreateResult createResult(final OrderSupplierCall orderSupplierCall,
                                           final OrderCallRequest orderCallRequest) {
        final OrderCallExecutorResult orderExecutorResult = orderCallExecutor.run(orderSupplierCall);
        final OrderCreateResult orderCreateResult = new OrderCreateResult(orderExecutorResult.orderOpt(),
                                                                          orderExecutorResult.exceptionOpt(),
                                                                          orderCallRequest);
        registerOrderCallRequest(orderCreateResult);
        return orderCreateResult;
    }

    private void registerOrderCallRequest(final OrderCreateResult orderCreateResult) {
        if (!orderCreateResult.exceptionOpt().isPresent())
            orderEventGateway.registerOrderRequest(orderCreateResult.orderOpt().get(),
                                                   orderCreateResult.callRequest());
    }
}
