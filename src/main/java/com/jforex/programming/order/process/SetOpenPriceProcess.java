package com.jforex.programming.order.process;

import static com.google.common.base.Preconditions.checkNotNull;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.process.option.OpenPriceOption;

public class SetOpenPriceProcess extends CommonProcess {

    private final IOrder order;
    private final double newPrice;

    public interface Option extends OpenPriceOption<Option> {

        public SetOpenPriceProcess build();
    }

    private SetOpenPriceProcess(final Builder builder) {
        super(builder);
        order = builder.order;
        newPrice = builder.newPrice;
    }

    public final IOrder order() {
        return order;
    }

    public final double newOpenPrice() {
        return newPrice;
    }

    public static final Option forParams(final IOrder order,
                                         final double newPrice) {
        return new Builder(checkNotNull(order), checkNotNull(newPrice));
    }

    private static class Builder extends CommonBuilder<Option>
                                 implements Option {

        private final IOrder order;
        private final double newPrice;

        private Builder(final IOrder order,
                        final double newPrice) {
            this.order = order;
            this.newPrice = newPrice;
        }

        @Override
        public SetOpenPriceProcess build() {
            return new SetOpenPriceProcess(this);
        }
    }
}
