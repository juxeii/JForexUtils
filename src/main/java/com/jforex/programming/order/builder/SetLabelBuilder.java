package com.jforex.programming.order.builder;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Map;
import java.util.function.Consumer;

import com.dukascopy.api.IOrder;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.event.OrderEventType;

public class SetLabelBuilder extends OrderBuilder {

    private final IOrder orderToSetLabel;
    private final String newLabel;
    private final Map<OrderEventType, Consumer<IOrder>> eventHandlerForType;

    public interface SetLabelOption extends CommonOption<SetLabelOption> {
        public SetLabelOption onReject(Consumer<IOrder> closeRejectAction);

        public SetLabelOption onOK(Consumer<IOrder> closeOKAction);

        public SetLabelBuilder build();
    }

    private SetLabelBuilder(final Builder builder) {
        super(builder);
        orderToSetLabel = builder.orderToSetLabel;
        newLabel = builder.newLabel;
        eventHandlerForType = Maps.immutableEnumMap(ImmutableMap.<OrderEventType, Consumer<IOrder>> builder()
            .put(OrderEventType.CHANGED_LABEL, builder.okAction)
            .put(OrderEventType.CHANGE_LABEL_REJECTED, builder.rejectAction)
            .build());
    }

    public final IOrder orderToSetLabel() {
        return orderToSetLabel;
    }

    public final String newLabel() {
        return newLabel;
    }

    public static final SetLabelOption forParams(final IOrder orderToSetLabel,
                                                 final String newLabel) {
        return new Builder(checkNotNull(orderToSetLabel), checkNotNull(newLabel));
    }

    @Override
    protected void callEventHandler(final OrderEvent orderEvent) {
        eventHandlerForType
            .get(orderEvent.type())
            .accept(orderEvent.order());
    }

    private static class Builder extends CommonBuilder<Builder> implements SetLabelOption {

        private final IOrder orderToSetLabel;
        private final String newLabel;
        private Consumer<IOrder> rejectAction = o -> {};
        private Consumer<IOrder> okAction = o -> {};

        private Builder(final IOrder orderToSetLabel,
                        final String newLabel) {
            this.orderToSetLabel = orderToSetLabel;
            this.newLabel = newLabel;
        }

        @Override
        public SetLabelOption onReject(final Consumer<IOrder> rejectAction) {
            this.rejectAction = checkNotNull(rejectAction);
            return this;
        }

        @Override
        public SetLabelOption onOK(final Consumer<IOrder> okAction) {
            this.okAction = checkNotNull(okAction);
            return this;
        }

        @Override
        public SetLabelBuilder build() {
            return new SetLabelBuilder(this);
        }
    }
}
