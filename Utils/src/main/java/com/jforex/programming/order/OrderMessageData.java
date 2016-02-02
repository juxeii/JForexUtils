package com.jforex.programming.order;

import java.util.Set;

import com.dukascopy.api.IMessage;
import com.dukascopy.api.IOrder;
import com.google.common.collect.ImmutableSet;

public final class OrderMessageData {

    private final IOrder order;
    private final IOrder.State orderState;
    private final IMessage.Type messageType;
    private final Set<IMessage.Reason> messageReasons;

    public OrderMessageData(final IMessage message) {
        this.order = message.getOrder();
        this.orderState = order.getState();
        this.messageType = message.getType();
        this.messageReasons = ImmutableSet.copyOf(message.getReasons());
    }

    public final IOrder order() {
        return order;
    }

    public final IOrder.State orderState() {
        return orderState;
    }

    public final IMessage.Type messageType() {
        return messageType;
    }

    public final Set<IMessage.Reason> messageReasons() {
        return messageReasons;
    }
}
