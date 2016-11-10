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
import com.jforex.programming.order.task.params.basic.BasicParamsBuilder;

import io.reactivex.functions.Action;

public class ClosePositionParams {

    private final Instrument instrument;
    private final MergePositionParams mergePositionParams;
    private final Map<OrderEventType, Consumer<OrderEvent>> consumerForEvent;
    private final CloseExecutionMode closeExecutionMode;

    private final Action closePositionStartAction;
    private final Action closePositionCompleteAction;
    private final Consumer<Throwable> closePositionErrorConsumer;
    private final RetryParams closePositionRetryParams;

    private final Function<IOrder, Action> closeStartAction;
    private final Function<IOrder, Action> closeCompleteAction;
    private final BiConsumer<Throwable, IOrder> closeErrorConsumer;
    private final RetryParams closeRetryParams;

    private ClosePositionParams(final Builder builder) {
        instrument = builder.instrument;
        mergePositionParams = builder.mergePositionParams;
        consumerForEvent = builder.consumerForEvent;
        consumerForEvent.putAll(mergePositionParams.consumerForEvent());

        closeExecutionMode = builder.closeExecutionMode;

        closePositionStartAction = builder.closePositionStartAction;
        closePositionCompleteAction = builder.closePositionCompleteAction;
        closePositionErrorConsumer = builder.closePositionErrorConsumer;
        closePositionRetryParams = builder.closePositionRetryParams;

        closeStartAction = builder.closeStartAction;
        closeCompleteAction = builder.closeCompleteAction;
        closeErrorConsumer = builder.closeErrorConsumer;
        closeRetryParams = builder.closeRetryParams;
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

    public Action closePositionStartAction() {
        return closePositionStartAction;
    }

    public Action closePositionCompleteAction() {
        return closePositionCompleteAction;
    }

    public Consumer<Throwable> closePositionErrorConsumer() {
        return closePositionErrorConsumer;
    }

    public RetryParams closePositionRetryParams() {
        return closePositionRetryParams;
    }

    public Action closeStartAction(final IOrder order) {
        return closeStartAction.apply(order);
    }

    public Action closeCompleteAction(final IOrder order) {
        return closeCompleteAction.apply(order);
    }

    public Consumer<Throwable> closeErrorConsumer(final IOrder order) {
        return err -> closeErrorConsumer.accept(err, order);
    }

    public RetryParams closeRetryParams() {
        return closeRetryParams;
    }

    public static Builder newBuilder(final MergePositionParams mergePositionParams) {
        checkNotNull(mergePositionParams);

        return new Builder(mergePositionParams);
    }

    public static class Builder extends BasicParamsBuilder<Builder> {

        private final Instrument instrument;
        private final MergePositionParams mergePositionParams;
        private CloseExecutionMode closeExecutionMode = CloseExecutionMode.CloseAll;
        private final RetryParams emptyRetryParams = new RetryParams(0, 0L);

        private Action closePositionStartAction = () -> {};
        private Action closePositionCompleteAction = () -> {};
        private Consumer<Throwable> closePositionErrorConsumer = t -> {};
        private RetryParams closePositionRetryParams = emptyRetryParams;

        private Function<IOrder, Action> closeStartAction = o -> () -> {};
        private Function<IOrder, Action> closeCompleteAction = o -> () -> {};
        private BiConsumer<Throwable, IOrder> closeErrorConsumer = (t, o) -> {};
        private RetryParams closeRetryParams = emptyRetryParams;

        public Builder(final MergePositionParams mergePositionParams) {
            this.instrument = mergePositionParams.instrument();
            this.mergePositionParams = mergePositionParams;
        }

        public Builder doOnclosePositionStart(final Action closePositionStartAction) {
            checkNotNull(closePositionStartAction);

            this.closePositionStartAction = closePositionStartAction;
            return this;
        }

        public Builder doOnclosePositionComplete(final Action closePositionCompleteAction) {
            checkNotNull(closePositionCompleteAction);

            this.closePositionCompleteAction = closePositionCompleteAction;
            return this;
        }

        public Builder doOnclosePositionError(final Consumer<Throwable> closePositionErrorConsumer) {
            checkNotNull(closePositionErrorConsumer);

            this.closePositionErrorConsumer = closePositionErrorConsumer;
            return this;
        }

        public Builder retryOnclosePositionReject(final int noOfRetries,
                                                  final long delayInMillis) {
            closePositionRetryParams = new RetryParams(noOfRetries, delayInMillis);
            return this;
        }

        public Builder doOnCloseStart(final Function<IOrder, Action> closeStartAction) {
            checkNotNull(closeStartAction);

            this.closeStartAction = closeStartAction;
            return this;
        }

        public Builder doOnCloseComplete(final Function<IOrder, Action> closeCompleteAction) {
            checkNotNull(closeCompleteAction);

            this.closeCompleteAction = closeCompleteAction;
            return this;
        }

        public Builder doOnCloseError(final BiConsumer<Throwable, IOrder> closeErrorConsumer) {
            checkNotNull(closeErrorConsumer);

            this.closeErrorConsumer = closeErrorConsumer;
            return this;
        }

        public Builder retryOnCloseReject(final int noOfRetries,
                                          final long delayInMillis) {
            closeRetryParams = new RetryParams(noOfRetries, delayInMillis);
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
