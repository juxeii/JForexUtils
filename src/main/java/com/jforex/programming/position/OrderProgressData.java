package com.jforex.programming.position;

import com.dukascopy.api.IOrder;

import rx.Subscriber;

public final class OrderProgressData {

    private final TaskEventData taskEventData;
    private final Subscriber<? super IOrder> subscriber;

    public OrderProgressData(final TaskEventData taskEventData,
                             final Subscriber<? super IOrder> subscriber) {
        this.taskEventData = taskEventData;
        this.subscriber = subscriber;
    }

    public final TaskEventData taskEventData() {
        return taskEventData;
    }

    public final Subscriber<? super IOrder> subscriber() {
        return subscriber;
    }
}
