package com.jforex.programming.order.task.params.position;

import java.util.function.Consumer;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.event.OrderEventType;

public class CancelSLParams extends PositionParamsBase<IOrder> {

    private CancelSLParams(final Builder builder) {
        super(builder);
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static class Builder extends PositionParamsBuilder<Builder, IOrder> {

        public Builder doOnCancelSL(final Consumer<OrderEvent> cancelSLConsumer) {
            return setEventConsumer(OrderEventType.CHANGED_SL, cancelSLConsumer);
        }

        public Builder doOnReject(final Consumer<OrderEvent> changeRejectConsumer) {
            return setEventConsumer(OrderEventType.CHANGE_SL_REJECTED, changeRejectConsumer);
        }

        public CancelSLParams build() {
            return new CancelSLParams(this);
        }
    }
}
