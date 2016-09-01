package com.jforex.programming.order.process;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.function.Consumer;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.event.OrderEventType;
import com.jforex.programming.order.process.option.GTTOption;

public class SetGTTProcess extends CommonProcess {

    private final IOrder order;
    private final long newGTT;

    private SetGTTProcess(final Builder builder) {
        super(builder);
        order = builder.order;
        newGTT = builder.newGTT;
    }

    public final IOrder order() {
        return order;
    }

    public final long newGTT() {
        return newGTT;
    }

    public static final GTTOption forParams(final IOrder order,
                                            final long newGTT) {
        return new Builder(checkNotNull(order), checkNotNull(newGTT));
    }

    private static class Builder extends CommonBuilder<GTTOption>
                                 implements GTTOption {

        private final IOrder order;
        private final long newGTT;

        private Builder(final IOrder order,
                        final long newGTT) {
            this.order = order;
            this.newGTT = newGTT;
        }

        public GTTOption onGTTReject(final Consumer<IOrder> rejectAction) {
            eventHandlerForType.put(OrderEventType.CHANGE_GTT_REJECTED, checkNotNull(rejectAction));
            return this;
        }

        public GTTOption onGTTChange(final Consumer<IOrder> doneAction) {
            eventHandlerForType.put(OrderEventType.CHANGED_GTT, checkNotNull(doneAction));
            return this;
        }

        @Override
        public SetGTTProcess build() {
            return new SetGTTProcess(this);
        }
    }
}
