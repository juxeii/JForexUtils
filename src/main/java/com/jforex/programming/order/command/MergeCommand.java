package com.jforex.programming.order.command;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Set;
import java.util.function.Function;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.process.option.MergeOption;

import rx.Completable;

public class MergeCommand extends CommonCommand {

    private final String mergeOrderLabel;
    private final Set<IOrder> toMergeOrders;

    public interface Option extends MergeOption<Option> {

        public MergeCommand build();
    }

    private MergeCommand(final Builder builder) {
        super(builder);
        mergeOrderLabel = builder.mergeOrderLabel;
        toMergeOrders = builder.toMergeOrders;
    }

    public String mergeOrderLabel() {
        return mergeOrderLabel;
    }

    public Set<IOrder> toMergeOrders() {
        return toMergeOrders;
    }

    public static final Option create(final String mergeOrderLabel,
                                      final Set<IOrder> toMergeOrders,
                                      final Function<MergeCommand, Completable> startFunction) {
        return new Builder(checkNotNull(mergeOrderLabel),
                           checkNotNull(toMergeOrders),
                           startFunction);
    }

    private static class Builder extends CommonBuilder<Option>
            implements Option {

        private final String mergeOrderLabel;
        private final Set<IOrder> toMergeOrders;

        private Builder(final String mergeOrderLabel,
                        final Set<IOrder> toMergeOrders,
                        final Function<MergeCommand, Completable> startFunction) {
            this.mergeOrderLabel = mergeOrderLabel;
            this.toMergeOrders = toMergeOrders;
            this.startFunction = startFunction;
        }

        @Override
        public MergeCommand build() {
            return new MergeCommand(this);
        }
    }
}
