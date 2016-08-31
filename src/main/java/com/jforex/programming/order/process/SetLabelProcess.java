package com.jforex.programming.order.process;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.function.Consumer;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.event.OrderEventType;

public class SetLabelProcess extends OrderProcess {

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

    public static final ChangeOption<SetLabelProcess> forParams(final IOrder order,
                                                                final String newLabel) {
        return new Builder(checkNotNull(order), checkNotNull(newLabel));
    }

    private static class Builder extends CommonBuilder<Builder> implements ChangeOption<SetLabelProcess> {

        private final IOrder order;
        private final String newLabel;

        private Builder(final IOrder order,
                        final String newLabel) {
            this.order = order;
            this.newLabel = newLabel;
        }

        @Override
        public ChangeOption<SetLabelProcess> onReject(final Consumer<IOrder> rejectAction) {
            eventHandlerForType.put(OrderEventType.CHANGE_LABEL_REJECTED, checkNotNull(rejectAction));
            return this;
        }

        @Override
        public ChangeOption<SetLabelProcess> onDone(final Consumer<IOrder> okAction) {
            eventHandlerForType.put(OrderEventType.CHANGED_LABEL, checkNotNull(okAction));
            return this;
        }

        @Override
        public SetLabelProcess build() {
            return new SetLabelProcess(this);
        }
    }
}
