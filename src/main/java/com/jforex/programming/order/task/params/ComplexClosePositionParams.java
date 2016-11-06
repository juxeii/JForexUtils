package com.jforex.programming.order.task.params;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Map;
import java.util.function.BiConsumer;

import com.dukascopy.api.Instrument;
import com.jforex.programming.order.event.OrderEventType;
import com.jforex.programming.order.task.CloseExecutionMode;

import io.reactivex.functions.Action;

public class ComplexClosePositionParams implements RetryParams {

    private final ComplexMergePositionParams complexMergePositionParams;
    private final ClosePositionParams closePositionParams;
    private final CloseExecutionMode closeExecutionMode;
    private final Map<OrderEventType, OrderEventConsumer> consumerForEvent;
    private final BiConsumer<Throwable, Instrument> errorConsumer;
    private final InstrumentConsumer startConsumer;
    private final InstrumentConsumer completeConsumer;
    private final int noOfRetries;
    private final long delayInMillis;

    public interface BuildOption {

        public ClosePositionParams build();
    }

    private ComplexClosePositionParams(final Builder builder) {
        complexMergePositionParams = builder.complexMergePositionParams;
        closePositionParams = builder.closePositionParams;
        closeExecutionMode = builder.closeExecutionMode;
        errorConsumer = builder.errorConsumer;
        startConsumer = builder.startConsumer;
        completeConsumer = builder.completeConsumer;
        noOfRetries = builder.noOfRetries;
        delayInMillis = builder.delayInMillis;

        consumerForEvent = complexMergePositionParams.consumerForEvent();
        consumerForEvent.putAll(closePositionParams.consumerForEvent());
    }

    public final Map<OrderEventType, OrderEventConsumer> consumerForEvent() {
        return consumerForEvent;
    }

    public ComplexMergePositionParams complexMergePositionParams() {
        return complexMergePositionParams;
    }

    public ClosePositionParams closePositionParams() {
        return closePositionParams;
    }

    public CloseExecutionMode closeExecutionMode() {
        return closeExecutionMode;
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

    public static Builder newBuilder() {
        return new Builder();
    }

    public static class Builder {

        private ComplexMergePositionParams complexMergePositionParams;
        private ClosePositionParams closePositionParams;
        private CloseExecutionMode closeExecutionMode;
        private BiConsumer<Throwable, Instrument> errorConsumer;
        private InstrumentConsumer startConsumer;
        private InstrumentConsumer completeConsumer;
        private int noOfRetries;
        private long delayInMillis;

        public Builder withMergeParams(final ComplexMergePositionParams complexMergePositionParams) {
            checkNotNull(complexMergePositionParams);

            this.complexMergePositionParams = complexMergePositionParams;
            return this;
        }

        public Builder withClosePositionParams(final ClosePositionParams closePositionParams) {
            checkNotNull(closePositionParams);

            this.closePositionParams = closePositionParams;
            return this;
        }

        public Builder withCloseExecutionMode(final CloseExecutionMode closeExecutionMode) {
            checkNotNull(closeExecutionMode);

            this.closeExecutionMode = closeExecutionMode;
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

        public ComplexClosePositionParams build() {
            return new ComplexClosePositionParams(this);
        }
    }
}
