package com.jforex.programming.order.process;

import static com.google.common.base.Preconditions.checkNotNull;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.process.option.TPOption;

public class SetTPProcess extends CommonProcess {

    private final IOrder order;
    private final double newTP;

    private SetTPProcess(final Builder builder) {
        super(builder);
        order = builder.order;
        newTP = builder.newTP;
    }

    public final IOrder order() {
        return order;
    }

    public final double newTP() {
        return newTP;
    }

    public static final TPOption forParams(final IOrder order,
                                           final double newTP) {
        return new Builder(checkNotNull(order), checkNotNull(newTP));
    }

    private static class Builder extends CommonBuilder
                                 implements TPOption {

        private final IOrder order;
        private final double newTP;

        private Builder(final IOrder order,
                        final double newTP) {
            this.order = order;
            this.newTP = newTP;
        }

        @SuppressWarnings("unchecked")
        @Override
        public SetTPProcess build() {
            return new SetTPProcess(this);
        }
    }
}
