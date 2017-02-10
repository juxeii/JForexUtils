package com.jforex.programming.order.task.params.basic;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.function.Consumer;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.event.OrderEventType;
import com.jforex.programming.order.task.params.TaskParamsBase;
import com.jforex.programming.order.task.params.TaskParamsType;
import com.jforex.programming.order.task.params.TaskParamsWithType;

public class SetOpenPriceParams extends TaskParamsWithType {

    private final IOrder order;
    private final double newOpenPrice;

    private SetOpenPriceParams(final Builder builder) {
        super(builder);

        this.order = builder.order;
        this.newOpenPrice = builder.newOpenPrice;
    }

    public IOrder order() {
        return order;
    }

    public double newOpenPrice() {
        return newOpenPrice;
    }

    @Override
    public TaskParamsType type() {
        return TaskParamsType.SETOPENPRICE;
    }

    public static Builder setOpenPriceWith(final IOrder order,
                                           final double newOpenPrice) {
        checkNotNull(order);

        return new Builder(order, newOpenPrice);
    }

    public static class Builder extends TaskParamsBase.Builder<Builder> {

        private final IOrder order;
        private final double newOpenPrice;

        public Builder(final IOrder order,
                       final double newOpenPrice) {
            this.order = order;
            this.newOpenPrice = newOpenPrice;
        }

        public Builder doOnChangedOpenPrice(final Consumer<OrderEvent> changedOpenPriceConsumer) {
            setEventConsumer(OrderEventType.CHANGED_PRICE, changedOpenPriceConsumer);
            return this;
        }

        public Builder doOnReject(final Consumer<OrderEvent> changeRejectConsumer) {
            setEventConsumer(OrderEventType.CHANGE_PRICE_REJECTED, changeRejectConsumer);
            return this;
        }

        public SetOpenPriceParams build() {
            return new SetOpenPriceParams(this);
        }
    }
}
