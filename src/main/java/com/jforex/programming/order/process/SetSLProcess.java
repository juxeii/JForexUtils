package com.jforex.programming.order.process;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.function.Consumer;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.event.OrderEventType;
import com.jforex.programming.order.process.option.SLOption;

public class SetSLProcess extends CommonProcess {

    private final IOrder order;
    private final double newSL;

    private SetSLProcess(final Builder builder) {
        super(builder);
        order = builder.order;
        newSL = builder.newSL;
    }

    public final IOrder order() {
        return order;
    }

    public final double newSL() {
        return newSL;
    }

    public static final SLOption forParams(final IOrder order,
                                           final double newSL) {
        return new Builder(checkNotNull(order), checkNotNull(newSL));
    }

    private static class Builder extends CommonBuilder<SLOption>
                                 implements SLOption {

        private final IOrder order;
        private final double newSL;

        private Builder(final IOrder order,
                        final double newSL) {
            this.order = order;
            this.newSL = newSL;
        }

        public SLOption onSLReject(final Consumer<IOrder> rejectAction) {
            eventHandlerForType.put(OrderEventType.CHANGE_SL_REJECTED, checkNotNull(rejectAction));
            return this;
        }

        public SLOption onSLChange(final Consumer<IOrder> doneAction) {
            eventHandlerForType.put(OrderEventType.CHANGED_SL, checkNotNull(doneAction));
            return this;
        }

        @Override
        public SetSLProcess build() {
            return new SetSLProcess(this);
        }
    }
}
