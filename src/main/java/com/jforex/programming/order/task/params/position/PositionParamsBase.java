package com.jforex.programming.order.task.params.position;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

import com.jforex.programming.order.task.params.CommonParamsBase;

import io.reactivex.functions.Action;

public abstract class PositionParamsBase<T> extends CommonParamsBase {

    private final BiConsumer<Throwable, T> errorConsumer;
    private final Consumer<T> startConsumer;
    private final Consumer<T> completeConsumer;

    protected PositionParamsBase(final PositionParamsBuilder<?, T> builder) {
        super(builder);

        errorConsumer = builder.errorConsumer;
        startConsumer = builder.startConsumer;
        completeConsumer = builder.completeConsumer;
    }

    public final Action startAction(final T param) {
        return () -> startConsumer.accept(param);
    }

    public final Action completeAction(final T param) {
        return () -> completeConsumer.accept(param);
    }

    public final Consumer<Throwable> errorConsumer(final T param) {
        return err -> errorConsumer.accept(err, param);
    }
}
