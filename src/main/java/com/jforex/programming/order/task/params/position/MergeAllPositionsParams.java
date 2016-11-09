package com.jforex.programming.order.task.params.position;

import static com.google.common.base.Preconditions.checkNotNull;

import com.jforex.programming.order.task.params.basic.BasicParamsBase;
import com.jforex.programming.order.task.params.basic.BasicParamsBuilder;

public class MergeAllPositionsParams extends BasicParamsBase {

    private final MergePositionParams mergePositionParams;

    private MergeAllPositionsParams(final Builder builder) {
        super(builder);

        mergePositionParams = builder.mergePositionParams;
        consumerForEvent = mergePositionParams.consumerForEvent();
    }

    public MergePositionParams mergePositionParams() {
        return mergePositionParams;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static class Builder extends BasicParamsBuilder<Builder> {

        private MergePositionParams mergePositionParams = MergePositionParams.newBuilder().build();

        public Builder withMergePositionParams(final MergePositionParams mergePositionParams) {
            checkNotNull(mergePositionParams);

            this.mergePositionParams = mergePositionParams;
            return this;
        }

        public MergeAllPositionsParams build() {
            return new MergeAllPositionsParams(this);
        }
    }
}
