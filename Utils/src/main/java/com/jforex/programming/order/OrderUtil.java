package com.jforex.programming.order;

import java.util.Collection;
import java.util.Map;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.event.OrderEventConsumer;
import com.jforex.programming.order.event.OrderEventType;

public class OrderUtil {

    private final OrderCreate orderCreate;
    private final OrderChange orderChange;

    public OrderUtil(final OrderCreate orderCreate,
                     final OrderChange orderChange) {
        this.orderCreate = orderCreate;
        this.orderChange = orderChange;
    }

    public OrderCreateResult submit(final OrderParams orderParams) {
        return orderCreate.submit(orderParams);
    }

    public OrderCreateResult submit(final OrderParams orderParams,
                                    final OrderEventConsumer orderEventConsumer) {
        return orderCreate.submit(orderParams, orderEventConsumer);
    }

    public OrderCreateResult submit(final OrderParams orderParams,
                                    final Map<OrderEventType, OrderEventConsumer> orderEventConsumerMap) {
        return orderCreate.submit(orderParams, orderEventConsumerMap);
    }

    public OrderCreateResult merge(final String mergeOrderLabel,
                                   final Collection<IOrder> toMergeOrders) {
        return orderCreate.merge(mergeOrderLabel, toMergeOrders);
    }

    public OrderCreateResult merge(final String mergeOrderLabel,
                                   final Collection<IOrder> toMergeOrders,
                                   final OrderEventConsumer orderEventConsumer) {
        return orderCreate.merge(mergeOrderLabel, toMergeOrders, orderEventConsumer);
    }

    public OrderCreateResult merge(final String mergeOrderLabel,
                                   final Collection<IOrder> toMergeOrders,
                                   final Map<OrderEventType, OrderEventConsumer> orderEventConsumerMap) {
        return orderCreate.merge(mergeOrderLabel, toMergeOrders, orderEventConsumerMap);
    }

    public OrderChangeResult close(final IOrder orderToClose) {
        return orderChange.close(orderToClose);
    }

    public OrderChangeResult setLabel(final IOrder orderToChangeLabel,
                                      final String newLabel) {
        return orderChange.setLabel(orderToChangeLabel, newLabel);
    }

    public OrderChangeResult setGTT(final IOrder orderToChangeGTT,
                                    final long newGTT) {
        return orderChange.setGTT(orderToChangeGTT, newGTT);
    }

    public OrderChangeResult setOpenPrice(final IOrder orderToChangeOpenPrice,
                                          final double newOpenPrice) {
        return orderChange.setOpenPrice(orderToChangeOpenPrice, newOpenPrice);
    }

    public OrderChangeResult setAmount(final IOrder orderToChangeAmount,
                                       final double newAmount) {
        return orderChange.setAmount(orderToChangeAmount, newAmount);
    }

    public OrderChangeResult setSL(final IOrder orderToChangeSL,
                                   final double newSL) {
        return orderChange.setSL(orderToChangeSL, newSL);
    }

    public OrderChangeResult setTP(final IOrder orderToChangeTP,
                                   final double newTP) {
        return orderChange.setTP(orderToChangeTP, newTP);
    }

    public OrderChangeResult setSLInPips(final IOrder orderToChangeSL,
                                         final double referencePrice,
                                         final double pips) {
        return orderChange.setSLInPips(orderToChangeSL, referencePrice, pips);
    }

    public OrderChangeResult setTPInPips(final IOrder orderToChangeTP,
                                         final double referencePrice,
                                         final double pips) {
        return orderChange.setTPInPips(orderToChangeTP, referencePrice, pips);
    }
}
