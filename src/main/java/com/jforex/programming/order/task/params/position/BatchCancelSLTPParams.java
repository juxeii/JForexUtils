package com.jforex.programming.order.task.params.position;

import static com.google.common.base.Preconditions.checkNotNull;

import com.jforex.programming.order.task.MergeExecutionMode;
import com.jforex.programming.order.task.params.basic.BasicParamsBuilder;

public class BatchCancelSLTPParams extends PositionParamsBase {

    private final BatchCancelSLParams batchCancelSLParams;
    private final BatchCancelTPParams batchCancelTPParams;
    private final MergeExecutionMode mergeExecutionMode;

    private BatchCancelSLTPParams(final Builder builder) {
        super(builder);

        batchCancelSLParams = builder.batchCancelSLParams;
        batchCancelTPParams = builder.batchCancelTPParams;
        mergeExecutionMode = builder.mergeExecutionMode;
        consumerForEvent = batchCancelSLParams.consumerForEvent();
        consumerForEvent.putAll(batchCancelTPParams.consumerForEvent());
    }

    public final BatchCancelSLParams batchCancelSLParams() {
        return batchCancelSLParams;
    }

    public final BatchCancelTPParams batchCancelTPParams() {
        return batchCancelTPParams;
    }

    public final MergeExecutionMode mergeExecutionMode() {
        return mergeExecutionMode;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static class Builder extends BasicParamsBuilder<Builder> {

        private BatchCancelSLParams batchCancelSLParams = BatchCancelSLParams.newBuilder().build();
        private BatchCancelTPParams batchCancelTPParams = BatchCancelTPParams.newBuilder().build();
        private MergeExecutionMode mergeExecutionMode = MergeExecutionMode.MergeCancelSLAndTP;

        public Builder withBatchCancelSLParams(final BatchCancelSLParams batchCancelSLParams) {
            checkNotNull(batchCancelSLParams);

            this.batchCancelSLParams = batchCancelSLParams;
            return this;
        }

        public Builder withBatchCancelTPParams(final BatchCancelTPParams batchCancelTPParams) {
            checkNotNull(batchCancelTPParams);

            this.batchCancelTPParams = batchCancelTPParams;
            return this;
        }

        public Builder withMergeExecutionMode(final MergeExecutionMode mergeExecutionMode) {
            checkNotNull(mergeExecutionMode);

            this.mergeExecutionMode = mergeExecutionMode;
            return this;
        }

        public BatchCancelSLTPParams build() {
            return new BatchCancelSLTPParams(this);
        }
    }
}
