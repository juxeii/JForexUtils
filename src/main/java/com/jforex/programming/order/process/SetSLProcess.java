package com.jforex.programming.order.process;

import static com.google.common.base.Preconditions.checkNotNull;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.process.option.SLOption;

public class SetSLProcess extends CommonProcess {

    private final IOrder order;
    private final double newSL;

    public interface Option extends SLOption<Option> {

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

    public static final Option forParams(final IOrder order,
                                         final double newSL) {
        return new Builder(checkNotNull(order), checkNotNull(newSL));
    }

    private static class Builder extends CommonBuilder<Option>
                                 implements Option {

        private final IOrder order;
        private final double newSL;

        private Builder(final IOrder order,
                        final double newSL) {
            this.order = order;
            this.newSL = newSL;
        }

        @Override
        public SetSLProcess build() {
            return new SetSLProcess(this);
        }
    }
}
