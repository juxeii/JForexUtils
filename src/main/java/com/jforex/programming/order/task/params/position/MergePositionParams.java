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
import com.jforex.programming.order.task.params.ComposeData;
import com.jforex.programming.order.task.params.ComposeDataForOrder;
import com.jforex.programming.order.task.params.ComposeParams;
import com.jforex.programming.order.task.params.ComposeParamsForOrder;
import com.jforex.programming.order.task.params.RetryParams;

import io.reactivex.functions.Action;

public class MergePositionParams {

    private final Instrument instrument;
    private final String mergeOrderLabel;
    private final Map<OrderEventType, Consumer<OrderEvent>> consumerForEvent;

    private final ComposeData mergePositionComposeParams;
    private final ComposeData cancelSLTPComposeParams;
    private final ComposeData batchCancelSLComposeParams;
    private final ComposeData batchCancelTPComposeParams;
    private final ComposeDataForOrder cancelSLComposeParams;
    private final ComposeDataForOrder cancelTPComposeParams;
    private final ComposeData mergeComposeParams;

    private final MergeExecutionMode mergeExecutionMode;
    private final BatchMode batchCancelSLMode;
    private final BatchMode batchCancelTPMode;

    private MergePositionParams(final Builder builder) {
        instrument = builder.instrument;
        mergeOrderLabel = builder.mergeOrderLabel;
        consumerForEvent = builder.consumerForEvent;

        mergePositionComposeParams = builder.mergePositionComposeParams;
        cancelSLTPComposeParams = builder.cancelSLTPComposeParams;
        batchCancelSLComposeParams = builder.batchCancelSLComposeParams;
        batchCancelTPComposeParams = builder.batchCancelTPComposeParams;
        cancelSLComposeParams = builder.cancelSLComposeParams;
        cancelTPComposeParams = builder.cancelTPComposeParams;
        mergeComposeParams = builder.mergeComposeParams;

        mergeExecutionMode = builder.mergeExecutionMode;
        batchCancelSLMode = builder.batchCancelSLMode;
        batchCancelTPMode = builder.batchCancelTPMode;
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

    public ComposeData mergePositionComposeParams() {
        return mergePositionComposeParams;
    }

    public ComposeData cancelSLTPComposeParams() {
        return cancelSLTPComposeParams;
    }

    public ComposeData batchCancelSLComposeParams() {
        return batchCancelSLComposeParams;
    }

    public ComposeData batchCancelTPComposeParams() {
        return batchCancelTPComposeParams;
    }

    public ComposeDataForOrder cancelSLComposeParams() {
        return cancelSLComposeParams;
    }

    public ComposeDataForOrder cancelTPComposeParams() {
        return cancelTPComposeParams;
    }

    public ComposeData mergeComposeParams() {
        return mergeComposeParams;
    }

    public MergeExecutionMode mergeExecutionMode() {
        return mergeExecutionMode;
    }

    public BatchMode batchCancelSLMode() {
        return batchCancelSLMode;
    }

    public BatchMode batchCancelTPMode() {
        return batchCancelTPMode;
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

        private final ComposeParams mergePositionComposeParams = new ComposeParams();
        private final ComposeParams cancelSLTPComposeParams = new ComposeParams();
        private final ComposeParams batchCancelSLComposeParams = new ComposeParams();
        private final ComposeParams batchCancelTPComposeParams = new ComposeParams();
        private final ComposeParamsForOrder cancelSLComposeParams = new ComposeParamsForOrder();
        private final ComposeParamsForOrder cancelTPComposeParams = new ComposeParamsForOrder();
        private final ComposeParams mergeComposeParams = new ComposeParams();

        private MergeExecutionMode mergeExecutionMode = MergeExecutionMode.MergeCancelSLAndTP;
        private BatchMode batchCancelSLMode = BatchMode.MERGE;
        private BatchMode batchCancelTPMode = BatchMode.MERGE;

        public Builder(final Instrument instrument,
                       final String mergeOrderLabel) {
            this.instrument = instrument;
            this.mergeOrderLabel = mergeOrderLabel;
        }

        public Builder doOnMergePositionStart(final Action mergePositionStartAction) {
            checkNotNull(mergePositionStartAction);

            mergePositionComposeParams.setStartAction(mergePositionStartAction);
            return this;
        }

        public Builder doOnMergePositionComplete(final Action mergePositionCompleteAction) {
            checkNotNull(mergePositionCompleteAction);

            mergePositionComposeParams.setCompleteAction(mergePositionCompleteAction);
            return this;
        }

        public Builder doOnMergePositionError(final Consumer<Throwable> mergePositionErrorConsumer) {
            checkNotNull(mergePositionErrorConsumer);

            mergePositionComposeParams.setErrorConsumer(mergePositionErrorConsumer);
            return this;
        }

        public Builder retryOnMergePositionReject(final int noOfRetries,
                                                  final long delayInMillis) {
            mergePositionComposeParams.setRetryParams(new RetryParams(noOfRetries, delayInMillis));
            return this;
        }

        public Builder doOnCancelSLTPStart(final Action cancelSLTPStartAction) {
            checkNotNull(cancelSLTPStartAction);

            cancelSLTPComposeParams.setStartAction(cancelSLTPStartAction);
            return this;
        }

        public Builder doOnCancelSLTPComplete(final Action cancelSLTPCompleteAction) {
            checkNotNull(cancelSLTPCompleteAction);

            cancelSLTPComposeParams.setCompleteAction(cancelSLTPCompleteAction);
            return this;
        }

        public Builder doOnCancelSLTPError(final Consumer<Throwable> cancelSLTPErrorConsumer) {
            checkNotNull(cancelSLTPErrorConsumer);

            cancelSLTPComposeParams.setErrorConsumer(cancelSLTPErrorConsumer);
            return this;
        }

        public Builder retryOnCancelSLTPReject(final int noOfRetries,
                                               final long delayInMillis) {
            cancelSLTPComposeParams.setRetryParams(new RetryParams(noOfRetries, delayInMillis));
            return this;
        }

        public Builder withMergeExecutionMode(final MergeExecutionMode mergeExecutionMode) {
            checkNotNull(mergeExecutionMode);

            this.mergeExecutionMode = mergeExecutionMode;
            return this;
        }

        public Builder doOnBatchCancelSLStart(final Action batchCancelSLStartAction) {
            checkNotNull(batchCancelSLStartAction);

            batchCancelSLComposeParams.setStartAction(batchCancelSLStartAction);
            return this;
        }

        public Builder doOnBatchCancelSLComplete(final Action batchCancelSLCompleteAction) {
            checkNotNull(batchCancelSLCompleteAction);

            batchCancelSLComposeParams.setCompleteAction(batchCancelSLCompleteAction);
            return this;
        }

        public Builder doOnBatchCancelSLError(final Consumer<Throwable> batchCancelSLErrorConsumer) {
            checkNotNull(batchCancelSLErrorConsumer);

            batchCancelSLComposeParams.setErrorConsumer(batchCancelSLErrorConsumer);
            return this;
        }

        public Builder retryOnBatchCancelSLReject(final int noOfRetries,
                                                  final long delayInMillis) {
            batchCancelSLComposeParams.setRetryParams(new RetryParams(noOfRetries, delayInMillis));
            return this;
        }

        public Builder withBatchCancelSLMode(final BatchMode batchCancelSLMode) {
            checkNotNull(batchCancelSLMode);

            this.batchCancelSLMode = batchCancelSLMode;
            return this;
        }

        public Builder doOnBatchCancelTPStart(final Action batchCancelTPStartAction) {
            checkNotNull(batchCancelTPStartAction);

            batchCancelTPComposeParams.setStartAction(batchCancelTPStartAction);
            return this;
        }

        public Builder doOnBatchCancelTPComplete(final Action batchCancelTPCompleteAction) {
            checkNotNull(batchCancelTPCompleteAction);

            batchCancelTPComposeParams.setCompleteAction(batchCancelTPCompleteAction);
            return this;
        }

        public Builder doOnBatchCancelTPError(final Consumer<Throwable> batchCancelTPErrorConsumer) {
            checkNotNull(batchCancelTPErrorConsumer);

            batchCancelTPComposeParams.setErrorConsumer(batchCancelTPErrorConsumer);
            return this;
        }

        public Builder retryOnBatchCancelTPReject(final int noOfRetries,
                                                  final long delayInMillis) {
            batchCancelTPComposeParams.setRetryParams(new RetryParams(noOfRetries, delayInMillis));
            return this;
        }

        public Builder withBatchCancelTPMode(final BatchMode batchCancelTPMode) {
            checkNotNull(batchCancelTPMode);

            this.batchCancelTPMode = batchCancelTPMode;
            return this;
        }

        public Builder doOnMergeStart(final Action mergeStartAction) {
            checkNotNull(mergeStartAction);

            mergeComposeParams.setStartAction(mergeStartAction);
            return this;
        }

        public Builder doOnMergeComplete(final Action mergeCompleteAction) {
            checkNotNull(mergeCompleteAction);

            mergeComposeParams.setCompleteAction(mergeCompleteAction);
            return this;
        }

        public Builder doOnMergeError(final Consumer<Throwable> mergeErrorConsumer) {
            checkNotNull(mergeErrorConsumer);

            mergeComposeParams.setErrorConsumer(mergeErrorConsumer);
            return this;
        }

        public Builder retryOnMergeReject(final int noOfRetries,
                                          final long delayInMillis) {
            mergeComposeParams.setRetryParams(new RetryParams(noOfRetries, delayInMillis));
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

            cancelSLComposeParams.setStartAction(cancelSLStartAction);
            return this;
        }

        public Builder doOnCancelSLComplete(final Function<IOrder, Action> cancelSLCompleteAction) {
            checkNotNull(cancelSLCompleteAction);

            cancelSLComposeParams.setCompleteAction(cancelSLCompleteAction);
            return this;
        }

        public Builder doOnCancelSLError(final BiConsumer<Throwable, IOrder> cancelSLErrorConsumer) {
            checkNotNull(cancelSLErrorConsumer);

            cancelSLComposeParams.setErrorConsumer(cancelSLErrorConsumer);
            return this;
        }

        public Builder retryOnCancelSLReject(final int noOfRetries,
                                             final long delayInMillis) {
            cancelSLComposeParams.setRetryParams(new RetryParams(noOfRetries, delayInMillis));
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

            cancelTPComposeParams.setStartAction(cancelTPStartAction);
            return this;
        }

        public Builder doOnCancelTPComplete(final Function<IOrder, Action> cancelTPCompleteAction) {
            checkNotNull(cancelTPCompleteAction);

            cancelTPComposeParams.setCompleteAction(cancelTPCompleteAction);
            return this;
        }

        public Builder doOnCancelTPError(final BiConsumer<Throwable, IOrder> cancelTPErrorConsumer) {
            checkNotNull(cancelTPErrorConsumer);

            cancelTPComposeParams.setErrorConsumer(cancelTPErrorConsumer);
            return this;
        }

        public Builder retryOnCancelTPReject(final int noOfRetries,
                                             final long delayInMillis) {
            cancelTPComposeParams.setRetryParams(new RetryParams(noOfRetries, delayInMillis));
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
