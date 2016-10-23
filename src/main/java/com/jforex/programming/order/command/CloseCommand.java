package com.jforex.programming.order.command;

import static com.google.common.base.Preconditions.checkNotNull;

import com.dukascopy.api.IOrder;

public class CloseCommand {

    private final IOrder order;
    private final double amount;
    private final double price;
    private final double slippage;

    public interface CloseOption {

        public CloseOption withAmount(double amount);

        public CloseOption withPrice(double price);

        public CloseOption withSlippage(double slippage);

        public CloseCommand build();
    }

    private CloseCommand(final Builder builder) {
        order = builder.order;
        amount = builder.amount;
        price = builder.price;
        slippage = builder.slippage;
    }

    public final IOrder order() {
        return order;
    }

    public double amount() {
        return amount;
    }

    public double price() {
        return price;
    }

    public double slippage() {
        return slippage;
    }

    public static CloseOption newBuilder(final IOrder order) {
        checkNotNull(order);

        return new Builder(order);
    }

    public static class Builder implements CloseOption {

        private final IOrder order;
        private double amount;
        private double price;
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
        public CloseOption withPrice(final double price) {
            this.price = price;
            return this;
        }

        @Override
        public CloseOption withSlippage(final double slippage) {
            this.slippage = slippage;
            return this;
        }

        @Override
        public CloseCommand build() {
            return new CloseCommand(this);
        }
    }
}
