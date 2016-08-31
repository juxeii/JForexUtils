package com.jforex.programming.order.process;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.function.Consumer;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.event.OrderEventType;

public class SetSLProcess extends OrderProcess {

    private final IOrder order;
    private final double newSL;

    public interface SetSLOption extends CommonOption<SetSLOption> {
        public SetSLOption onReject(Consumer<IOrder> rejectAction);

        public SetSLOption onOK(Consumer<IOrder> okAction);

        public SetSLProcess build();
    }

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

    public static final SetSLOption forParams(final IOrder order,
                                              final double newSL) {
        return new Builder(checkNotNull(order), checkNotNull(newSL));
    }

    private static class Builder extends CommonProcess<Builder> implements SetSLOption {

        private final IOrder order;
        private final double newSL;

        private Builder(final IOrder order,
                        final double newSL) {
            this.order = order;
            this.newSL = newSL;
        }

        @Override
        public SetSLOption onReject(final Consumer<IOrder> rejectAction) {
            eventHandlerForType.put(OrderEventType.CHANGE_SL_REJECTED, checkNotNull(rejectAction));
            return this;
        }

        @Override
        public SetSLOption onOK(final Consumer<IOrder> okAction) {
            eventHandlerForType.put(OrderEventType.CHANGED_SL, checkNotNull(okAction));
            return this;
        }

        @Override
        public SetSLProcess build() {
            return new SetSLProcess(this);
        }
    }
}
