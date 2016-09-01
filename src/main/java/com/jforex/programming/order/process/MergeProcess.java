package com.jforex.programming.order.process;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Collection;
import java.util.function.Consumer;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.event.OrderEventType;
import com.jforex.programming.order.process.option.MergeOption;

public class MergeProcess extends CommonProcess {

    private final String mergeOrderLabel;
    private final Collection<IOrder> toMergeOrders;

    private MergeProcess(final Builder builder) {
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

    private static class Builder extends CommonBuilder
                                 implements MergeOption {

        private final String mergeOrderLabel;
        private final Collection<IOrder> toMergeOrders;

        private Builder(final String mergeOrderLabel,
                        final Collection<IOrder> toMergeOrders) {
            this.mergeOrderLabel = mergeOrderLabel;
            this.toMergeOrders = toMergeOrders;
        }

        @Override
        public MergeOption onError(final Consumer<Throwable> errorAction) {
            this.errorAction = checkNotNull(errorAction);
            return this;
        }

        @Override
        public MergeOption doRetries(final int noOfRetries,
                                     final long delayInMillis) {
            this.noOfRetries = noOfRetries;
            this.delayInMillis = delayInMillis;
            return this;
        }

        public MergeOption onRemoveSLReject(final Consumer<IOrder> removeSLRejectAction) {
            eventHandlerForType.put(OrderEventType.CHANGE_SL_REJECTED, checkNotNull(removeSLRejectAction));
            return this;
        }

        public MergeOption onRemoveTPReject(final Consumer<IOrder> removeTPRejectAction) {
            eventHandlerForType.put(OrderEventType.CHANGE_TP_REJECTED, checkNotNull(removeTPRejectAction));
            return this;
        }

        public MergeOption onRemoveSL(final Consumer<IOrder> removedSLAction) {
            eventHandlerForType.put(OrderEventType.CHANGED_SL, checkNotNull(removedSLAction));
            return this;
        }

        public MergeOption onRemoveTP(final Consumer<IOrder> removedTPAction) {
            eventHandlerForType.put(OrderEventType.CHANGED_TP, checkNotNull(removedTPAction));
            return this;
        }

        public MergeOption onMergeReject(final Consumer<IOrder> mergeRejectAction) {
            eventHandlerForType.put(OrderEventType.MERGE_REJECTED, checkNotNull(mergeRejectAction));
            return this;
        }

        public MergeOption onMerge(final Consumer<IOrder> mergedAction) {
            eventHandlerForType.put(OrderEventType.MERGE_OK, checkNotNull(mergedAction));
            return this;
        }

        public MergeOption onMergeClose(final Consumer<IOrder> mergeClosedAction) {
            eventHandlerForType.put(OrderEventType.MERGE_CLOSE_OK, checkNotNull(mergeClosedAction));
            return this;
        }

        @Override
        public MergeProcess build() {
            return new MergeProcess(this);
        }
    }
}
