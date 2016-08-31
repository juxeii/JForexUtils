package com.jforex.programming.order.process;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.function.Consumer;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.event.OrderEventType;

public class SetAmountProcess extends OrderProcess {

    private final IOrder order;
    private final double newAmount;

    private SetAmountProcess(final Builder builder) {
        super(builder);
        order = builder.order;
        newAmount = builder.newAmount;
    }

    public final IOrder order() {
        return order;
    }

    public final double newAmount() {
        return newAmount;
    }

    public static final ChangeOption<SetAmountProcess> forParams(final IOrder order,
                                                                 final double newAmount) {
        return new Builder(checkNotNull(order), checkNotNull(newAmount));
    }

    private static class Builder extends CommonBuilder<Builder> implements ChangeOption<SetAmountProcess> {

        private final IOrder order;
        private final double newAmount;

        private Builder(final IOrder order,
                        final double newAmount) {
            this.order = order;
            this.newAmount = newAmount;
        }

        @Override
        public ChangeOption<SetAmountProcess> onReject(final Consumer<IOrder> rejectAction) {
            eventHandlerForType.put(OrderEventType.CHANGE_AMOUNT_REJECTED, checkNotNull(rejectAction));
            return this;
        }

        @Override
        public ChangeOption<SetAmountProcess> onDone(final Consumer<IOrder> okAction) {
            eventHandlerForType.put(OrderEventType.CHANGED_AMOUNT, checkNotNull(okAction));
            return this;
        }

        @Override
        public SetAmountProcess build() {
            return new SetAmountProcess(this);
        }
    }
}
