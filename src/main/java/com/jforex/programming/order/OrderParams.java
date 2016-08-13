package com.jforex.programming.order;

import static com.google.common.base.Preconditions.checkNotNull;

import org.aeonbits.owner.ConfigFactory;

import com.dukascopy.api.IEngine.OrderCommand;
import com.dukascopy.api.Instrument;
import com.jforex.programming.settings.PlatformSettings;
import com.jforex.programming.settings.UserSettings;

public final class OrderParams implements Cloneable {

    private final String label;
    private final Instrument instrument;
    private final OrderCommand orderCommand;
    private final double amount;
    private final double price;
    private final double slippage;
    private final double stopLossPrice;
    private final double takeProfitPrice;
    private final long goodTillTime;
    private final String comment;

    public interface WithOrderCommand {
        public WithAmount withOrderCommand(OrderCommand orderCommand);
    }

    public interface WithAmount {
        public WithLabel withAmount(double amount);
    }

    public interface WithLabel {
        public WithOptions withLabel(String label);
    }

    public interface WithOptions {
        public WithOptions price(double price);

        public WithOptions slippage(double slippage);

        public WithOptions stopLossPrice(double stopLossPrice);

        public WithOptions takeProfitPrice(double takeProfitPrice);

        public WithOptions goodTillTime(long goodTillTime);

        public WithOptions comment(String comment);

        public OrderParams build();
    }

    public interface Clone {
        public Clone withLabel(final String label);

        public Clone forInstrument(Instrument instrument);

        public Clone withOrderCommand(OrderCommand orderCommand);

        public Clone withAmount(double amount);

        public Clone price(double price);

        public Clone slippage(double slippage);

        public Clone stopLossPrice(double stopLossPrice);

        public Clone takeProfitPrice(double takeProfitPrice);

        public Clone goodTillTime(long goodTillTime);

        public Clone comment(String comment);

        public OrderParams build();
    }

    private OrderParams(final Builder builder) {
        label = builder.label;
        instrument = builder.instrument;
        orderCommand = builder.orderCommand;
        amount = builder.amount;
        price = builder.price;
        slippage = builder.slippage;
        stopLossPrice = builder.stopLossPrice;
        takeProfitPrice = builder.takeProfitPrice;
        goodTillTime = builder.goodTillTime;
        comment = builder.comment;
    }

    public final String label() {
        return label;
    }

    public final Instrument instrument() {
        return instrument;
    }

    public final OrderCommand orderCommand() {
        return orderCommand;
    }

    public final double amount() {
        return amount;
    }

    public final double price() {
        return price;
    }

    public final double slippage() {
        return slippage;
    }

    public final double stopLossPrice() {
        return stopLossPrice;
    }

    public final double takeProfitPrice() {
        return takeProfitPrice;
    }

    public final long goodTillTime() {
        return goodTillTime;
    }

    public final String comment() {
        return comment;
    }

    @Override
    public final Clone clone() {
        return new Builder(this);
    }

    public static final WithOrderCommand forInstrument(final Instrument instrument) {
        return new Builder(checkNotNull(instrument));
    }

    private static class Builder implements
            WithOrderCommand,
            WithAmount,
            WithLabel,
            WithOptions,
            Clone {

        private String label;
        private Instrument instrument;
        private OrderCommand orderCommand;
        private double amount;

        private double price;
        private double slippage;
        private double stopLossPrice;
        private double takeProfitPrice;
        private long goodTillTime;
        private String comment;

        private static final PlatformSettings platformSettings =
                ConfigFactory.create(PlatformSettings.class);
        private static final UserSettings userSettings =
                ConfigFactory.create(UserSettings.class);

        private Builder(final Instrument instrument) {
            this.instrument = instrument;
            price = userSettings.defaultOpenPrice();
            slippage = userSettings.defaultSlippage();
            stopLossPrice = platformSettings.noSLPrice();
            takeProfitPrice = platformSettings.noTPPrice();
            goodTillTime = userSettings.defaultGTT();
            comment = userSettings.defaultOrderComment();
        }

        private Builder(final OrderParams orderSpecification) {
            label = orderSpecification.label;
            instrument = orderSpecification.instrument;
            orderCommand = orderSpecification.orderCommand;
            amount = orderSpecification.amount;
            price = orderSpecification.price;
            slippage = orderSpecification.slippage;
            stopLossPrice = orderSpecification.stopLossPrice;
            takeProfitPrice = orderSpecification.takeProfitPrice;
            goodTillTime = orderSpecification.goodTillTime;
            comment = orderSpecification.comment;
        }

        @Override
        public final Builder withLabel(final String label) {
            this.label = checkNotNull(label);
            return this;
        }

        @Override
        public final Builder forInstrument(final Instrument instrument) {
            this.instrument = checkNotNull(instrument);
            return this;
        }

        @Override
        public final Builder withOrderCommand(final OrderCommand orderCommand) {
            this.orderCommand = checkNotNull(orderCommand);
            return this;
        }

        @Override
        public final Builder withAmount(final double amount) {
            this.amount = amount;
            return this;
        }

        @Override
        public final Builder price(final double price) {
            this.price = price;
            return this;
        }

        @Override
        public final Builder slippage(final double slippage) {
            this.slippage = slippage;
            return this;
        }

        @Override
        public final Builder stopLossPrice(final double stopLossPrice) {
            this.stopLossPrice = stopLossPrice;
            return this;
        }

        @Override
        public final Builder takeProfitPrice(final double takeProfitPrice) {
            this.takeProfitPrice = takeProfitPrice;
            return this;
        }

        @Override
        public final Builder goodTillTime(final long goodTillTime) {
            this.goodTillTime = goodTillTime;
            return this;
        }

        @Override
        public final Builder comment(final String comment) {
            this.comment = checkNotNull(comment);
            return this;
        }

        @Override
        public final OrderParams build() {
            return new OrderParams(this);
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        long temp;
        temp = Double.doubleToLongBits(amount);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        result = prime * result + ((comment == null) ? 0 : comment.hashCode());
        result = prime * result + (int) (goodTillTime ^ (goodTillTime >>> 32));
        result = prime * result + ((instrument == null) ? 0 : instrument.hashCode());
        result = prime * result + ((label == null) ? 0 : label.hashCode());
        result = prime * result + ((orderCommand == null) ? 0 : orderCommand.hashCode());
        temp = Double.doubleToLongBits(price);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(slippage);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(stopLossPrice);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(takeProfitPrice);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final OrderParams other = (OrderParams) obj;
        if (Double.doubleToLongBits(amount) != Double.doubleToLongBits(other.amount))
            return false;
        if (comment == null) {
            if (other.comment != null)
                return false;
        } else if (!comment.equals(other.comment))
            return false;
        if (goodTillTime != other.goodTillTime)
            return false;
        if (instrument != other.instrument)
            return false;
        if (label == null) {
            if (other.label != null)
                return false;
        } else if (!label.equals(other.label))
            return false;
        if (orderCommand != other.orderCommand)
            return false;
        if (Double.doubleToLongBits(price) != Double.doubleToLongBits(other.price))
            return false;
        if (Double.doubleToLongBits(slippage) != Double.doubleToLongBits(other.slippage))
            return false;
        if (Double.doubleToLongBits(stopLossPrice) != Double.doubleToLongBits(other.stopLossPrice))
            return false;
        if (Double.doubleToLongBits(takeProfitPrice) != Double.doubleToLongBits(other.takeProfitPrice))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "OrderParams [label=" + label + ", instrument=" + instrument + ", orderCommand=" + orderCommand
                + ", amount=" + amount + ", price=" + price + ", slippage=" + slippage + ", stopLossPrice="
                + stopLossPrice + ", takeProfitPrice=" + takeProfitPrice + ", goodTillTime=" + goodTillTime
                + ", comment=" + comment + "]";
    }
}
