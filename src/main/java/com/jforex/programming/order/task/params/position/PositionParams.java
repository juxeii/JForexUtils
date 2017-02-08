package com.jforex.programming.order.task.params.position;

import com.jforex.programming.order.task.params.ComposeData;
import com.jforex.programming.order.task.params.basic.BasicParamsBuilder;

public class PositionParams {

    protected ComposeData composeData;

    private PositionParams(final Builder builder) {
        composeData = builder.composeParams;
    }

    public ComposeData composeData() {
        return composeData;
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
