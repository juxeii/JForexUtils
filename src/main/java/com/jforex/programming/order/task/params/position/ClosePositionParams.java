package com.jforex.programming.order.task.params.position;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

import com.dukascopy.api.IOrder;
import com.dukascopy.api.Instrument;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.event.OrderEventType;
import com.jforex.programming.order.task.BatchMode;
import com.jforex.programming.order.task.CloseExecutionMode;
import com.jforex.programming.order.task.params.CommonParamsBuilder;
import com.jforex.programming.order.task.params.ComposeData;
import com.jforex.programming.order.task.params.ComposeParams;
import com.jforex.programming.order.task.params.RetryParams;
import com.jforex.programming.order.task.params.basic.CloseParams;

import io.reactivex.functions.Action;

public class ClosePositionParams {

    private final Instrument instrument;
    private final String mergeOrderLabel;
    private final Map<OrderEventType, Consumer<OrderEvent>> consumerForEvent;
    private final MergePositionParams mergePositionParams;
    private final Function<IOrder, CloseParams> closeParamsFactory;
    private final ComposeParams closePositionComposeParams;
    private final CloseExecutionMode closeExecutionMode;
    private final BatchMode closeBatchMode;

    private ClosePositionParams(final Builder builder) {
        instrument = builder.instrument;
        mergeOrderLabel = builder.mergeOrderLabel;
        closeExecutionMode = builder.closeExecutionMode;
        closeBatchMode = builder.closeBatchMode;
        mergePositionParams = builder.mergePositionParams;
        closeParamsFactory = builder.closeParamsFactory;
        closePositionComposeParams = builder.closePositionComposeParams;
        consumerForEvent = builder.consumerForEvent;
        consumerForEvent.putAll(mergePositionParams.consumerForEvent());
    }

    public Instrument instrument() {
        return instrument;
    }

    public String mergeOrderLabel() {
        return mergeOrderLabel;
    }

    public Map<OrderEventType, Consumer<OrderEvent>> consumerForEvent() {
        return consumerForEvent;
    }

    public CloseExecutionMode closeExecutionMode() {
        return closeExecutionMode;
    }

    public BatchMode closeBatchMode() {
        return closeBatchMode;
    }

    public ComposeData closePositionComposeParams() {
        return closePositionComposeParams;
    }

    public MergePositionParams mergePositionParams() {
        return mergePositionParams;
    }

    public CloseParams closeParamsFactory(final IOrder order) {
        return closeParamsFactory.apply(order);
    }

    public static Builder newBuilder(final MergePositionParams mergePositionParams,
                                     final Function<IOrder, CloseParams> closeParamsFactory) {
        checkNotNull(mergePositionParams);
        checkNotNull(closeParamsFactory);

        return new Builder(mergePositionParams, closeParamsFactory);
    }

    public static class Builder extends CommonParamsBuilder<Builder> {

        private final Instrument instrument;
        private final String mergeOrderLabel;
        private final MergePositionParams mergePositionParams;
        private final Function<IOrder, CloseParams> closeParamsFactory;
        private final ComposeParams closePositionComposeParams = new ComposeParams();
        private CloseExecutionMode closeExecutionMode = CloseExecutionMode.CloseAll;
        private final BatchMode closeBatchMode = BatchMode.MERGE;

        public Builder(final MergePositionParams mergePositionParams,
                       final Function<IOrder, CloseParams> closeParamsFactory) {
            this.mergePositionParams = mergePositionParams;
            this.instrument = mergePositionParams.instrument();
            this.mergeOrderLabel = mergePositionParams.mergeOrderLabel();
            this.closeParamsFactory = closeParamsFactory;
        }

        public Builder withCloseExecutionMode(final CloseExecutionMode closeExecutionMode) {
            checkNotNull(closeExecutionMode);

            this.closeExecutionMode = closeExecutionMode;
            return this;
        }

        public Builder doOnClosePositionStart(final Action closePositionStartAction) {
            checkNotNull(closePositionStartAction);

            closePositionComposeParams.setStartAction(closePositionStartAction);
            return this;
        }

        public Builder doOnClosePositionComplete(final Action closePositionCompleteAction) {
            checkNotNull(closePositionCompleteAction);

            closePositionComposeParams.setCompleteAction(closePositionCompleteAction);
            return this;
        }

        public Builder doOnClosePositionError(final Consumer<Throwable> closePositionErrorConsumer) {
            checkNotNull(closePositionErrorConsumer);

            closePositionComposeParams.setErrorConsumer(closePositionErrorConsumer);
            return this;
        }

        public Builder retryOnClosePositionReject(final int noOfRetries,
                                                  final long delayInMillis) {
            closePositionComposeParams.setRetryParams(new RetryParams(noOfRetries, delayInMillis));
            return this;
        }

        public ClosePositionParams build() {
            return new ClosePositionParams(this);
        }
    }
}
