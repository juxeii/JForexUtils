package com.jforex.programming.order.task.params.position;

import static com.google.common.base.Preconditions.checkNotNull;

import com.jforex.programming.order.task.params.basic.BasicParamsBase;
import com.jforex.programming.order.task.params.basic.BasicParamsBuilder;

public class MergeAllPositionsParams extends BasicParamsBase {

    private final MergePositionParams complexMergePositionParams;

    private MergeAllPositionsParams(final Builder builder) {
        super(builder);

        complexMergePositionParams = builder.complexMergePositionParams;
        consumerForEvent = complexMergePositionParams.consumerForEvent();
    }

    public MergePositionParams complexMergePositionParams() {
        return complexMergePositionParams;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static class Builder extends BasicParamsBuilder<Builder> {

        private MergePositionParams complexMergePositionParams;

        public Builder withClosePositionParams(final MergePositionParams complexMergePositionParams) {
            checkNotNull(complexMergePositionParams);

            this.complexMergePositionParams = complexMergePositionParams;
            return this;
        }

        public MergeAllPositionsParams build() {
            return new MergeAllPositionsParams(this);
        }
    }
}
