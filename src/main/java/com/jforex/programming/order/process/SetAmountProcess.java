package com.jforex.programming.order.process;

import static com.google.common.base.Preconditions.checkNotNull;

import com.dukascopy.api.IOrder;

public class SetAmountProcess extends OrderProcess {

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

    public static final AmountOption<CommonBuilder> forParams(final IOrder order,
                                                              final double newAmount) {
        return new Builder(checkNotNull(order), checkNotNull(newAmount));
    }

    public static class Builder extends CommonBuilder implements AmountOption<CommonBuilder> {

        private final IOrder order;
        private final double newAmount;

        private Builder(final IOrder order,
                        final double newAmount) {
            this.order = order;
            this.newAmount = newAmount;
        }

        public SetAmountProcess build() {
            return new SetAmountProcess(this);
        }
    }
}
