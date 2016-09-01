package com.jforex.programming.order.process;

import static com.google.common.base.Preconditions.checkNotNull;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.process.option.AmountOption;

public class SetAmountProcess extends CommonProcess {

    private final IOrder order;
    private final double newAmount;

    private SetAmountProcess(final Builder builder) {
        super(builder);
        order = builder.order;
        newAmount = builder.newAmount;
    }

    public final IOrder order() {
        return order;
    }

    public final double newAmount() {
        return newAmount;
    }

    public static final AmountOption forParams(final IOrder order,
                                               final double newAmount) {
        return new Builder(checkNotNull(order), checkNotNull(newAmount));
    }

    public static class Builder extends CommonBuilder
                                implements AmountOption {

        private final IOrder order;
        private final double newAmount;

        private Builder(final IOrder order,
                        final double newAmount) {
            this.order = order;
            this.newAmount = newAmount;
        }

        @SuppressWarnings("unchecked")
        public SetAmountProcess build() {
            return new SetAmountProcess(this);
        }
    }
}
