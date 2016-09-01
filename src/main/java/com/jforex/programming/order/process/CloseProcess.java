package com.jforex.programming.order.process;

import static com.google.common.base.Preconditions.checkNotNull;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.process.option.CloseOption;

public class CloseProcess extends CommonProcess {

    private final IOrder orderToClose;

    public interface Option extends CloseOption<Option> {

        public CloseProcess build();
    }

    private CloseProcess(final Builder builder) {
        super(builder);
        orderToClose = builder.orderToClose;
    }

    public final IOrder orderToClose() {
        return orderToClose;
    }

    public static final Option forOrder(final IOrder orderToClose) {
        return new Builder(checkNotNull(orderToClose));
    }

    public static class Builder extends CommonBuilder<Option>
                                implements Option {

        private final IOrder orderToClose;

        private Builder(final IOrder orderToClose) {
            this.orderToClose = orderToClose;
        }

        @Override
        public CloseProcess build() {
            return new CloseProcess(this);
        }
    }
}
