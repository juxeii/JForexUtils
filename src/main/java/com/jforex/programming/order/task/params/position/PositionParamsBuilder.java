package com.jforex.programming.order.task.params.position;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

import com.jforex.programming.order.task.params.CommonParamsBuilder;

@SuppressWarnings("unchecked")
public class PositionParamsBuilder<T, V> extends CommonParamsBuilder<T> {

    protected BiConsumer<Throwable, V> errorConsumer = (t, v) -> {};
    protected Consumer<V> startConsumer = v -> {};
    protected Consumer<V> completeConsumer = v -> {};

    public T doOnStart(final Consumer<V> startConsumer) {
        checkNotNull(startConsumer);

        this.startConsumer = startConsumer;
        return (T) this;
    }

    public T doOnError(final BiConsumer<Throwable, V> errorConsumer) {
        checkNotNull(errorConsumer);

        this.errorConsumer = errorConsumer;
        return (T) this;
    }

    public T doOnComplete(final Consumer<V> completeConsumer) {
        checkNotNull(completeConsumer);

        this.completeConsumer = completeConsumer;
        return (T) this;
    }
}
