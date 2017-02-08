package com.jforex.programming.order.task.params;

import com.jforex.programming.order.task.params.basic.BasicParamsBuilder;

public abstract class BasicTaskParamsBase extends TaskComposeAndEventMapData
                                          implements TaskParamsBase {

    protected BasicTaskParamsBase(final BasicParamsBuilder<?> builder) {
        super(builder);
    }

    @Override
    public abstract TaskParamsType type();
}
