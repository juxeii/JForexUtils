package com.jforex.programming.order.task.params.basic;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.function.Consumer;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.event.OrderEventType;
import com.jforex.programming.order.task.params.TaskParamsBase;
import com.jforex.programming.order.task.params.TaskParamsType;
import com.jforex.programming.order.task.params.TaskParamsWithType;

public class SetAmountParams extends TaskParamsWithType {

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

    @Override
    public TaskParamsType type() {
        return TaskParamsType.SETAMOUNT;
    }

    public static Builder setAmountWith(final IOrder order,
                                        final double newAmount) {
        checkNotNull(order);

        return new Builder(order, newAmount);
    }

    public static class Builder extends TaskParamsBase.Builder<Builder> {

        private final IOrder order;
        private final double newAmount;

        public Builder(final IOrder order,
                       final double newAmount) {
            this.order = order;
            this.newAmount = newAmount;
        }

        public Builder doOnChangedAmount(final Consumer<OrderEvent> changedAmountConsumer) {
            setEventConsumer(OrderEventType.CHANGED_AMOUNT, changedAmountConsumer);
            return this;
        }

        public Builder doOnReject(final Consumer<OrderEvent> changeRejectConsumer) {
            setEventConsumer(OrderEventType.CHANGE_AMOUNT_REJECTED, changeRejectConsumer);
            return this;
        }

        public SetAmountParams build() {
            return new SetAmountParams(this);
        }
    }
}
