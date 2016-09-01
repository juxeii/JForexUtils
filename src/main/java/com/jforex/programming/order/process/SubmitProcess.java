package com.jforex.programming.order.process;

import static com.google.common.base.Preconditions.checkNotNull;

import com.jforex.programming.order.OrderParams;
import com.jforex.programming.order.process.option.SubmitOption;

public class SubmitProcess extends CommonProcess {

    private final OrderParams orderParams;

    private SubmitProcess(final Builder builder) {
        super(builder);
        orderParams = builder.orderParams;
    }

    public final OrderParams orderParams() {
        return orderParams;
    }

    public static final SubmitOption forOrderParams(final OrderParams orderParams) {
        return new Builder(checkNotNull(orderParams));
    }

    private static class Builder extends CommonBuilder
                                 implements SubmitOption {

        private final OrderParams orderParams;

        private Builder(final OrderParams orderParams) {
            this.orderParams = orderParams;
        }

        @SuppressWarnings("unchecked")
        @Override
        public SubmitProcess build() {
            return new SubmitProcess(this);
        }
    }
}
