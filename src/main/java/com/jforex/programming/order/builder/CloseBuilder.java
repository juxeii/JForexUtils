package com.jforex.programming.order.builder;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.function.Consumer;

import com.dukascopy.api.IOrder;

public class CloseBuilder {

    private final IOrder orderToClose;
    private final Consumer<Throwable> errorAction;
    private final Consumer<IOrder> closeRejectAction;
    private final Consumer<IOrder> closeOKAction;
    private final Consumer<IOrder> partialCloseAction;
    private final int noOfRetries;
    private final long delayInMillis;

    public final IOrder orderToClose() {
        return orderToClose;
    }

    public final Consumer<Throwable> errorAction() {
        return errorAction;
    }

    public final Consumer<IOrder> closeRejectAction() {
        return closeRejectAction;
    }

    public final Consumer<IOrder> closeOKAction() {
        return closeOKAction;
    }

    public final Consumer<IOrder> partialCloseAction() {
        return partialCloseAction;
    }

    public final int noOfRetries() {
        return noOfRetries;
    }

    public final long delayInMillis() {
        return delayInMillis;
    }

    public interface CloseOption extends CommonOption<CloseOption> {
        public CloseOption onCloseReject(Consumer<IOrder> closeRejectAction);

        public CloseOption onCloseOK(Consumer<IOrder> closeOKAction);

        public CloseOption onPartialCloseOK(Consumer<IOrder> partialCloseAction);

        public CloseBuilder build();
    }

    private CloseBuilder(final Builder builder) {
        orderToClose = builder.orderToClose;
        errorAction = builder.errorAction;
        closeRejectAction = builder.closeRejectAction;
        closeOKAction = builder.closeOKAction;
        partialCloseAction = builder.partialCloseAction;
        noOfRetries = builder.noOfRetries;
        delayInMillis = builder.delayInMillis;
    }

    public static final CloseOption forOrder(final IOrder orderToClose) {
        return new Builder(checkNotNull(orderToClose));
    }

    private static class Builder extends CommonBuilder<Builder> implements CloseOption {

        private final IOrder orderToClose;
        private Consumer<IOrder> closeRejectAction = o -> {};
        private Consumer<IOrder> closeOKAction = o -> {};
        private Consumer<IOrder> partialCloseAction = o -> {};

        private Builder(final IOrder orderToClose) {
            this.orderToClose = orderToClose;
        }

        @Override
        public CloseOption onCloseReject(final Consumer<IOrder> closeRejectAction) {
            this.closeRejectAction = checkNotNull(closeRejectAction);
            return this;
        }

        @Override
        public CloseOption onCloseOK(final Consumer<IOrder> closeOKAction) {
            this.closeOKAction = checkNotNull(closeOKAction);
            return this;
        }

        @Override
        public CloseOption onPartialCloseOK(final Consumer<IOrder> partialCloseAction) {
            this.partialCloseAction = checkNotNull(partialCloseAction);
            return this;
        }

        @Override
        public CloseBuilder build() {
            return new CloseBuilder(this);
        }
    }
}
