package com.jforex.programming.order.task.params.basic;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.function.Consumer;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.event.OrderEventType;
import com.jforex.programming.order.task.params.CommonParamsBase;

public class SetGTTParams extends CommonParamsBase {

    private final IOrder order;
    private final long newGTT;

    private SetGTTParams(final Builder builder) {
        super(builder);

        this.order = builder.order;
        this.newGTT = builder.newGTT;
    }

    public IOrder order() {
        return order;
    }

    public long newGTT() {
        return newGTT;
    }

    public static Builder setGTTWith(final IOrder order,
                                     final long newGTT) {
        checkNotNull(order);

        return new Builder(order, newGTT);
    }

    public static class Builder extends BasicParamsBuilder<Builder> {

        private final IOrder order;
        private final long newGTT;

        public Builder(final IOrder order,
                       final long newGTT) {
            this.order = order;
            this.newGTT = newGTT;
        }

        public Builder doOnChangedGTT(final Consumer<OrderEvent> changedGTTConsumer) {
            return setEventConsumer(OrderEventType.CHANGED_GTT, changedGTTConsumer);
        }

        public Builder doOnReject(final Consumer<OrderEvent> changeRejectConsumer) {
            return setEventConsumer(OrderEventType.CHANGE_GTT_REJECTED, changeRejectConsumer);
        }

        public SetGTTParams build() {
            return new SetGTTParams(this);
        }
    }
}
