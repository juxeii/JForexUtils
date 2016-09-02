package com.jforex.programming.order.command;

import static com.google.common.base.Preconditions.checkNotNull;

import com.jforex.programming.order.OrderParams;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.process.option.MergeOption;
import com.jforex.programming.order.process.option.SubmitOption;

import rx.Observable;

public class SubmitAndMergeCommand extends CommonCommand {

    private final OrderParams orderParams;
    private final String mergeOrderLabel;

    public interface Option extends MergeOption<Option>, SubmitOption<Option> {

        public SubmitAndMergeCommand build();
    }

    private SubmitAndMergeCommand(final Builder builder) {
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

    public static final Option create(final OrderParams orderParams,
                                      final String mergeOrderLabel,
                                      final Observable<OrderEvent> observable) {
        return new Builder(checkNotNull(orderParams),
                           checkNotNull(mergeOrderLabel),
                           observable);
    }

    private static class Builder extends CommonBuilder<Option>
                                 implements Option {

        private final OrderParams orderParams;
        private final String mergeOrderLabel;

        private Builder(final OrderParams orderParams,
                        final String mergeOrderLabel,
                        final Observable<OrderEvent> observable) {
            this.orderParams = orderParams;
            this.mergeOrderLabel = mergeOrderLabel;
            this.observable = observable;
        }

        @Override
        public SubmitAndMergeCommand build() {
            return new SubmitAndMergeCommand(this);
        }
    }
}
