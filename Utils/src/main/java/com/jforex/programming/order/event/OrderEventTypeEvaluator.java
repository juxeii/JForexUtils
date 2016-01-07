package com.jforex.programming.order.event;

import java.util.Optional;

import com.jforex.programming.order.call.OrderCallRequest;

@FunctionalInterface
public interface OrderEventTypeEvaluator {

    abstract OrderEventType get(OrderMessageData orderMessageData,
                                Optional<OrderCallRequest> orderCallRequestOpt);
}
