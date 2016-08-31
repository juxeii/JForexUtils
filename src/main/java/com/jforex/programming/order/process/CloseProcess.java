package com.jforex.programming.order.process;

import static com.google.common.base.Preconditions.checkNotNull;

import com.dukascopy.api.IOrder;

public class CloseProcess extends OrderProcess {

    private final IOrder orderToClose;

    private CloseProcess(final Builder builder) {
        super(builder);
        orderToClose = builder.orderToClose;
    }

    public final IOrder orderToClose() {
        return orderToClose;
    }

    public static final CloseOption forOrder(final IOrder orderToClose) {
        return new Builder(checkNotNull(orderToClose));
    }

    public static class Builder extends CommonBuilder
            implements CloseOption {

        private final IOrder orderToClose;

        private Builder(final IOrder orderToClose) {
            this.orderToClose = orderToClose;
        }

        @SuppressWarnings("unchecked")
        @Override
        public CloseProcess build() {
            return new CloseProcess(this);
        }
    }
}
