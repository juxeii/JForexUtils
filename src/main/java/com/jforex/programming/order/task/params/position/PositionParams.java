package com.jforex.programming.order.task.params.position;

import com.jforex.programming.order.task.params.CommonParamsBase;
import com.jforex.programming.order.task.params.basic.BasicParamsBuilder;

public class PositionParams extends CommonParamsBase {

    private PositionParams(final Builder builder) {
        super(builder);
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static class Builder extends BasicParamsBuilder<Builder> {

        public PositionParams build() {
            return new PositionParams(this);
        }
    }
}
