package com.jforex.programming.order.task.params.basic;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Optional;
import java.util.function.Consumer;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.event.OrderEventType;
import com.jforex.programming.strategy.StrategyUtil;

public class CloseParams extends BasicParamsBase {

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

    public static Builder closeOrder(final IOrder order) {
        checkNotNull(order);

        return new Builder(order);
    }

    public static class Builder extends BasicParamsBuilder<Builder> {

        private final IOrder order;
        private double partialCloseAmount = 0.0;
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
            return setEventConsumer(OrderEventType.CLOSE_OK, closeConsumer);
        }

        public Builder doOnPartialClose(final Consumer<OrderEvent> partialCloseConsumer) {
            return setEventConsumer(OrderEventType.PARTIAL_CLOSE_OK, partialCloseConsumer);
        }

        public Builder doOnReject(final Consumer<OrderEvent> rejectConsumer) {
            return setEventConsumer(OrderEventType.CLOSE_REJECTED, rejectConsumer);
        }

        public CloseParams build() {
            return new CloseParams(this);
        }
    }
}
