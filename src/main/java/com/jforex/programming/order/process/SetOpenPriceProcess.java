package com.jforex.programming.order.process;

import static com.google.common.base.Preconditions.checkNotNull;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.process.option.OpenPriceOption;

public class SetOpenPriceProcess extends CommonProcess {

    private final IOrder order;
    private final double newPrice;

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

    public static final OpenPriceOption forParams(final IOrder order,
                                                  final double newPrice) {
        return new Builder(checkNotNull(order), checkNotNull(newPrice));
    }

    private static class Builder extends CommonBuilder
                                 implements OpenPriceOption {

        private final IOrder order;
        private final double newPrice;

        private Builder(final IOrder order,
                        final double newPrice) {
            this.order = order;
            this.newPrice = newPrice;
        }

        @SuppressWarnings("unchecked")
        @Override
        public SetOpenPriceProcess build() {
            return new SetOpenPriceProcess(this);
        }
    }
}
