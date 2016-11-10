package com.jforex.programming.order.task.params.position;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.task.params.CommonParamsBuilder;

@SuppressWarnings("unchecked")
public class ParamsBuilderForCancel<T> extends CommonParamsBuilder<T> {

    protected BiConsumer<Throwable, IOrder> errorConsumer = (t, o) -> {};
    protected Consumer<IOrder> startConsumer = o -> {};
    protected Consumer<IOrder> completeConsumer = o -> {};

    public T doOnStart(final Consumer<IOrder> startConsumer) {
        checkNotNull(startConsumer);

        this.startConsumer = startConsumer;
        return (T) this;
    }

    public T doOnError(final BiConsumer<Throwable, IOrder> errorConsumer) {
        checkNotNull(errorConsumer);

        this.errorConsumer = errorConsumer;
        return (T) this;
    }

    public T doOnComplete(final Consumer<IOrder> completeConsumer) {
        checkNotNull(completeConsumer);

        this.completeConsumer = completeConsumer;
        return (T) this;
    }
}
