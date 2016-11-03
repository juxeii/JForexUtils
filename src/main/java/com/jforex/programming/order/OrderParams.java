package com.jforex.programming.order;

import static com.google.common.base.Preconditions.checkNotNull;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.dukascopy.api.IEngine.OrderCommand;
import com.dukascopy.api.Instrument;
import com.jforex.programming.settings.PlatformSettings;
import com.jforex.programming.settings.UserSettings;
import com.jforex.programming.strategy.StrategyUtil;

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
        checkNotNull(instrument);

        return new Builder(instrument);
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

        private static final PlatformSettings platformSettings = StrategyUtil.platformSettings;
        private static final UserSettings userSettings = StrategyUtil.userSettings;

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
            checkNotNull(label);

            this.label = label;
            return this;
        }

        @Override
        public final Builder forInstrument(final Instrument instrument) {
            checkNotNull(instrument);

            this.instrument = instrument;
            return this;
        }

        @Override
        public final Builder withOrderCommand(final OrderCommand orderCommand) {
            checkNotNull(orderCommand);

            this.orderCommand = orderCommand;
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
            checkNotNull(comment);

            this.comment = comment;
            return this;
        }

        @Override
        public final OrderParams build() {
            return new OrderParams(this);
        }
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (!(obj instanceof OrderParams))
            return false;

        final OrderParams other = (OrderParams) obj;
        final EqualsBuilder builder = new EqualsBuilder();
        builder.append(amount, other.amount);
        builder.append(comment, other.comment);
        builder.append(goodTillTime, other.goodTillTime);
        builder.append(instrument, other.instrument);
        builder.append(label, other.label);
        builder.append(orderCommand, other.orderCommand);
        builder.append(price, other.price);
        builder.append(slippage, other.slippage);
        builder.append(stopLossPrice, other.stopLossPrice);
        builder.append(takeProfitPrice, other.takeProfitPrice);

        return builder.isEquals();
    }

    @Override
    public int hashCode() {
        final HashCodeBuilder builder = new HashCodeBuilder();
        builder.append(instrument);
        builder.append(label);
        builder.append(orderCommand);
        builder.append(amount);
        builder.append(goodTillTime);
        builder.append(price);
        builder.append(slippage);
        builder.append(stopLossPrice);
        builder.append(takeProfitPrice);
        builder.append(comment);

        return builder.toHashCode();
    }

    @Override
    public String toString() {
        final ToStringBuilder toStringBuilder = new ToStringBuilder(this, ToStringStyle.DEFAULT_STYLE);
        toStringBuilder.append("instrument", instrument);
        toStringBuilder.append("label", label);
        toStringBuilder.append("orderCommand", orderCommand);
        toStringBuilder.append("amount", amount);
        toStringBuilder.append("goodTillTime", goodTillTime);
        toStringBuilder.append("price", price);
        toStringBuilder.append("amount", amount);
        toStringBuilder.append("slippage", slippage);
        toStringBuilder.append("stopLossPrice", stopLossPrice);
        toStringBuilder.append("takeProfitPrice", takeProfitPrice);

        return toStringBuilder.toString();
    }
}
