package com.jforex.programming.order.task.params.position;

import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.event.OrderEventType;
import com.jforex.programming.order.task.params.RetryParams;

import io.reactivex.functions.Action;

public abstract class PositionParamsBase<T> implements RetryParams {

    protected Map<OrderEventType, Consumer<OrderEvent>> consumerForEvent;
    private final BiConsumer<Throwable, T> errorConsumer;
    private final Consumer<T> startConsumer;
    private final Consumer<T> completeConsumer;
    private final int noOfRetries;
    private final long delayInMillis;

    protected PositionParamsBase(final PositionParamsBuilder<?, T> builder) {
        consumerForEvent = builder.consumerForEvent;
        errorConsumer = builder.errorConsumer;
        startConsumer = builder.startConsumer;
        completeConsumer = builder.completeConsumer;
        noOfRetries = builder.noOfRetries;
        delayInMillis = builder.delayInMillis;
    }

    public Map<OrderEventType, Consumer<OrderEvent>> consumerForEvent() {
        return consumerForEvent;
    }

    public final Action startAction(final T instrument) {
        return () -> startConsumer.accept(instrument);
    }

    public final Action completeAction(final T instrument) {
        return () -> completeConsumer.accept(instrument);
    }

    public final Consumer<Throwable> errorConsumer(final T instrument) {
        return err -> errorConsumer.accept(err, instrument);
    }

    @Override
    public int noOfRetries() {
        return noOfRetries;
    }

    @Override
    public long delayInMillis() {
        return delayInMillis;
    }
}
