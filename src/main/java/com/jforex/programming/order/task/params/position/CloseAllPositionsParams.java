package com.jforex.programming.order.task.params.position;

import static com.google.common.base.Preconditions.checkNotNull;

import com.jforex.programming.order.task.params.basic.BasicParamsBase;
import com.jforex.programming.order.task.params.basic.BasicParamsBuilder;

public class CloseAllPositionsParams extends BasicParamsBase {

    private final ClosePositionParams complexClosePositionParams;

    private CloseAllPositionsParams(final Builder builder) {
        super(builder);

        complexClosePositionParams = builder.complexClosePositionParams;
        consumerForEvent = complexClosePositionParams.consumerForEvent();
    }

    public ClosePositionParams complexClosePositionParams() {
        return complexClosePositionParams;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static class Builder extends BasicParamsBuilder<Builder> {

        private ClosePositionParams complexClosePositionParams;

        public Builder withClosePositionParams(final ClosePositionParams complexClosePositionParams) {
            checkNotNull(complexClosePositionParams);

            this.complexClosePositionParams = complexClosePositionParams;
            return this;
        }

        public CloseAllPositionsParams build() {
            return new CloseAllPositionsParams(this);
        }
    }
}
