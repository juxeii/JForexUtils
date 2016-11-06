package com.jforex.programming.order.task.params;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.event.OrderEventType;

import io.reactivex.functions.Action;

public class CancelTPParams implements RetryParams {

    private final Map<OrderEventType, OrderEventConsumer> consumerForEvent;
    private final BiConsumer<Throwable, IOrder> errorConsumer;
    private final OrderConsumer startConsumer;
    private final OrderConsumer completeConsumer;
    private final int noOfRetries;
    private final long delayInMillis;

    private CancelTPParams(final Builder builder) {
        consumerForEvent = builder.consumerForEvent;
        errorConsumer = builder.errorConsumer;
        startConsumer = builder.startConsumer;
        completeConsumer = builder.completeConsumer;
        noOfRetries = builder.noOfRetries;
        delayInMillis = builder.delayInMillis;
    }

    public final Map<OrderEventType, OrderEventConsumer> consumerForEvent() {
        return consumerForEvent;
    }

    public final Action startAction(final IOrder order) {
        return () -> startConsumer.accept(order);
    }

    public final Action completeAction(final IOrder order) {
        return () -> completeConsumer.accept(order);
    }

    public final ErrorConsumer errorConsumer(final IOrder order) {
        return err -> errorConsumer.accept(err, order);
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

        private final Map<OrderEventType, OrderEventConsumer> consumerForEvent = new HashMap<>();
        private BiConsumer<Throwable, IOrder> errorConsumer;
        private OrderConsumer startConsumer;
        private OrderConsumer completeConsumer;
        private int noOfRetries;
        private long delayInMillis;

        private Builder setEventConsumer(final OrderEventType orderEventType,
                                         final OrderEventConsumer consumer) {
            checkNotNull(consumer);

            consumerForEvent.put(orderEventType, consumer);
            return this;
        }

        public Builder doOnStart(final OrderConsumer startConsumer) {
            checkNotNull(startConsumer);

            this.startConsumer = startConsumer;
            return this;
        }

        public Builder doOnException(final BiConsumer<Throwable, IOrder> errorConsumer) {
            checkNotNull(errorConsumer);

            this.errorConsumer = errorConsumer;
            return this;
        }

        public Builder doOnComplete(final OrderConsumer completeConsumer) {
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

        public Builder doOnCancelTP(final OrderEventConsumer cancelTPConsumer) {
            return setEventConsumer(OrderEventType.CHANGED_TP, cancelTPConsumer);
        }

        public Builder doOnReject(final OrderEventConsumer changeRejectConsumer) {
            return setEventConsumer(OrderEventType.CHANGE_TP_REJECTED, changeRejectConsumer);
        }

        public CancelTPParams build() {
            return new CancelTPParams(this);
        }
    }
}
