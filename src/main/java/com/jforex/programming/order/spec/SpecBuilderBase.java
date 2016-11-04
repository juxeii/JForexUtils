package com.jforex.programming.order.spec;

import java.util.HashMap;
import java.util.Map;

import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.event.OrderEventType;

import io.reactivex.Observable;
import io.reactivex.functions.Action;

public class SpecBuilderBase<B> extends BuilderBase<B> {

    protected final Observable<OrderEvent> observable;
    protected ErrorConsumer errorConsumer = t -> {};
    protected Action startAction = () -> {};
    protected Action completeAction = () -> {};
    protected final Map<OrderEventType, OrderEventConsumer> consumerForEvent = new HashMap<>();
    protected int noOfRetries;
    protected long delayInMillis;

    public SpecBuilderBase(final Observable<OrderEvent> observable) {
        this.observable = observable;
    }
}
