package com.jforex.programming.order.process;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.function.Consumer;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.event.OrderEventType;
import com.jforex.programming.order.process.option.TPOption;

public class SetTPProcess extends CommonProcess {

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

    public static final TPOption forParams(final IOrder order,
                                           final double newTP) {
        return new Builder(checkNotNull(order), checkNotNull(newTP));
    }

    private static class Builder extends CommonBuilder<TPOption>
                                 implements TPOption {

        private final IOrder order;
        private final double newTP;

        private Builder(final IOrder order,
                        final double newTP) {
            this.order = order;
            this.newTP = newTP;
        }

        public TPOption onTPReject(final Consumer<IOrder> rejectAction) {
            eventHandlerForType.put(OrderEventType.CHANGE_TP_REJECTED, checkNotNull(rejectAction));
            return this;
        }

        public TPOption onTPChange(final Consumer<IOrder> doneAction) {
            eventHandlerForType.put(OrderEventType.CHANGED_TP, checkNotNull(doneAction));
            return this;
        }

        @Override
        public SetTPProcess build() {
            return new SetTPProcess(this);
        }
    }
}
