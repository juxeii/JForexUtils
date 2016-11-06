package com.jforex.programming.order.task.params;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Map;
import java.util.function.BiConsumer;

import com.dukascopy.api.Instrument;
import com.jforex.programming.order.event.OrderEventType;
import com.jforex.programming.order.task.BatchMode;

import io.reactivex.functions.Action;

public class BatchCancelSLParams implements RetryParams {

    private final CancelSLParams cancelSLParams;
    private final BatchMode batchMode;
    private final Map<OrderEventType, OrderEventConsumer> consumerForEvent;
    private final BiConsumer<Throwable, Instrument> errorConsumer;
    private final InstrumentConsumer startConsumer;
    private final InstrumentConsumer completeConsumer;
    private final int noOfRetries;
    private final long delayInMillis;

    private BatchCancelSLParams(final Builder builder) {
        cancelSLParams = builder.cancelSLParams;
        batchMode = builder.batchMode;
        errorConsumer = builder.errorConsumer;
        startConsumer = builder.startConsumer;
        completeConsumer = builder.completeConsumer;
        noOfRetries = builder.noOfRetries;
        delayInMillis = builder.delayInMillis;

        consumerForEvent = cancelSLParams.consumerForEvent();
    }

    public final CancelSLParams cancelSLParams() {
        return cancelSLParams;
    }

    public final BatchMode batchMode() {
        return batchMode;
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

        private CancelSLParams cancelSLParams = CancelSLParams.newBuilder().build();
        private BatchMode batchMode = BatchMode.MERGE;
        private BiConsumer<Throwable, Instrument> errorConsumer;
        private InstrumentConsumer startConsumer;
        private InstrumentConsumer completeConsumer;
        private int noOfRetries;
        private long delayInMillis;

        public Builder withCancelSLParams(final CancelSLParams cancelSLParams) {
            checkNotNull(cancelSLParams);

            this.cancelSLParams = cancelSLParams;
            return this;
        }

        public Builder withBatchMode(final BatchMode batchMode) {
            checkNotNull(batchMode);

            this.batchMode = batchMode;
            return this;
        }

        public Builder doOnStart(final InstrumentConsumer startConsumer) {
            checkNotNull(startConsumer);

            this.startConsumer = startConsumer;
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

        public BatchCancelSLParams build() {
            return new BatchCancelSLParams(this);
        }
    }
}
