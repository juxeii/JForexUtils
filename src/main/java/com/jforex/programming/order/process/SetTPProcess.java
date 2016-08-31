package com.jforex.programming.order.process;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.function.Consumer;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.event.OrderEventType;

public class SetTPProcess extends OrderProcess {

    private final IOrder order;
    private final double newTP;

    private SetTPProcess(final Builder builder) {
        super(builder);
        order = builder.order;
        newTP = builder.newTP;
    }

    public final IOrder order() {
        return order;
    }

    public final double newTP() {
        return newTP;
    }

    public static final ChangeOption<SetTPProcess> forParams(final IOrder order,
                                                             final double newTP) {
        return new Builder(checkNotNull(order), checkNotNull(newTP));
    }

    private static class Builder extends CommonBuilder<Builder> implements ChangeOption<SetTPProcess> {

        private final IOrder order;
        private final double newTP;

        private Builder(final IOrder order,
                        final double newTP) {
            this.order = order;
            this.newTP = newTP;
        }

        @Override
        public ChangeOption<SetTPProcess> onReject(final Consumer<IOrder> rejectAction) {
            eventHandlerForType.put(OrderEventType.CHANGE_TP_REJECTED, checkNotNull(rejectAction));
            return this;
        }

        @Override
        public ChangeOption<SetTPProcess> onDone(final Consumer<IOrder> okAction) {
            eventHandlerForType.put(OrderEventType.CHANGED_TP, checkNotNull(okAction));
            return this;
        }

        @Override
        public SetTPProcess build() {
            return new SetTPProcess(this);
        }
    }
}
