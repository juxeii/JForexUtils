package com.jforex.programming.order.command;

public class CloseCommand {

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
        amount = builder.amount;
        price = builder.price;
        slippage = builder.slippage;
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

    public static CloseOption newBuilder() {
        return new Builder();
    }

    public static class Builder implements CloseOption {

        private double amount;
        private double price;
        private double slippage;

        public Builder() {
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
