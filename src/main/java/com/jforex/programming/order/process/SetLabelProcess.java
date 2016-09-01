package com.jforex.programming.order.process;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.function.Consumer;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.event.OrderEventType;
import com.jforex.programming.order.process.option.LabelOption;

public class SetLabelProcess extends CommonProcess {

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

    public static final LabelOption forParams(final IOrder order,
                                              final String newLabel) {
        return new Builder(checkNotNull(order), checkNotNull(newLabel));
    }

    private static class Builder extends CommonBuilder
                                 implements LabelOption {

        private final IOrder order;
        private final String newLabel;

        private Builder(final IOrder order,
                        final String newLabel) {
            this.order = order;
            this.newLabel = newLabel;
        }

        @Override
        public LabelOption onError(final Consumer<Throwable> errorAction) {
            this.errorAction = checkNotNull(errorAction);
            return this;
        }

        @Override
        public LabelOption doRetries(final int noOfRetries,
                                     final long delayInMillis) {
            this.noOfRetries = noOfRetries;
            this.delayInMillis = delayInMillis;
            return this;
        }

        public LabelOption onLabelReject(final Consumer<IOrder> rejectAction) {
            eventHandlerForType.put(OrderEventType.CHANGE_LABEL_REJECTED, checkNotNull(rejectAction));
            return this;
        }

        public LabelOption onLabelChange(final Consumer<IOrder> doneAction) {
            eventHandlerForType.put(OrderEventType.CHANGED_LABEL, checkNotNull(doneAction));
            return this;
        }

        @Override
        public SetLabelProcess build() {
            return new SetLabelProcess(this);
        }
    }
}
