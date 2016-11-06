package com.jforex.programming.order.task.params;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Map;
import java.util.function.BiConsumer;

import com.dukascopy.api.Instrument;
import com.jforex.programming.order.event.OrderEventType;
import com.jforex.programming.order.task.MergeExecutionMode;

import io.reactivex.functions.Action;

public class BatchCancelSLAndTPParams implements RetryParams {

    private final BatchCancelSLParams batchCancelSLParams;
    private final BatchCancelTPParams batchCancelTPParams;
    private final MergeExecutionMode mergeExecutionMode;
    private final Map<OrderEventType, OrderEventConsumer> consumerForEvent;
    private final BiConsumer<Throwable, Instrument> errorConsumer;
    private final InstrumentConsumer startConsumer;
    private final InstrumentConsumer completeConsumer;
    private final int noOfRetries;
    private final long delayInMillis;

    private BatchCancelSLAndTPParams(final Builder builder) {
        batchCancelSLParams = builder.batchCancelSLParams;
        batchCancelTPParams = builder.batchCancelTPParams;
        mergeExecutionMode = builder.mergeExecutionMode;
        errorConsumer = builder.errorConsumer;
        startConsumer = builder.startConsumer;
        completeConsumer = builder.completeConsumer;
        noOfRetries = builder.noOfRetries;
        delayInMillis = builder.delayInMillis;

        consumerForEvent = batchCancelSLParams.consumerForEvent();
        consumerForEvent.putAll(batchCancelTPParams.consumerForEvent());
    }

    public final BatchCancelSLParams batchCancelSLParams() {
        return batchCancelSLParams;
    }

    public final BatchCancelTPParams batchCancelTPParams() {
        return batchCancelTPParams;
    }

    public final MergeExecutionMode mergeExecutionMode() {
        return mergeExecutionMode;
    }

    public final Map<OrderEventType, OrderEventConsumer> consumerForEvent() {
        return consumerForEvent;
    }

    public final Action startAction(final Instrument instrument) {
        return () -> startConsumer.accept(instrument);
    }

    public final Action completeAction(final Instrument instrument) {
        return () -> completeConsumer.accept(instrument);
    }

    public final ErrorConsumer errorConsumer(final Instrument instrument) {
        return err -> errorConsumer.accept(err, instrument);
    }

    @Override
    public int noOfRetries() {
        return noOfRetries;
    }

    @Override
    public long delayInMillis() {
        return delayInMillis;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static class Builder {

        private BatchCancelSLParams batchCancelSLParams = BatchCancelSLParams.newBuilder().build();
        private BatchCancelTPParams batchCancelTPParams = BatchCancelTPParams.newBuilder().build();
        private MergeExecutionMode mergeExecutionMode = MergeExecutionMode.MergeCancelSLAndTP;
        private BiConsumer<Throwable, Instrument> errorConsumer;
        private InstrumentConsumer startConsumer;
        private InstrumentConsumer completeConsumer;
        private int noOfRetries;
        private long delayInMillis;

        public Builder doOnStart(final InstrumentConsumer startConsumer) {
            checkNotNull(startConsumer);

            this.startConsumer = startConsumer;
            return this;
        }

        public Builder withBatchCancelSLParams(final BatchCancelSLParams batchCancelSLParams) {
            checkNotNull(batchCancelSLParams);

            this.batchCancelSLParams = batchCancelSLParams;
            return this;
        }

        public Builder withBatchCancelTPParams(final BatchCancelTPParams batchCancelTPParams) {
            checkNotNull(batchCancelTPParams);

            this.batchCancelTPParams = batchCancelTPParams;
            return this;
        }

        public Builder withMergeExecutionMode(final MergeExecutionMode mergeExecutionMode) {
            checkNotNull(mergeExecutionMode);

            this.mergeExecutionMode = mergeExecutionMode;
            return this;
        }

        public Builder doOnException(final BiConsumer<Throwable, Instrument> errorConsumer) {
            checkNotNull(errorConsumer);

            this.errorConsumer = errorConsumer;
            return this;
        }

        public Builder doOnComplete(final InstrumentConsumer completeConsumer) {
            checkNotNull(completeConsumer);

            this.completeConsumer = completeConsumer;
            return this;
        }

        public Builder retryOnReject(final int noOfRetries,
                                     final long delayInMillis) {
            this.noOfRetries = noOfRetries;
            this.delayInMillis = delayInMillis;
            return this;
        }

        public BatchCancelSLAndTPParams build() {
            return new BatchCancelSLAndTPParams(this);
        }
    }
}
