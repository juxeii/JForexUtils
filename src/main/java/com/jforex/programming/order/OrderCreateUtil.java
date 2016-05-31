package com.jforex.programming.order;

import java.util.Collection;

import com.jforex.programming.builder.OrderParams;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.event.OrderEventTypeData;

import com.dukascopy.api.IEngine;
import com.dukascopy.api.IOrder;

import rx.Observable;

public class OrderCreateUtil {

    private final IEngine engine;
    private final OrderUtilHandler orderUtilHandler;

    public OrderCreateUtil(final IEngine engine,
                           final OrderUtilHandler orderUtilHandler) {
        this.engine = engine;
        this.orderUtilHandler = orderUtilHandler;
    }

    public Observable<OrderEvent> submitOrder(final OrderParams orderParams) {
        final OrderSupplier submitCall = () -> engine.submitOrder(orderParams.label(),
                                                                      orderParams.instrument(),
                                                                      orderParams.orderCommand(),
                                                                      orderParams.amount(),
                                                                      orderParams.price(),
                                                                      orderParams.slippage(),
                                                                      orderParams.stopLossPrice(),
                                                                      orderParams.takeProfitPrice(),
                                                                      orderParams.goodTillTime(),
                                                                      orderParams.comment());
        return orderUtilHandler.runOrderSupplierCall(submitCall, OrderEventTypeData.submitData);
    }

    public Observable<OrderEvent> mergeOrders(final String mergeOrderLabel,
                                              final Collection<IOrder> toMergeOrders) {
        final OrderSupplier mergeCall = () -> engine.mergeOrders(mergeOrderLabel, toMergeOrders);
        return orderUtilHandler.runOrderSupplierCall(mergeCall, OrderEventTypeData.mergeData);
    }
}
