package com.jforex.programming.order.task.params.basic;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.function.Consumer;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.event.OrderEventType;
import com.jforex.programming.order.task.params.TaskParamsBase;

public class CancelTPParams extends TaskParamsBase {

    private final IOrder order;

    private CancelTPParams(final Builder builder) {
        super(builder);

        this.order = builder.order;
    }

    public IOrder order() {
        return order;
    }

    public static Builder withOrder(final IOrder order) {
        checkNotNull(order);

        return new Builder(order);
    }

    public static class Builder extends TaskParamsBase.Builder<Builder> {

        private final IOrder order;

        public Builder(final IOrder order) {
            this.order = order;
        }

        public Builder doOnCancelTP(final Consumer<OrderEvent> cancelTPConsumer) {
            setEventConsumer(OrderEventType.CHANGED_TP, cancelTPConsumer);
            return this;
        }

        public Builder doOnReject(final Consumer<OrderEvent> changeRejectConsumer) {
            setEventConsumer(OrderEventType.CHANGE_TP_REJECTED, changeRejectConsumer);
            return this;
        }

        public CancelTPParams build() {
            return new CancelTPParams(this);
        }
    }
}
