package com.jforex.programming.order.task.params.position;

import java.util.function.Consumer;

import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.event.OrderEventType;

public class CancelTPParams extends ParamsBaseForCancel {

    private CancelTPParams(final Builder builder) {
        super(builder);
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static class Builder extends ParamsBuilderForCancel<Builder> {

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
