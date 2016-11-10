package com.jforex.programming.order.task.params.position;

import com.dukascopy.api.Instrument;
import com.jforex.programming.order.task.params.basic.BasicParamsBase;
import com.jforex.programming.order.task.params.basic.BasicParamsBuilder;

public abstract class PositionParamsBase extends BasicParamsBase {

    protected Instrument instrument;

    protected PositionParamsBase(final BasicParamsBuilder<?> builder) {
        super(builder);
    }

    public final Instrument instrument() {
        return instrument;
    }
}
