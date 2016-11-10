package com.jforex.programming.order.task.params.position;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.function.Function;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.task.BatchMode;
import com.jforex.programming.order.task.params.basic.BasicParamsBuilder;
import com.jforex.programming.order.task.params.basic.CancelTPParams;

public class BatchCancelTPParams extends PositionParamsBase {

    private final Function<IOrder, CancelTPParams> cancelTPFactory;
    private final BatchMode batchMode;

    private BatchCancelTPParams(final Builder builder) {
        super(builder);

        cancelTPFactory = builder.cancelTPFactory;
        batchMode = builder.batchMode;
    }

    public final Function<IOrder, CancelTPParams> cancelTPParamsFactory() {
        return cancelTPFactory;
    }

    public final BatchMode batchMode() {
        return batchMode;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static class Builder extends BasicParamsBuilder<Builder> {

        private Function<IOrder, CancelTPParams> cancelTPFactory =
                order -> CancelTPParams.withOrder(order).build();
        private BatchMode batchMode = BatchMode.MERGE;

        public Builder withCancelTPParams(final Function<IOrder, CancelTPParams> cancelTPFactory) {
            checkNotNull(cancelTPFactory);

            this.cancelTPFactory = cancelTPFactory;
            return this;
        }

        public Builder withBatchMode(final BatchMode batchMode) {
            checkNotNull(batchMode);

            this.batchMode = batchMode;
            return this;
        }

        public BatchCancelTPParams build() {
            return new BatchCancelTPParams(this);
        }
    }
}
