package com.jforex.programming.order.process;

import static com.google.common.base.Preconditions.checkNotNull;

import com.jforex.programming.order.OrderParams;
import com.jforex.programming.order.process.option.MergeOption;
import com.jforex.programming.order.process.option.SubmitOption;

public class SubmitAndMergePositionProcess extends CommonProcess {

    private final OrderParams orderParams;
    private final String mergeOrderLabel;

    public interface Option extends MergeOption<Option>, SubmitOption<Option> {

        public SubmitAndMergePositionProcess build();
    }

    private SubmitAndMergePositionProcess(final Builder builder) {
        super(builder);
        orderParams = builder.orderParams;
        mergeOrderLabel = builder.mergeOrderLabel;
    }

    public final OrderParams orderParams() {
        return orderParams;
    }

    public final String mergeOrderLabel() {
        return mergeOrderLabel;
    }

    public static final Option forParams(final OrderParams orderParams,
                                         final String mergeOrderLabel) {
        return new Builder(checkNotNull(orderParams), checkNotNull(mergeOrderLabel));
    }

    private static class Builder extends CommonBuilder<Option>
                                 implements Option {

        private final OrderParams orderParams;
        private final String mergeOrderLabel;

        private Builder(final OrderParams orderParams,
                        final String mergeOrderLabel) {
            this.orderParams = orderParams;
            this.mergeOrderLabel = mergeOrderLabel;
        }

        @Override
        public SubmitAndMergePositionProcess build() {
            return new SubmitAndMergePositionProcess(this);
        }
    }
}
