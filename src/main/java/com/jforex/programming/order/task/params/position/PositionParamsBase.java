package com.jforex.programming.order.task.params.position;

import java.util.function.Consumer;

import com.dukascopy.api.Instrument;
import com.jforex.programming.order.task.params.CommonParamsBase;
import com.jforex.programming.order.task.params.basic.BasicParamsBuilder;

import io.reactivex.functions.Action;

public abstract class PositionParamsBase extends CommonParamsBase {

    protected Instrument instrument;
    private final Consumer<Throwable> errorConsumer;
    private final Action startAction;
    private final Action completeAction;

    protected PositionParamsBase(final BasicParamsBuilder<?> builder) {
        super(builder);

        errorConsumer = builder.errorConsumer;
        startAction = builder.startAction;
        completeAction = builder.completeAction;
    }

    public final Instrument instrument() {
        return instrument;
    }

    public final Action startAction() {
        return startAction;
    }

    public final Action completeAction() {
        return completeAction;
    }

    public final Consumer<Throwable> errorConsumer() {
        return errorConsumer;
    }
}
