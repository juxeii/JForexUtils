package com.jforex.programming.position;

import com.dukascopy.api.IOrder;

import rx.Subscriber;

public final class OrderProgressData {

    private final OrderEventData taskEventData;
    private final Subscriber<? super IOrder> subscriber;

    public OrderProgressData(final OrderEventData taskEventData,
                             final Subscriber<? super IOrder> subscriber) {
        this.taskEventData = taskEventData;
        this.subscriber = subscriber;
    }

    public final OrderEventData taskEventData() {
        return taskEventData;
    }

    public final Subscriber<? super IOrder> subscriber() {
        return subscriber;
    }
}
