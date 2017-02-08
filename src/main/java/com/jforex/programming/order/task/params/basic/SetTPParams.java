package com.jforex.programming.order.task.params.basic;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.function.Consumer;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.event.OrderEventType;
import com.jforex.programming.order.task.params.BasicTaskParamsBase;
import com.jforex.programming.order.task.params.SetSLTPMode;
import com.jforex.programming.order.task.params.TaskParamsType;

public class SetTPParams extends BasicTaskParamsBase {

    private final IOrder order;
    private final double priceOrPips;
    private final SetSLTPMode setSLTPMode;

    private SetTPParams(final Builder builder) {
        super(builder);

        this.order = builder.order;
        this.priceOrPips = builder.priceOrPips;
        this.setSLTPMode = builder.setSLTPMode;
    }

    public IOrder order() {
        return order;
    }

    public double priceOrPips() {
        return priceOrPips;
    }

    public SetSLTPMode setSLTPMode() {
        return setSLTPMode;
    }

    @Override
    public TaskParamsType type() {
        return TaskParamsType.SETTP;
    }

    public static Builder setTPAtPrice(final IOrder order,
                                       final double newTP) {
        checkNotNull(order);

        return new Builder(order,
                           newTP,
                           SetSLTPMode.PRICE);
    }

    public static Builder setTPWithPips(final IOrder order,
                                        final double pips) {
        checkNotNull(order);

        return new Builder(order,
                           pips,
                           SetSLTPMode.PIPS);
    }

    public static class Builder extends BasicParamsBuilder<Builder> {

        private final IOrder order;
        private final double priceOrPips;
        private final SetSLTPMode setSLTPMode;

        public Builder(final IOrder order,
                       final double priceOrPips,
                       final SetSLTPMode setSLTPMode) {
            this.order = order;
            this.priceOrPips = priceOrPips;
            this.setSLTPMode = setSLTPMode;
        }

        public Builder doOnChangedTP(final Consumer<OrderEvent> changedTPConsumer) {
            return setEventConsumer(OrderEventType.CHANGED_TP, changedTPConsumer);
        }

        public Builder doOnReject(final Consumer<OrderEvent> changeRejectConsumer) {
            return setEventConsumer(OrderEventType.CHANGE_TP_REJECTED, changeRejectConsumer);
        }

        public SetTPParams build() {
            return new SetTPParams(this);
        }
    }
}
