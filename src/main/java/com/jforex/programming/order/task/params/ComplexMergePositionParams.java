package com.jforex.programming.order.task.params;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Map;
import java.util.function.BiConsumer;

import com.dukascopy.api.Instrument;
import com.jforex.programming.order.event.OrderEventType;

import io.reactivex.functions.Action;

public class ComplexMergePositionParams implements RetryParams {

    private final BatchCancelSLAndTPParams batchCancelSLAndTPParams;
    private final MergePositionParams mergePositionParams;
    private final Map<OrderEventType, OrderEventConsumer> consumerForEvent;
    private final BiConsumer<Throwable, Instrument> errorConsumer;
    private final InstrumentConsumer startConsumer;
    private final InstrumentConsumer completeConsumer;
    private final int noOfRetries;
    private final long delayInMillis;

    private ComplexMergePositionParams(final Builder builder) {
        batchCancelSLAndTPParams = builder.batchCancelSLAndTPParams;
        mergePositionParams = builder.mergePositionParams;
        errorConsumer = builder.errorConsumer;
        startConsumer = builder.startConsumer;
        completeConsumer = builder.completeConsumer;
        noOfRetries = builder.noOfRetries;
        delayInMillis = builder.delayInMillis;

        consumerForEvent = batchCancelSLAndTPParams.consumerForEvent();
        consumerForEvent.putAll(mergePositionParams.consumerForEvent());
    }

    public BatchCancelSLAndTPParams batchCancelSLAndTPParams() {
        return batchCancelSLAndTPParams;
    }

    public MergePositionParams mergePositionParams() {
        return mergePositionParams;
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

    public int noOfRetries() {
        return noOfRetries;
    }

    public long delayInMillis() {
        return delayInMillis;
    }

    public static Builder withMergeParams(final MergePositionParams mergePositionParams) {
        checkNotNull(mergePositionParams);

        return new Builder(mergePositionParams);
    }

    public static class Builder {

        private final BatchCancelSLAndTPParams batchCancelSLAndTPParams =
                BatchCancelSLAndTPParams.newBuilder().build();
        private MergePositionParams mergePositionParams;
        private BiConsumer<Throwable, Instrument> errorConsumer;
        private InstrumentConsumer startConsumer;
        private InstrumentConsumer completeConsumer;
        private int noOfRetries;
        private long delayInMillis;

        public Builder(final MergePositionParams mergePositionParams) {
            this.mergePositionParams = mergePositionParams;
        }

        public Builder withMergeParams(final MergePositionParams mergePositionParams) {
            checkNotNull(mergePositionParams);

            this.mergePositionParams = mergePositionParams;
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

        public ComplexMergePositionParams build() {
            return new ComplexMergePositionParams(this);
        }
    }
}
