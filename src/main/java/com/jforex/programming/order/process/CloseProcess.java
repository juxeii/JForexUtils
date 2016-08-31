package com.jforex.programming.order.process;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.function.Consumer;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.event.OrderEventType;

public class CloseProcess extends OrderProcess {

    private final IOrder orderToClose;

    public interface CloseOption extends CommonOption<CloseOption> {
        public CloseOption onCloseReject(Consumer<IOrder> closeRejectAction);

        public CloseOption onCloseOK(Consumer<IOrder> closeOKAction);

        public CloseOption onPartialCloseOK(Consumer<IOrder> partialCloseAction);

        public CloseProcess build();
    }

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

    private static class Builder extends CommonProcess<Builder> implements CloseOption {

        private final IOrder orderToClose;

        private Builder(final IOrder orderToClose) {
            this.orderToClose = orderToClose;
        }

        @Override
        public CloseOption onCloseReject(final Consumer<IOrder> closeRejectAction) {
            eventHandlerForType.put(OrderEventType.CLOSE_REJECTED, checkNotNull(closeRejectAction));
            return this;
        }

        @Override
        public CloseOption onCloseOK(final Consumer<IOrder> closeOKAction) {
            eventHandlerForType.put(OrderEventType.CLOSE_OK, checkNotNull(closeOKAction));
            return this;
        }

        @Override
        public CloseOption onPartialCloseOK(final Consumer<IOrder> partialCloseAction) {
            eventHandlerForType.put(OrderEventType.PARTIAL_CLOSE_OK, checkNotNull(partialCloseAction));
            return this;
        }

        @Override
        public CloseProcess build() {
            return new CloseProcess(this);
        }
    }
}
