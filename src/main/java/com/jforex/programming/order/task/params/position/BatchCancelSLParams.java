package com.jforex.programming.order.task.params.position;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.function.Function;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.task.BatchMode;
import com.jforex.programming.order.task.params.basic.BasicParamsBuilder;
import com.jforex.programming.order.task.params.basic.CancelSLParams;

public class BatchCancelSLParams extends PositionParamsBase {

    private final Function<IOrder, CancelSLParams> cancelSLFactory;
    private final BatchMode batchMode;

    private BatchCancelSLParams(final Builder builder) {
        super(builder);

        cancelSLFactory = builder.cancelSLFactory;
        batchMode = builder.batchMode;
    }

    public final Function<IOrder, CancelSLParams> cancelSLParamsFactory() {
        return cancelSLFactory;
    }

    public final BatchMode batchMode() {
        return batchMode;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static class Builder extends BasicParamsBuilder<Builder> {

        private Function<IOrder, CancelSLParams> cancelSLFactory =
                order -> CancelSLParams.withOrder(order).build();
        private BatchMode batchMode = BatchMode.MERGE;

        public Builder withCancelSLParams(final Function<IOrder, CancelSLParams> cancelSLFactory) {
            checkNotNull(cancelSLFactory);

            this.cancelSLFactory = cancelSLFactory;
            return this;
        }

        public Builder withBatchMode(final BatchMode batchMode) {
            checkNotNull(batchMode);

            this.batchMode = batchMode;
            return this;
        }

        public BatchCancelSLParams build() {
            return new BatchCancelSLParams(this);
        }
    }
}
