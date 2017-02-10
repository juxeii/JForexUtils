package com.jforex.programming.order.task.params.basic;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Optional;
import java.util.function.Consumer;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.event.OrderEventType;
import com.jforex.programming.order.task.params.TaskParamsBase;
import com.jforex.programming.order.task.params.TaskParamsType;
import com.jforex.programming.order.task.params.TaskParamsWithType;
import com.jforex.programming.strategy.StrategyUtil;

public class CloseParams extends TaskParamsWithType {

    private final IOrder order;
    private final double partialCloseAmount;
    private final Optional<Double> maybePrice;
    private final double slippage;

    private static final double defaultCloseSlippage = StrategyUtil.platformSettings.defaultCloseSlippage();
    private static final double noCloseSlippageValue = Double.NaN;

    private CloseParams(final Builder builder) {
        super(builder);

        order = builder.order;
        partialCloseAmount = builder.partialCloseAmount;
        maybePrice = builder.maybePrice;
        slippage = evalSlippage(builder.slippage);
    }

    private double evalSlippage(final double builderSlippage) {
        if (builderSlippage == 0.0)
            return noCloseSlippageValue;
        return builderSlippage < 0
                ? defaultCloseSlippage
                : builderSlippage;
    }

    @Override
    public TaskParamsType type() {
        return TaskParamsType.CLOSE;
    }

    public IOrder order() {
        return order;
    }

    public double partialCloseAmount() {
        return partialCloseAmount;
    }

    public Optional<Double> maybePrice() {
        return maybePrice;
    }

    public double slippage() {
        return slippage;
    }

    public static Builder withOrder(final IOrder order) {
        checkNotNull(order);

        return new Builder(order);
    }

    public static class Builder extends TaskParamsBase.Builder<Builder> {

        private final IOrder order;
        private double partialCloseAmount;
        private Optional<Double> maybePrice = Optional.empty();
        private double slippage;

        public Builder(final IOrder order) {
            this.order = order;
        }

        public Builder closePartial(final double partialCloseAmount) {
            this.partialCloseAmount = partialCloseAmount;
            return this;
        }

        public Builder atPrice(final double price,
                               final double slippage) {
            maybePrice = Optional.of(price);
            this.slippage = slippage;
            return this;
        }

        public Builder doOnClose(final Consumer<OrderEvent> closeConsumer) {
            setEventConsumer(OrderEventType.CLOSE_OK, closeConsumer);
            return this;
        }

        public Builder doOnPartialClose(final Consumer<OrderEvent> partialCloseConsumer) {
            setEventConsumer(OrderEventType.PARTIAL_CLOSE_OK, partialCloseConsumer);
            return this;
        }

        public Builder doOnReject(final Consumer<OrderEvent> rejectConsumer) {
            setEventConsumer(OrderEventType.CLOSE_REJECTED, rejectConsumer);
            return this;
        }

        public CloseParams build() {
            return new CloseParams(this);
        }
    }
}
