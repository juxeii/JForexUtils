package com.jforex.programming.order.process;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.function.Consumer;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.event.OrderEventType;
import com.jforex.programming.order.process.option.CloseOption;

public class CloseProcess extends CommonProcess {

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

        @Override
        public CloseOption onError(final Consumer<Throwable> errorAction) {
            this.errorAction = checkNotNull(errorAction);
            return this;
        }

        @Override
        public CloseOption doRetries(final int noOfRetries,
                                     final long delayInMillis) {
            this.noOfRetries = noOfRetries;
            this.delayInMillis = delayInMillis;
            return this;
        }

        @Override
        public CloseOption onCloseReject(final Consumer<IOrder> closeRejectAction) {
            eventHandlerForType.put(OrderEventType.CLOSE_REJECTED, checkNotNull(closeRejectAction));
            return this;
        }

        @Override
        public CloseOption onClose(final Consumer<IOrder> closedAction) {
            eventHandlerForType.put(OrderEventType.CLOSE_OK, checkNotNull(closedAction));
            return this;
        }

        @Override
        public CloseOption onPartialClose(final Consumer<IOrder> partialClosedAction) {
            eventHandlerForType.put(OrderEventType.PARTIAL_CLOSE_OK, checkNotNull(partialClosedAction));
            return this;
        }

        @Override
        public CloseProcess build() {
            return new CloseProcess(this);
        }
    }
}
