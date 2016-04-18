package com.jforex.programming.order;

import static com.jforex.programming.order.event.OrderEventTypeSets.endOfOrderEventTypes;

import java.util.Collection;
import java.util.Map;
import java.util.function.Consumer;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.event.OrderEventConsumer;
import com.jforex.programming.order.event.OrderEventGateway;
import com.jforex.programming.order.event.OrderEventType;

public class OrderUtil {

    private final OrderCreate orderCreate;
    private final OrderChange orderChange;
    private final OrderEventGateway orderEventGateway;

    public OrderUtil(final OrderCreate orderCreate,
                     final OrderChange orderChange,
                     final OrderEventGateway orderEventGateway) {
        this.orderCreate = orderCreate;
        this.orderChange = orderChange;
        this.orderEventGateway = orderEventGateway;
    }

    public OrderCreateResult submit(final OrderParams orderParams) {
        return orderCreate.submit(orderParams);
    }

    public OrderCreateResult merge(final String mergeOrderLabel,
                                   final Collection<IOrder> toMergeOrders) {
        return orderCreate.merge(mergeOrderLabel, toMergeOrders);
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

    public void registerEventConsumer(final IOrder order,
                                      final OrderEventConsumer orderEventConsumer) {
        registerOnObservable(order, orderEventConsumer::onOrderEvent);
    }

    private void registerOnObservable(final IOrder order,
                                      final Consumer<OrderEvent> orderEventConsumer) {
        orderEventGateway.observable()
                         .filter(orderEvent -> orderEvent.order().equals(order))
                         .takeUntil(orderEvent -> endOfOrderEventTypes.contains(orderEvent.type()))
                         .subscribe(orderEventConsumer::accept);
    }

    public void registerEventConsumerMap(final IOrder order,
                                         final Map<OrderEventType, OrderEventConsumer> orderEventConsumerMap) {
        registerOnObservable(order,
                             orderEvent -> {
                                 final OrderEventType orderEventType = orderEvent.type();
                                 if (orderEventConsumerMap.containsKey(orderEventType))
                                     orderEventConsumerMap.get(orderEventType).onOrderEvent(orderEvent);
                             });
    }
}
