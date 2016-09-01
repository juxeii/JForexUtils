package com.jforex.programming.order.process;

import static com.google.common.base.Preconditions.checkNotNull;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.process.option.TPOption;

public class SetTPProcess extends CommonProcess {

    private final IOrder order;
    private final double newTP;

    public interface Option extends TPOption<Option> {

        public SetTPProcess build();
    }

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

    public static final Option forParams(final IOrder order,
                                         final double newTP) {
        return new Builder(checkNotNull(order), checkNotNull(newTP));
    }

    private static class Builder extends CommonBuilder<Option>
                                 implements Option {

        private final IOrder order;
        private final double newTP;

        private Builder(final IOrder order,
                        final double newTP) {
            this.order = order;
            this.newTP = newTP;
        }

        @Override
        public SetTPProcess build() {
            return new SetTPProcess(this);
        }
    }
}
