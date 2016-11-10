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
import com.jforex.programming.order.task.BatchMode;
import com.jforex.programming.order.task.MergeExecutionMode;
import com.jforex.programming.order.task.params.CommonParamsBuilder;

import io.reactivex.functions.Action;

public class MergePositionParams {

    private final Instrument instrument;
    private final String mergeOrderLabel;
    private final Map<OrderEventType, Consumer<OrderEvent>> consumerForEvent;

    private final Action mergePositionStartAction;
    private final Action mergePositionCompleteAction;
    private final Consumer<Throwable> mergePositionErrorConsumer;
    private final RetryParams mergePositionRetryParams;

    private final Action cancelSLTPStartAction;
    private final Action cancelSLTPCompleteAction;
    private final Consumer<Throwable> cancelSLTPErrorConsumer;
    private final MergeExecutionMode mergeExecutionMode;
    private final RetryParams cancelSLTPRetryParams;

    private final Action batchCancelSLStartAction;
    private final Action batchCancelSLCompleteAction;
    private final Consumer<Throwable> batchCancelSLErrorConsumer;
    private final BatchMode batchCancelSLMode;
    private final RetryParams batchCancelSLRetryParams;

    private final Action batchCancelTPStartAction;
    private final Action batchCancelTPCompleteAction;
    private final Consumer<Throwable> batchCancelTPErrorConsumer;
    private final BatchMode batchCancelTPMode;
    private final RetryParams batchCancelTPRetryParams;

    private final Function<IOrder, Action> cancelSLStartAction;
    private final Function<IOrder, Action> cancelSLCompleteAction;
    private final BiConsumer<Throwable, IOrder> cancelSLErrorConsumer;
    private final RetryParams cancelSLRetryParams;

    private final Function<IOrder, Action> cancelTPStartAction;
    private final Function<IOrder, Action> cancelTPCompleteAction;
    private final BiConsumer<Throwable, IOrder> cancelTPErrorConsumer;
    private final RetryParams cancelTPRetryParams;

    private final Action mergeStartAction;
    private final Action mergeCompleteAction;
    private final Consumer<Throwable> mergeErrorConsumer;
    private final RetryParams mergeRetryParams;

    private MergePositionParams(final Builder builder) {
        instrument = builder.instrument;
        mergeOrderLabel = builder.mergeOrderLabel;
        consumerForEvent = builder.consumerForEvent;

        mergePositionStartAction = builder.mergePositionStartAction;
        mergePositionCompleteAction = builder.mergePositionCompleteAction;
        mergePositionErrorConsumer = builder.mergePositionErrorConsumer;
        mergePositionRetryParams = builder.mergePositionRetryParams;

        cancelSLTPStartAction = builder.cancelSLTPStartAction;
        cancelSLTPCompleteAction = builder.cancelSLTPCompleteAction;
        cancelSLTPErrorConsumer = builder.cancelSLTPErrorConsumer;
        mergeExecutionMode = builder.mergeExecutionMode;
        cancelSLTPRetryParams = builder.cancelSLTPRetryParams;

        batchCancelSLStartAction = builder.batchCancelSLStartAction;
        batchCancelSLCompleteAction = builder.batchCancelSLCompleteAction;
        batchCancelSLErrorConsumer = builder.batchCancelSLErrorConsumer;
        batchCancelSLMode = builder.batchCancelSLMode;
        batchCancelSLRetryParams = builder.batchCancelSLRetryParams;

        batchCancelTPStartAction = builder.batchCancelTPStartAction;
        batchCancelTPCompleteAction = builder.batchCancelTPCompleteAction;
        batchCancelTPErrorConsumer = builder.batchCancelTPErrorConsumer;
        batchCancelTPMode = builder.batchCancelTPMode;
        batchCancelTPRetryParams = builder.batchCancelTPRetryParams;

        cancelSLStartAction = builder.cancelSLStartAction;
        cancelSLCompleteAction = builder.cancelSLCompleteAction;
        cancelSLErrorConsumer = builder.cancelSLErrorConsumer;
        cancelSLRetryParams = builder.cancelSLRetryParams;

        cancelTPStartAction = builder.cancelTPStartAction;
        cancelTPCompleteAction = builder.cancelTPCompleteAction;
        cancelTPErrorConsumer = builder.cancelTPErrorConsumer;
        cancelTPRetryParams = builder.cancelTPRetryParams;

        mergeStartAction = builder.mergeStartAction;
        mergeCompleteAction = builder.mergeCompleteAction;
        mergeErrorConsumer = builder.mergeErrorConsumer;
        mergeRetryParams = builder.mergeRetryParams;
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

    public Action mergePositionStartAction() {
        return mergePositionStartAction;
    }

    public Action mergePositionCompleteAction() {
        return mergePositionCompleteAction;
    }

    public Consumer<Throwable> mergePositionErrorConsumer() {
        return mergePositionErrorConsumer;
    }

    public RetryParams mergePositionRetryParams() {
        return mergePositionRetryParams;
    }

    public Action cancelSLTPStartAction() {
        return cancelSLTPStartAction;
    }

    public Action cancelSLTPCompleteAction() {
        return cancelSLTPCompleteAction;
    }

    public Consumer<Throwable> cancelSLTPErrorConsumer() {
        return cancelSLTPErrorConsumer;
    }

    public MergeExecutionMode mergeExecutionMode() {
        return mergeExecutionMode;
    }

    public RetryParams cancelSLTPRetryParams() {
        return cancelSLTPRetryParams;
    }

    public Action batchCancelSLStartAction() {
        return batchCancelSLStartAction;
    }

    public Action batchCancelSLCompleteAction() {
        return batchCancelSLCompleteAction;
    }

    public Consumer<Throwable> batchCancelSLErrorConsumer() {
        return batchCancelSLErrorConsumer;
    }

    public BatchMode batchCancelSLMode() {
        return batchCancelSLMode;
    }

    public RetryParams batchCancelSLRetryParams() {
        return batchCancelSLRetryParams;
    }

    public Action batchCancelTPStartAction() {
        return batchCancelTPStartAction;
    }

    public Action batchCancelTPCompleteAction() {
        return batchCancelTPCompleteAction;
    }

    public Consumer<Throwable> batchCancelTPErrorConsumer() {
        return batchCancelTPErrorConsumer;
    }

    public BatchMode batchCancelTPMode() {
        return batchCancelTPMode;
    }

    public RetryParams batchCancelTPRetryParams() {
        return batchCancelTPRetryParams;
    }

    public Action cancelSLStartAction(final IOrder order) {
        return cancelSLStartAction.apply(order);
    }

    public Action cancelSLCompleteAction(final IOrder order) {
        return cancelSLCompleteAction.apply(order);
    }

    public Consumer<Throwable> cancelSLErrorConsumer(final IOrder order) {
        return err -> cancelSLErrorConsumer.accept(err, order);
    }

    public RetryParams cancelSLRetryParams() {
        return cancelSLRetryParams;
    }

    public Action cancelTPStartAction(final IOrder order) {
        return cancelTPStartAction.apply(order);
    }

    public Action cancelTPCompleteAction(final IOrder order) {
        return cancelTPCompleteAction.apply(order);
    }

    public Consumer<Throwable> cancelTPErrorConsumer(final IOrder order) {
        return err -> cancelTPErrorConsumer.accept(err, order);
    }

    public RetryParams cancelTPRetryParams() {
        return cancelTPRetryParams;
    }

    public Action mergeStartAction() {
        return mergeStartAction;
    }

    public Action mergeCompleteAction() {
        return mergeCompleteAction;
    }

    public Consumer<Throwable> mergeErrorConsumer() {
        return mergeErrorConsumer;
    }

    public RetryParams mergeRetryParams() {
        return mergeRetryParams;
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
        private final RetryParams emptyRetryParams = new RetryParams(0, 0L);

        private Action mergePositionStartAction = () -> {};
        private Action mergePositionCompleteAction = () -> {};
        private Consumer<Throwable> mergePositionErrorConsumer = t -> {};
        private RetryParams mergePositionRetryParams = emptyRetryParams;

        private Action cancelSLTPStartAction = () -> {};
        private Action cancelSLTPCompleteAction = () -> {};
        private Consumer<Throwable> cancelSLTPErrorConsumer = t -> {};
        private MergeExecutionMode mergeExecutionMode = MergeExecutionMode.MergeCancelSLAndTP;
        private RetryParams cancelSLTPRetryParams = emptyRetryParams;

        private Action batchCancelSLStartAction = () -> {};
        private Action batchCancelSLCompleteAction = () -> {};
        private Consumer<Throwable> batchCancelSLErrorConsumer = t -> {};
        private BatchMode batchCancelSLMode = BatchMode.MERGE;
        private RetryParams batchCancelSLRetryParams = emptyRetryParams;

        private Action batchCancelTPStartAction = () -> {};
        private Action batchCancelTPCompleteAction = () -> {};
        private Consumer<Throwable> batchCancelTPErrorConsumer = t -> {};
        private BatchMode batchCancelTPMode = BatchMode.MERGE;
        private RetryParams batchCancelTPRetryParams = emptyRetryParams;

        private Function<IOrder, Action> cancelSLStartAction = o -> () -> {};
        private Function<IOrder, Action> cancelSLCompleteAction = o -> () -> {};
        private BiConsumer<Throwable, IOrder> cancelSLErrorConsumer = (t, o) -> {};
        private RetryParams cancelSLRetryParams = emptyRetryParams;

        private Function<IOrder, Action> cancelTPStartAction = o -> () -> {};
        private Function<IOrder, Action> cancelTPCompleteAction = o -> () -> {};
        private BiConsumer<Throwable, IOrder> cancelTPErrorConsumer = (t, o) -> {};
        private RetryParams cancelTPRetryParams = emptyRetryParams;

        private Action mergeStartAction = () -> {};
        private Action mergeCompleteAction = () -> {};
        private Consumer<Throwable> mergeErrorConsumer = t -> {};
        private RetryParams mergeRetryParams = emptyRetryParams;

        public Builder(final Instrument instrument,
                       final String mergeOrderLabel) {
            this.instrument = instrument;
            this.mergeOrderLabel = mergeOrderLabel;
        }

        public Builder doOnMergePositionStart(final Action mergePositionStartAction) {
            checkNotNull(mergePositionStartAction);

            this.mergePositionStartAction = mergePositionStartAction;
            return this;
        }

        public Builder doOnMergePositionComplete(final Action mergePositionCompleteAction) {
            checkNotNull(mergePositionCompleteAction);

            this.mergePositionCompleteAction = mergePositionCompleteAction;
            return this;
        }

        public Builder doOnMergePositionError(final Consumer<Throwable> mergePositionErrorConsumer) {
            checkNotNull(mergePositionErrorConsumer);

            this.mergePositionErrorConsumer = mergePositionErrorConsumer;
            return this;
        }

        public Builder retryOnMergePositionReject(final int noOfRetries,
                                                  final long delayInMillis) {
            mergePositionRetryParams = new RetryParams(noOfRetries, delayInMillis);
            return this;
        }

        public Builder doOnCancelSLTPStart(final Action cancelSLTPStartAction) {
            checkNotNull(cancelSLTPStartAction);

            this.cancelSLTPStartAction = cancelSLTPStartAction;
            return this;
        }

        public Builder doOnCancelSLTPComplete(final Action cancelSLTPCompleteAction) {
            checkNotNull(cancelSLTPCompleteAction);

            this.cancelSLTPCompleteAction = cancelSLTPCompleteAction;
            return this;
        }

        public Builder doOnCancelSLTPError(final Consumer<Throwable> cancelSLTPErrorConsumer) {
            checkNotNull(cancelSLTPErrorConsumer);

            this.cancelSLTPErrorConsumer = cancelSLTPErrorConsumer;
            return this;
        }

        public Builder withMergeExecutionMode(final MergeExecutionMode mergeExecutionMode) {
            checkNotNull(mergeExecutionMode);

            this.mergeExecutionMode = mergeExecutionMode;
            return this;
        }

        public Builder retryOnCancelSLTPReject(final int noOfRetries,
                                               final long delayInMillis) {
            cancelSLTPRetryParams = new RetryParams(noOfRetries, delayInMillis);
            return this;
        }

        public Builder doOnBatchCancelSLStart(final Action batchCancelSLStartAction) {
            checkNotNull(batchCancelSLStartAction);

            this.batchCancelSLStartAction = batchCancelSLStartAction;
            return this;
        }

        public Builder doOnBatchCancelSLComplete(final Action batchCancelSLCompleteAction) {
            checkNotNull(batchCancelSLCompleteAction);

            this.batchCancelSLCompleteAction = batchCancelSLCompleteAction;
            return this;
        }

        public Builder doOnBatchCancelSLError(final Consumer<Throwable> batchCancelSLErrorConsumer) {
            checkNotNull(batchCancelSLErrorConsumer);

            this.batchCancelSLErrorConsumer = batchCancelSLErrorConsumer;
            return this;
        }

        public Builder withBatchCancelSLMode(final BatchMode batchCancelSLMode) {
            checkNotNull(batchCancelSLMode);

            this.batchCancelSLMode = batchCancelSLMode;
            return this;
        }

        public Builder retryOnBatchCancelSLReject(final int noOfRetries,
                                                  final long delayInMillis) {
            batchCancelSLRetryParams = new RetryParams(noOfRetries, delayInMillis);
            return this;
        }

        public Builder doOnBatchCancelTPStart(final Action batchCancelTPStartAction) {
            checkNotNull(batchCancelTPStartAction);

            this.batchCancelTPStartAction = batchCancelTPStartAction;
            return this;
        }

        public Builder doOnBatchCancelTPComplete(final Action batchCancelTPCompleteAction) {
            checkNotNull(batchCancelTPCompleteAction);

            this.batchCancelTPCompleteAction = batchCancelTPCompleteAction;
            return this;
        }

        public Builder doOnBatchCancelTPError(final Consumer<Throwable> batchCancelTPErrorConsumer) {
            checkNotNull(batchCancelTPErrorConsumer);

            this.batchCancelTPErrorConsumer = batchCancelTPErrorConsumer;
            return this;
        }

        public Builder withBatchCancelTPMode(final BatchMode batchCancelTPMode) {
            checkNotNull(batchCancelTPMode);

            this.batchCancelTPMode = batchCancelTPMode;
            return this;
        }

        public Builder retryOnBatchCancelTPReject(final int noOfRetries,
                                                  final long delayInMillis) {
            batchCancelTPRetryParams = new RetryParams(noOfRetries, delayInMillis);
            return this;
        }

        public Builder doOnMergeStart(final Action mergeStartAction) {
            checkNotNull(mergeStartAction);

            this.mergeStartAction = mergeStartAction;
            return this;
        }

        public Builder doOnMergeComplete(final Action mergeCompleteAction) {
            checkNotNull(mergeCompleteAction);

            this.mergeCompleteAction = mergeCompleteAction;
            return this;
        }

        public Builder doOnMergeError(final Consumer<Throwable> mergeErrorConsumer) {
            checkNotNull(mergeErrorConsumer);

            this.mergeErrorConsumer = mergeErrorConsumer;
            return this;
        }

        public Builder retryOnMergeReject(final int noOfRetries,
                                          final long delayInMillis) {
            mergeRetryParams = new RetryParams(noOfRetries, delayInMillis);
            return this;
        }

        public Builder doOnMerge(final Consumer<OrderEvent> mergeConsumer) {
            return setEventConsumer(OrderEventType.MERGE_OK, mergeConsumer);
        }

        public Builder doOnMergeClose(final Consumer<OrderEvent> mergeCloseConsumer) {
            return setEventConsumer(OrderEventType.MERGE_CLOSE_OK, mergeCloseConsumer);
        }

        public Builder doOnMergeReject(final Consumer<OrderEvent> rejectConsumer) {
            return setEventConsumer(OrderEventType.MERGE_REJECTED, rejectConsumer);
        }

        public Builder doOnCancelSLStart(final Function<IOrder, Action> cancelSLStartAction) {
            checkNotNull(cancelSLStartAction);

            this.cancelSLStartAction = cancelSLStartAction;
            return this;
        }

        public Builder doOnCancelSLComplete(final Function<IOrder, Action> cancelSLCompleteAction) {
            checkNotNull(cancelSLCompleteAction);

            this.cancelSLCompleteAction = cancelSLCompleteAction;
            return this;
        }

        public Builder doOnCancelSLError(final BiConsumer<Throwable, IOrder> cancelSLErrorConsumer) {
            checkNotNull(cancelSLErrorConsumer);

            this.cancelSLErrorConsumer = cancelSLErrorConsumer;
            return this;
        }

        public Builder retryOnCancelSLReject(final int noOfRetries,
                                             final long delayInMillis) {
            cancelSLRetryParams = new RetryParams(noOfRetries, delayInMillis);
            return this;
        }

        public Builder doOnCancelSL(final Consumer<OrderEvent> cancelSLConsumer) {
            return setEventConsumer(OrderEventType.CHANGED_SL, cancelSLConsumer);
        }

        public Builder doOnCancelSLReject(final Consumer<OrderEvent> cancelSLRejectConsumer) {
            return setEventConsumer(OrderEventType.CHANGE_SL_REJECTED, cancelSLRejectConsumer);
        }

        public Builder doOnCancelTPStart(final Function<IOrder, Action> cancelTPStartAction) {
            checkNotNull(cancelTPStartAction);

            this.cancelTPStartAction = cancelTPStartAction;
            return this;
        }

        public Builder doOnCancelTPComplete(final Function<IOrder, Action> cancelTPCompleteAction) {
            checkNotNull(cancelTPCompleteAction);

            this.cancelTPCompleteAction = cancelTPCompleteAction;
            return this;
        }

        public Builder doOnCancelTPError(final BiConsumer<Throwable, IOrder> cancelTPErrorConsumer) {
            checkNotNull(cancelTPErrorConsumer);

            this.cancelTPErrorConsumer = cancelTPErrorConsumer;
            return this;
        }

        public Builder retryOnCancelTPReject(final int noOfRetries,
                                             final long delayInMillis) {
            cancelTPRetryParams = new RetryParams(noOfRetries, delayInMillis);
            return this;
        }

        public Builder doOnCancelTP(final Consumer<OrderEvent> cancelTPConsumer) {
            return setEventConsumer(OrderEventType.CHANGED_TP, cancelTPConsumer);
        }

        public Builder doOnCancelTPReject(final Consumer<OrderEvent> cancelTPRejectConsumer) {
            return setEventConsumer(OrderEventType.CHANGE_TP_REJECTED, cancelTPRejectConsumer);
        }

        public MergePositionParams build() {
            return new MergePositionParams(this);
        }
    }
}
