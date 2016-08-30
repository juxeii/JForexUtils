package com.jforex.programming.order.builder;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.function.Consumer;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.event.OrderEventType;

public class SetLabelBuilder extends OrderBuilder {

    private final IOrder orderToSetLabel;
    private final String newLabel;

    public interface SetLabelOption extends CommonOption<SetLabelOption> {
        public SetLabelOption onReject(Consumer<IOrder> setLabelRejectAction);

        public SetLabelOption onOK(Consumer<IOrder> setLabelOKAction);

        public SetLabelBuilder build();
    }

    private SetLabelBuilder(final Builder builder) {
        super(builder);
        orderToSetLabel = builder.orderToSetLabel;
        newLabel = builder.newLabel;
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

    private static class Builder extends CommonBuilder<Builder> implements SetLabelOption {

        private final IOrder orderToSetLabel;
        private final String newLabel;

        private Builder(final IOrder orderToSetLabel,
                        final String newLabel) {
            this.orderToSetLabel = orderToSetLabel;
            this.newLabel = newLabel;
        }

        @Override
        public SetLabelOption onReject(final Consumer<IOrder> rejectAction) {
            eventHandlerForType.put(OrderEventType.CHANGE_LABEL_REJECTED, checkNotNull(rejectAction));
            return this;
        }

        @Override
        public SetLabelOption onOK(final Consumer<IOrder> okAction) {
            eventHandlerForType.put(OrderEventType.CHANGED_LABEL, checkNotNull(okAction));
            return this;
        }

        @Override
        public SetLabelBuilder build() {
            return new SetLabelBuilder(this);
        }
    }
}
