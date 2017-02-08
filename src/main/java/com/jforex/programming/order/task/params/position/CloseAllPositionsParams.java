package com.jforex.programming.order.task.params.position;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

import com.dukascopy.api.Instrument;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.event.OrderEventType;
import com.jforex.programming.order.task.params.CommonParamsBuilder;
import com.jforex.programming.order.task.params.ComposeData;
import com.jforex.programming.order.task.params.ComposeParams;
import com.jforex.programming.order.task.params.RetryParams;
import com.jforex.programming.order.task.params.TaskParamsBase;
import com.jforex.programming.order.task.params.TaskParamsType;
import com.jforex.programming.rx.RetryDelayFunction;

import io.reactivex.functions.Action;

public class CloseAllPositionsParams implements TaskParamsBase {

    private final Function<Instrument, ClosePositionParams> paramsFactory;
    private final ComposeData closeAllPositionsComposeData;

    private CloseAllPositionsParams(final Builder builder) {
        paramsFactory = builder.paramsFactory;
        closeAllPositionsComposeData = builder.closeAllPositionsComposeParams;
    }

    public ClosePositionParams paramsForInstrument(final Instrument instrument) {
        return paramsFactory.apply(instrument);
    }

    @Override
    public TaskParamsType type() {
        return TaskParamsType.CLOSEALLPOSITIONS;
    }

    @Override
    public ComposeData composeData() {
        return closeAllPositionsComposeData;
    }

    @Override
    public Map<OrderEventType, Consumer<OrderEvent>> consumerForEvent() {
        return new HashMap<OrderEventType, Consumer<OrderEvent>>();
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
                                                      final RetryDelayFunction delayFunction) {
            closeAllPositionsComposeParams.setRetryParams(new RetryParams(noOfRetries, delayFunction));
            return this;
        }

        public CloseAllPositionsParams build() {
            return new CloseAllPositionsParams(this);
        }
    }
}
