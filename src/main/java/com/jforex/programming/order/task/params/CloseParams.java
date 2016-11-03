package com.jforex.programming.order.task.params;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Optional;

import com.dukascopy.api.IOrder;
import com.jforex.programming.strategy.StrategyUtil;

public class CloseParams {

    private final IOrder order;
    private final double amount;
    private final Optional<Double> maybePrice;
    private final double slippage;

    private static final double defaultCloseSlippage = StrategyUtil.platformSettings.defaultCloseSlippage();
    private static final double noCloseSlippageValue = Double.NaN;

    public interface CloseOption {

        public CloseOption withAmount(double amount);

        public SlippageOption withPrice(double price);

        public CloseParams build();
    }

    public interface SlippageOption {

        public SlippageOption withSlippage(double slippage);

        public CloseParams build();
    }

    private CloseParams(final Builder builder) {
        order = builder.order;
        amount = builder.amount;
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

    public final IOrder order() {
        return order;
    }

    public double amount() {
        return amount;
    }

    public Optional<Double> maybePrice() {
        return maybePrice;
    }

    public double slippage() {
        return slippage;
    }

    public static CloseOption newBuilder(final IOrder order) {
        checkNotNull(order);

        return new Builder(order);
    }

    public static class Builder implements
                                CloseOption,
                                SlippageOption {

        private final IOrder order;
        private double amount;
        private Optional<Double> maybePrice = Optional.empty();
        private double slippage;

        public Builder(final IOrder order) {
            this.order = order;
        }

        @Override
        public CloseOption withAmount(final double amount) {
            this.amount = amount;
            return this;
        }

        @Override
        public SlippageOption withPrice(final double price) {
            maybePrice = Optional.of(price);
            return this;
        }

        @Override
        public SlippageOption withSlippage(final double slippage) {
            this.slippage = slippage;
            return this;
        }

        @Override
        public CloseParams build() {
            return new CloseParams(this);
        }
    }
}
