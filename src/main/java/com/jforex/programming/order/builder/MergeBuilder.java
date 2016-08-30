package com.jforex.programming.order.builder;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Collection;
import java.util.function.Consumer;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.event.OrderEventType;

public class MergeBuilder extends OrderBuilder {

    private final String mergeOrderLabel;
    private final Collection<IOrder> toMergeOrders;

    public interface MergeOption extends CommonOption<MergeOption> {
        public MergeOption onMergeReject(Consumer<IOrder> mergeRejectAction);

        public MergeOption onMergeOK(Consumer<IOrder> mergeOKAction);

        public MergeOption onMergeCloseOK(Consumer<IOrder> mergeCloseOKAction);

        public MergeBuilder build();
    }

    private MergeBuilder(final Builder builder) {
        super(builder);
        mergeOrderLabel = builder.mergeOrderLabel;
        toMergeOrders = builder.toMergeOrders;
    }

    public final String mergeOrderLabel() {
        return mergeOrderLabel;
    }

    public final Collection<IOrder> toMergeOrders() {
        return toMergeOrders;
    }

    public static final MergeOption forParams(final String mergeOrderLabel,
                                              final Collection<IOrder> toMergeOrders) {
        return new Builder(checkNotNull(mergeOrderLabel), checkNotNull(toMergeOrders));
    }

    private static class Builder extends CommonBuilder<Builder> implements MergeOption {

        private final String mergeOrderLabel;
        private final Collection<IOrder> toMergeOrders;

        private Builder(final String mergeOrderLabel,
                        final Collection<IOrder> toMergeOrders) {
            this.mergeOrderLabel = mergeOrderLabel;
            this.toMergeOrders = toMergeOrders;
        }

        @Override
        public MergeOption onMergeReject(final Consumer<IOrder> mergeRejectAction) {
            eventHandlerForType.put(OrderEventType.MERGE_REJECTED, checkNotNull(mergeRejectAction));
            return this;
        }

        @Override
        public MergeOption onMergeOK(final Consumer<IOrder> mergeOKAction) {
            eventHandlerForType.put(OrderEventType.MERGE_OK, checkNotNull(mergeOKAction));
            return this;
        }

        @Override
        public MergeOption onMergeCloseOK(final Consumer<IOrder> mergeCloseOKAction) {
            eventHandlerForType.put(OrderEventType.MERGE_CLOSE_OK, checkNotNull(mergeCloseOKAction));
            return this;
        }

        @Override
        public MergeBuilder build() {
            return new MergeBuilder(this);
        }
    }
}
