package com.jforex.programming.order.builder;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Collection;
import java.util.function.Consumer;

import com.dukascopy.api.IOrder;

public class MergeBuilder {

    private final String mergeOrderLabel;
    private final Collection<IOrder> toMergeOrders;
    private final Consumer<Throwable> errorAction;
    private final Consumer<IOrder> mergeRejectAction;
    private final Consumer<IOrder> mergeOKAction;
    private final Consumer<IOrder> mergeCloseOKAction;

    public final String mergeOrderLabel() {
        return mergeOrderLabel;
    }

    public final Collection<IOrder> toMergeOrders() {
        return toMergeOrders;
    }

    public final Consumer<Throwable> errorAction() {
        return errorAction;
    }

    public final Consumer<IOrder> mergeRejectAction() {
        return mergeRejectAction;
    }

    public final Consumer<IOrder> mergeOKAction() {
        return mergeOKAction;
    }

    public final Consumer<IOrder> mergeCloseOKAction() {
        return mergeCloseOKAction;
    }

    public interface MergeOption extends CommonOption<MergeOption> {
        public MergeOption onMergeReject(Consumer<IOrder> mergeRejectAction);

        public MergeOption onMergeOK(Consumer<IOrder> mergeOKAction);

        public MergeOption onMergeCloseOK(Consumer<IOrder> mergeCloseOKAction);

        public MergeBuilder build();
    }

    private MergeBuilder(final Builder builder) {
        mergeOrderLabel = builder.mergeOrderLabel;
        toMergeOrders = builder.toMergeOrders;
        errorAction = builder.errorAction;
        mergeRejectAction = builder.mergeRejectAction;
        mergeOKAction = builder.mergeOKAction;
        mergeCloseOKAction = builder.mergeCloseOKAction;
    }

    public static final MergeOption forParams(final String mergeOrderLabel,
                                              final Collection<IOrder> toMergeOrders) {
        return new Builder(checkNotNull(mergeOrderLabel), checkNotNull(toMergeOrders));
    }

    private static class Builder extends CommonBuilder<Builder> implements MergeOption {

        private final String mergeOrderLabel;
        private final Collection<IOrder> toMergeOrders;
        private Consumer<IOrder> mergeRejectAction = o -> {};
        private Consumer<IOrder> mergeOKAction = o -> {};
        private Consumer<IOrder> mergeCloseOKAction = o -> {};

        private Builder(final String mergeOrderLabel,
                        final Collection<IOrder> toMergeOrders) {
            this.mergeOrderLabel = mergeOrderLabel;
            this.toMergeOrders = toMergeOrders;
        }

        @Override
        public MergeOption onMergeReject(final Consumer<IOrder> mergeRejectAction) {
            this.mergeRejectAction = checkNotNull(mergeRejectAction);
            return this;
        }

        @Override
        public MergeOption onMergeOK(final Consumer<IOrder> mergeOKAction) {
            this.mergeOKAction = checkNotNull(mergeOKAction);
            return this;
        }

        @Override
        public MergeOption onMergeCloseOK(final Consumer<IOrder> mergeCloseOKAction) {
            this.mergeCloseOKAction = checkNotNull(mergeCloseOKAction);
            return this;
        }

        @Override
        public MergeBuilder build() {
            return new MergeBuilder(this);
        }
    }
}
