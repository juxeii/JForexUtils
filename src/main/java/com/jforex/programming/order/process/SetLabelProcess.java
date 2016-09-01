package com.jforex.programming.order.process;

import static com.google.common.base.Preconditions.checkNotNull;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.process.option.LabelOption;

public class SetLabelProcess extends CommonProcess {

    private final IOrder order;
    private final String newLabel;

    private SetLabelProcess(final Builder builder) {
        super(builder);
        order = builder.order;
        newLabel = builder.newLabel;
    }

    public final IOrder order() {
        return order;
    }

    public final String newLabel() {
        return newLabel;
    }

    public static final LabelOption forParams(final IOrder order,
                                              final String newLabel) {
        return new Builder(checkNotNull(order), checkNotNull(newLabel));
    }

    private static class Builder extends CommonBuilder
                                 implements LabelOption {

        private final IOrder order;
        private final String newLabel;

        private Builder(final IOrder order,
                        final String newLabel) {
            this.order = order;
            this.newLabel = newLabel;
        }

        @SuppressWarnings("unchecked")
        @Override
        public SetLabelProcess build() {
            return new SetLabelProcess(this);
        }
    }
}
