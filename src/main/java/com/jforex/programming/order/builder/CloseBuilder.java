package com.jforex.programming.order.builder;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Map;
import java.util.function.Consumer;

import com.dukascopy.api.IOrder;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.event.OrderEventType;

public class CloseBuilder extends OrderBuilder {

    private final IOrder orderToClose;
    private final Map<OrderEventType, Consumer<IOrder>> eventHandlerForType;

    public interface CloseOption extends CommonOption<CloseOption> {
        public CloseOption onCloseReject(Consumer<IOrder> closeRejectAction);

        public CloseOption onCloseOK(Consumer<IOrder> closeOKAction);

        public CloseOption onPartialCloseOK(Consumer<IOrder> partialCloseAction);

        public CloseBuilder build();
    }

    private CloseBuilder(final Builder builder) {
        super(builder);
        orderToClose = builder.orderToClose;
        eventHandlerForType = Maps.immutableEnumMap(ImmutableMap.<OrderEventType, Consumer<IOrder>> builder()
            .put(OrderEventType.CLOSE_OK, builder.closeOKAction)
            .put(OrderEventType.PARTIAL_CLOSE_OK, builder.partialCloseAction)
            .put(OrderEventType.CLOSE_REJECTED, builder.closeRejectAction)
            .build());
    }

    public final IOrder orderToClose() {
        return orderToClose;
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

    @Override
    protected void callEventHandler(final OrderEvent orderEvent) {
        eventHandlerForType
            .get(orderEvent.type())
            .accept(orderEvent.order());
    }
}
