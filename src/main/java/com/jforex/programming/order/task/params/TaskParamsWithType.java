package com.jforex.programming.order.task.params;

public abstract class TaskParamsWithType extends TaskParamsBase implements TaskParams {

    protected TaskParamsWithType(final Builder<?> builder) {
        super(builder);
    }

    @Override
    public abstract TaskParamsType type();
}
