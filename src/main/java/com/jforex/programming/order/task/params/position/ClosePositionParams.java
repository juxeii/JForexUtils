package com.jforex.programming.order.task.params.position;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import com.dukascopy.api.IOrder;
import com.dukascopy.api.Instrument;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.event.OrderEventType;
import com.jforex.programming.order.task.CloseExecutionMode;
import com.jforex.programming.order.task.params.CommonParamsBuilder;
import com.jforex.programming.order.task.params.ComposeData;
import com.jforex.programming.order.task.params.ComposeParams;
import com.jforex.programming.order.task.params.ComposeParamsForOrder;
import com.jforex.programming.order.task.params.RetryParams;

import io.reactivex.functions.Action;

public class ClosePositionParams {

    private final Instrument instrument;
    private final String mergeOrderLabel;
    private final Map<OrderEventType, Consumer<OrderEvent>> consumerForEvent;
    private final MergePositionParams mergePositionParams;
    private final ComposeParams closePositionComposeParams;
    private final ComposeParamsForOrder closeComposeParams;
    private final CloseExecutionMode closeExecutionMode;

    private ClosePositionParams(final Builder builder) {
        instrument = builder.instrument;
        mergeOrderLabel = builder.mergeOrderLabel;
        closeExecutionMode = builder.closeExecutionMode;
        mergePositionParams = builder.mergePositionParams;
        closePositionComposeParams = builder.closePositionComposeParams;
        closeComposeParams = builder.closeComposeParams;
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

    public ComposeData closePositionComposeParams() {
        return closePositionComposeParams;
    }

    public ComposeData closeComposeParams(final IOrder order) {
        return closeComposeParams.convertWithOrder(order);
    }

    public MergePositionParams mergePositionParams() {
        return mergePositionParams;
    }

    public static Builder newBuilder(final Instrument instrument,
                                     final String mergeOrderLabel) {
        checkNotNull(instrument);
        checkNotNull(mergeOrderLabel);

        return new Builder(instrument, mergeOrderLabel);
    }

    public static class Builder extends CommonParamsBuilder<Builder> {

        private final Instrument instrument;
        private final String mergeOrderLabel;
        private MergePositionParams mergePositionParams;
        private final ComposeParams closePositionComposeParams = new ComposeParams();
        private final ComposeParamsForOrder closeComposeParams = new ComposeParamsForOrder();
        private CloseExecutionMode closeExecutionMode = CloseExecutionMode.CloseAll;

        public Builder(final Instrument instrument,
                       final String mergeOrderLabel) {
            this.instrument = instrument;
            this.mergeOrderLabel = mergeOrderLabel;
            mergePositionParams = MergePositionParams
                .newBuilder(instrument, mergeOrderLabel)
                .build();
        }

        public Builder withMergePositionParams(final MergePositionParams mergePositionParams) {
            checkNotNull(mergePositionParams);

            this.mergePositionParams = mergePositionParams;
            return this;
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

        public Builder doOnCloseStart(final Function<IOrder, Action> closeStartAction) {
            checkNotNull(closeStartAction);

            closeComposeParams.setStartAction(closeStartAction);
            return this;
        }

        public Builder doOnCloseComplete(final Function<IOrder, Action> closeCompleteAction) {
            checkNotNull(closeCompleteAction);

            closeComposeParams.setCompleteAction(closeCompleteAction);
            return this;
        }

        public Builder doOnCloseError(final BiConsumer<Throwable, IOrder> closeErrorConsumer) {
            checkNotNull(closeErrorConsumer);

            closeComposeParams.setErrorConsumer(closeErrorConsumer);
            return this;
        }

        public Builder retryOnCloseReject(final int noOfRetries,
                                          final long delayInMillis) {
            closeComposeParams.setRetryParams(new RetryParams(noOfRetries, delayInMillis));
            return this;
        }

        public Builder doOnClose(final Consumer<OrderEvent> closeConsumer) {
            return setEventConsumer(OrderEventType.CLOSE_OK, closeConsumer);
        }

        public Builder doOnPartialClose(final Consumer<OrderEvent> partialCloseConsumer) {
            return setEventConsumer(OrderEventType.PARTIAL_CLOSE_OK, partialCloseConsumer);
        }

        public Builder doOnCloseReject(final Consumer<OrderEvent> rejectConsumer) {
            return setEventConsumer(OrderEventType.CLOSE_REJECTED, rejectConsumer);
        }

        public ClosePositionParams build() {
            return new ClosePositionParams(this);
        }
    }
}
