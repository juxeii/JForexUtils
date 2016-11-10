package com.jforex.programming.order.task.params.position;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

import com.dukascopy.api.IOrder;
import com.dukascopy.api.Instrument;
import com.jforex.programming.order.task.params.CommonParamsBase;

import io.reactivex.functions.Action;

public abstract class ParamsBaseForCancel extends CommonParamsBase {

    private Instrument instrument;
    private BiConsumer<Throwable, IOrder> errorConsumer = (t, o) -> {};
    private Consumer<IOrder> startConsumer = o -> {};
    private Consumer<IOrder> completeConsumer = o -> {};

    protected ParamsBaseForCancel(final ParamsBuilderForCancel<?> builder) {
        super(builder);

        errorConsumer = builder.errorConsumer;
        startConsumer = builder.startConsumer;
        completeConsumer = builder.completeConsumer;
    }

    public final Instrument instrument() {
        return instrument;
    }

    public final Action startAction(final IOrder order) {
        return () -> startConsumer.accept(order);
    }

    public final Action completeAction(final IOrder order) {
        return () -> completeConsumer.accept(order);
    }

    public final Consumer<Throwable> errorConsumer(final IOrder order) {
        return err -> errorConsumer.accept(err, order);
    }
}
