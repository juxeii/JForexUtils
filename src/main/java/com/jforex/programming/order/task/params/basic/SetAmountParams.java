package com.jforex.programming.order.task.params.basic;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.function.Consumer;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.event.OrderEventType;
import com.jforex.programming.order.task.params.BasicTaskParamsBase;
import com.jforex.programming.order.task.params.TaskParamsType;

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

    public static class Builder extends BasicParamsBuilder<Builder> {

        private final IOrder order;
        private final double newAmount;

        public Builder(final IOrder order,
                       final double newAmount) {
            this.order = order;
            this.newAmount = newAmount;
        }

        public Builder doOnChangedAmount(final Consumer<OrderEvent> changedAmountConsumer) {
            return setEventConsumer(OrderEventType.CHANGED_AMOUNT, changedAmountConsumer);
        }

        public Builder doOnReject(final Consumer<OrderEvent> changeRejectConsumer) {
            return setEventConsumer(OrderEventType.CHANGE_AMOUNT_REJECTED, changeRejectConsumer);
        }

        public SetAmountParams build() {
            return new SetAmountParams(this);
        }
    }

    @Override
    public TaskParamsType type() {
        return TaskParamsType.SETAMOUNT;
    }
}
