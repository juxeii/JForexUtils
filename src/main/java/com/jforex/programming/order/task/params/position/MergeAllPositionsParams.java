package com.jforex.programming.order.task.params.position;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.function.Consumer;
import java.util.function.Function;

import com.dukascopy.api.Instrument;
import com.jforex.programming.order.task.params.CommonParamsBuilder;
import com.jforex.programming.order.task.params.ComposeParams;
import com.jforex.programming.order.task.params.RetryParams;

import io.reactivex.functions.Action;

public class MergeAllPositionsParams {

    private final Function<Instrument, MergePositionParams> paramsFactory;
    private final ComposeParams mergeAllPositionsComposeParams;

    private MergeAllPositionsParams(final Builder builder) {
        paramsFactory = builder.paramsFactory;
        mergeAllPositionsComposeParams = builder.mergeAllPositionsComposeParams;
    }

    public Function<Instrument, MergePositionParams> paramsFactory() {
        return paramsFactory;
    }

    public ComposeParams mergeAllPositionsComposeParams() {
        return mergeAllPositionsComposeParams;
    }

    public static Builder newBuilder(final Function<Instrument, MergePositionParams> paramsFactory) {
        checkNotNull(paramsFactory);

        return new Builder(paramsFactory);
    }

    public static class Builder extends CommonParamsBuilder<Builder> {

        private final Function<Instrument, MergePositionParams> paramsFactory;
        private final ComposeParams mergeAllPositionsComposeParams = new ComposeParams();

        public Builder(final Function<Instrument, MergePositionParams> paramsFactory) {
            this.paramsFactory = paramsFactory;
        }

        public Builder doOnMergeAllPositionsStart(final Action mergeAllPositionsStartAction) {
            checkNotNull(mergeAllPositionsStartAction);

            mergeAllPositionsComposeParams.setStartAction(mergeAllPositionsStartAction);
            return this;
        }

        public Builder doOnMergeAllPositionsComplete(final Action mergeAllPositionsCompleteAction) {
            checkNotNull(mergeAllPositionsCompleteAction);

            mergeAllPositionsComposeParams.setCompleteAction(mergeAllPositionsCompleteAction);
            return this;
        }

        public Builder doOnMergeAllPositionsError(final Consumer<Throwable> mergeAllPositionsErrorConsumer) {
            checkNotNull(mergeAllPositionsErrorConsumer);

            mergeAllPositionsComposeParams.setErrorConsumer(mergeAllPositionsErrorConsumer);
            return this;
        }

        public Builder retryOnMergeAllPositionsReject(final int noOfRetries,
                                                      final long delayInMillis) {
            mergeAllPositionsComposeParams.setRetryParams(new RetryParams(noOfRetries, delayInMillis));
            return this;
        }

        public MergeAllPositionsParams build() {
            return new MergeAllPositionsParams(this);
        }
    }
}
