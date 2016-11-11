package com.jforex.programming.order.task.params.basic;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.function.Consumer;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.event.OrderEventType;
import com.jforex.programming.order.task.params.CommonParamsBase;

public class CancelTPParams extends CommonParamsBase {

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

    public static class Builder extends BasicParamsBuilder<Builder> {

        private final IOrder order;

        public Builder(final IOrder order) {
            this.order = order;
        }

        public Builder doOnCancelTP(final Consumer<OrderEvent> cancelTPConsumer) {
            return setEventConsumer(OrderEventType.CHANGED_TP, cancelTPConsumer);
        }

        public Builder doOnReject(final Consumer<OrderEvent> changeRejectConsumer) {
            return setEventConsumer(OrderEventType.CHANGE_TP_REJECTED, changeRejectConsumer);
        }

        public CancelTPParams build() {
            return new CancelTPParams(this);
        }
    }
}
