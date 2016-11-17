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

    public ClosePositionParams paramsForInstrument(final Instrument instrument) {
        return paramsFactory.apply(instrument);
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

        public Builder doOnCloseAllPositionsStart(final Action closeAllPositionsStartAction) {
            checkNotNull(closeAllPositionsStartAction);

            closeAllPositionsComposeParams.setStartAction(closeAllPositionsStartAction);
            return this;
        }

        public Builder doOnCloseAllPositionsComplete(final Action closeAllPositionsCompleteAction) {
            checkNotNull(closeAllPositionsCompleteAction);

            closeAllPositionsComposeParams.setCompleteAction(closeAllPositionsCompleteAction);
            return this;
        }

        public Builder doOnCloseAllPositionsError(final Consumer<Throwable> closeAllPositionsErrorConsumer) {
            checkNotNull(closeAllPositionsErrorConsumer);

            closeAllPositionsComposeParams.setErrorConsumer(closeAllPositionsErrorConsumer);
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
