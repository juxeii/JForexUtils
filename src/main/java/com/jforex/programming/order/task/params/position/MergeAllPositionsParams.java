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

public class MergeAllPositionsParams implements TaskParamsBase {

    private final Function<Instrument, MergePositionParams> paramsFactory;
    private final ComposeData mergeAllPositionsComposeData;

    private MergeAllPositionsParams(final Builder builder) {
        paramsFactory = builder.paramsFactory;
        mergeAllPositionsComposeData = builder.mergeAllPositionsComposeParams;
    }

    public MergePositionParams paramsForInstrument(final Instrument instrument) {
        return paramsFactory.apply(instrument);
    }

    @Override
    public TaskParamsType type() {
        return TaskParamsType.MERGEALLPOSITIONS;
    }

    @Override
    public ComposeData composeData() {
        return mergeAllPositionsComposeData;
    }

    @Override
    public Map<OrderEventType, Consumer<OrderEvent>> consumerForEvent() {
        return new HashMap<OrderEventType, Consumer<OrderEvent>>();
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
                                                      final RetryDelayFunction delayFunction) {
            mergeAllPositionsComposeParams.setRetryParams(new RetryParams(noOfRetries, delayFunction));
            return this;
        }

        public MergeAllPositionsParams build() {
            return new MergeAllPositionsParams(this);
        }
    }
}
