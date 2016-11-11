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
import com.jforex.programming.order.task.params.ComposeParams;
import com.jforex.programming.order.task.params.ComposeParamsForOrder;
import com.jforex.programming.order.task.params.RetryParams;
import com.jforex.programming.order.task.params.basic.BasicParamsBuilder;

import io.reactivex.functions.Action;

public class ClosePositionParams {

    private final Instrument instrument;
    private final MergePositionParams mergePositionParams;
    private final Map<OrderEventType, Consumer<OrderEvent>> consumerForEvent;
    private final CloseExecutionMode closeExecutionMode;

    private final ComposeParams closePositionComposeParams;
    private final ComposeParamsForOrder closeComposeParams;

    private ClosePositionParams(final Builder builder) {
        instrument = builder.instrument;
        mergePositionParams = builder.mergePositionParams;
        closeExecutionMode = builder.closeExecutionMode;
        consumerForEvent = builder.consumerForEvent;
        consumerForEvent.putAll(mergePositionParams.consumerForEvent());

        closePositionComposeParams = builder.closePositionComposeParams;
        closeComposeParams = builder.closeComposeParams;
    }

    public Instrument instrument() {
        return instrument;
    }

    public MergePositionParams mergePositionParams() {
        return mergePositionParams;
    }

    public Map<OrderEventType, Consumer<OrderEvent>> consumerForEvent() {
        return consumerForEvent;
    }

    public CloseExecutionMode closeExecutionMode() {
        return closeExecutionMode;
    }

    public ComposeParams closePositionComposeParams() {
        return closePositionComposeParams;
    }

    public ComposeParamsForOrder closeComposeParams() {
        return closeComposeParams;
    }

    public static Builder newBuilder(final MergePositionParams mergePositionParams) {
        checkNotNull(mergePositionParams);

        return new Builder(mergePositionParams);
    }

    public static class Builder extends BasicParamsBuilder<Builder> {

        private final Instrument instrument;
        private final MergePositionParams mergePositionParams;
        private CloseExecutionMode closeExecutionMode = CloseExecutionMode.CloseAll;

        private final ComposeParams closePositionComposeParams = new ComposeParams();
        private final ComposeParamsForOrder closeComposeParams = new ComposeParamsForOrder();

        public Builder(final MergePositionParams mergePositionParams) {
            this.instrument = mergePositionParams.instrument();
            this.mergePositionParams = mergePositionParams;
        }

        public Builder doOnclosePositionStart(final Action closePositionStartAction) {
            checkNotNull(closePositionStartAction);

            closePositionComposeParams.setStartAction(closePositionStartAction);
            return this;
        }

        public Builder doOnclosePositionComplete(final Action closePositionCompleteAction) {
            checkNotNull(closePositionCompleteAction);

            closePositionComposeParams.setCompleteAction(closePositionCompleteAction);
            return this;
        }

        public Builder doOnclosePositionError(final Consumer<Throwable> closePositionErrorConsumer) {
            checkNotNull(closePositionErrorConsumer);

            closePositionComposeParams.setErrorConsumer(closePositionErrorConsumer);
            return this;
        }

        public Builder retryOnclosePositionReject(final int noOfRetries,
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

        public Builder withCloseExecutionMode(final CloseExecutionMode closeExecutionMode) {
            checkNotNull(closeExecutionMode);

            this.closeExecutionMode = closeExecutionMode;
            return this;
        }

        public ClosePositionParams build() {
            return new ClosePositionParams(this);
        }
    }
}
