package com.jforex.programming.order.task.params.basic;

import java.util.function.Consumer;

import com.jforex.programming.order.task.params.CommonParamsBase;

import io.reactivex.functions.Action;

public class BasicParamsBase extends CommonParamsBase {

    private final Consumer<Throwable> errorConsumer;
    private final Action startAction;
    private final Action completeAction;

    public BasicParamsBase(final BasicParamsBuilder<?> builder) {
        super(builder);

        errorConsumer = builder.errorConsumer;
        startAction = builder.startAction;
        completeAction = builder.completeAction;
    }

    public Consumer<Throwable> errorConsumer() {
        return errorConsumer;
    }

    public Action startAction() {
        return startAction;
    }

    public Action completeAction() {
        return completeAction;
    }
}
