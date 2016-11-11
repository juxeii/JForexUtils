package com.jforex.programming.order.task.params.position;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.function.Consumer;
import java.util.function.Function;

import com.dukascopy.api.Instrument;
import com.jforex.programming.order.task.params.CommonParamsBuilder;
import com.jforex.programming.order.task.params.ComposeParams;
import com.jforex.programming.order.task.params.RetryParams;

import io.reactivex.functions.Action;

public class CloseAllPositionsParams {

    private final Function<Instrument, ClosePositionParams> paramsFactory;
    private final ComposeParams closeAllPositionsComposeParams;

    private CloseAllPositionsParams(final Builder builder) {
        paramsFactory = builder.paramsFactory;
        closeAllPositionsComposeParams = builder.closeAllPositionsComposeParams;
    }

    public Function<Instrument, ClosePositionParams> paramsFactory() {
        return paramsFactory;
    }

    public ComposeParams closeAllPositionsComposeParams() {
        return closeAllPositionsComposeParams;
    }

    public static Builder newBuilder(final Function<Instrument, ClosePositionParams> paramsFactory) {
        checkNotNull(paramsFactory);

        return new Builder(paramsFactory);
    }

    public static class Builder extends CommonParamsBuilder<Builder> {

        private final Function<Instrument, ClosePositionParams> paramsFactory;
        private final ComposeParams closeAllPositionsComposeParams = new ComposeParams();

        public Builder(final Function<Instrument, ClosePositionParams> paramsFactory) {
            this.paramsFactory = paramsFactory;
        }

        public Builder doOnCloseAllPositionsStart(final Action CloseAllPositionsStartAction) {
            checkNotNull(CloseAllPositionsStartAction);

            closeAllPositionsComposeParams.setStartAction(CloseAllPositionsStartAction);
            return this;
        }

        public Builder doOnCloseAllPositionsComplete(final Action CloseAllPositionsCompleteAction) {
            checkNotNull(CloseAllPositionsCompleteAction);

            closeAllPositionsComposeParams.setCompleteAction(CloseAllPositionsCompleteAction);
            return this;
        }

        public Builder doOnCloseAllPositionsError(final Consumer<Throwable> CloseAllPositionsErrorConsumer) {
            checkNotNull(CloseAllPositionsErrorConsumer);

            closeAllPositionsComposeParams.setErrorConsumer(CloseAllPositionsErrorConsumer);
            return this;
        }

        public Builder retryOnCloseAllPositionsReject(final int noOfRetries,
                                                      final long delayInMillis) {
            closeAllPositionsComposeParams.setRetryParams(new RetryParams(noOfRetries, delayInMillis));
            return this;
        }

        public CloseAllPositionsParams build() {
            return new CloseAllPositionsParams(this);
        }
    }
}
