package com.jforex.programming.order.task.params.position;

import static com.google.common.base.Preconditions.checkNotNull;

import com.jforex.programming.order.task.params.basic.BasicParamsBase;
import com.jforex.programming.order.task.params.basic.BasicParamsBuilder;

public class CloseAllPositionsParams extends BasicParamsBase {

    private final ClosePositionParams closePositionParams;

    private CloseAllPositionsParams(final Builder builder) {
        super(builder);

        closePositionParams = builder.closePositionParams;
        consumerForEvent = closePositionParams.consumerForEvent();
    }

    public ClosePositionParams closePositionParams() {
        return closePositionParams;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static class Builder extends BasicParamsBuilder<Builder> {

        private ClosePositionParams closePositionParams = ClosePositionParams.newBuilder().build();

        public Builder withClosePositionParams(final ClosePositionParams closePositionParams) {
            checkNotNull(closePositionParams);

            this.closePositionParams = closePositionParams;
            return this;
        }

        public CloseAllPositionsParams build() {
            return new CloseAllPositionsParams(this);
        }
    }
}
