package com.jforex.programming.order.call;

@FunctionalInterface
public interface OrderCallResultConsumer {

    abstract void onOrderCallResult(OrderCallResult orderCallResult);
}
