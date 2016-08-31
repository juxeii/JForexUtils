package com.jforex.programming.order.builder;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.function.Consumer;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.event.OrderEventType;

public class SetLabelProcess extends OrderProcess {

    private final IOrder order;
    private final String newLabel;

    public interface SetLabelOption extends CommonOption<SetLabelOption> {
        public SetLabelOption onReject(Consumer<IOrder> setLabelRejectAction);

        public SetLabelOption onOK(Consumer<IOrder> setLabelOKAction);

        public SetLabelProcess build();
    }

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

    public static final SetLabelOption forParams(final IOrder order,
                                                 final String newLabel) {
        return new Builder(checkNotNull(order), checkNotNull(newLabel));
    }

    private static class Builder extends CommonProcess<Builder> implements SetLabelOption {

        private final IOrder order;
        private final String newLabel;

        private Builder(final IOrder order,
                        final String newLabel) {
            this.order = order;
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
        public SetLabelProcess build() {
            return new SetLabelProcess(this);
        }
    }
}
