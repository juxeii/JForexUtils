package com.jforex.programming.order.task.params;

import static com.google.common.base.Preconditions.checkNotNull;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.event.OrderEventType;

public class SetAmountParams extends BasicTaskParamsBase {

    private final IOrder order;
    private final double newAmount;

    private SetAmountParams(final Builder builder) {
        super(builder);

        this.order = builder.order;
        this.newAmount = builder.newAmount;
    }

    public IOrder order() {
        return order;
    }

    public double newAmount() {
        return newAmount;
    }

    public static Builder setAmountWith(final IOrder order,
                                        final double newAmount) {
        checkNotNull(order);

        return new Builder(order, newAmount);
    }

    public static class Builder extends GeneralBuilder<Builder> {

        private final IOrder order;
        private final double newAmount;

        public Builder(final IOrder order,
                       final double newAmount) {
            this.order = order;
            this.newAmount = newAmount;
        }

        public Builder doOnChangedAmount(final OrderEventConsumer changedAmountConsumer) {
            return setEventConsumer(OrderEventType.CHANGED_AMOUNT, changedAmountConsumer);
        }

        public Builder doOnReject(final OrderEventConsumer changeRejectConsumer) {
            return setEventConsumer(OrderEventType.CHANGE_AMOUNT_REJECTED, changeRejectConsumer);
        }

        public SetAmountParams build() {
            return new SetAmountParams(this);
        }
    }
}
