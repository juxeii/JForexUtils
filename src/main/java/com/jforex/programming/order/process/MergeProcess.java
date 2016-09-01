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

    public interface Option extends MergeOption<Option> {

        public MergeProcess build();
    }

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

    public static final Option forParams(final String mergeOrderLabel,
                                         final Collection<IOrder> toMergeOrders) {
        return new Builder(checkNotNull(mergeOrderLabel), checkNotNull(toMergeOrders));
    }

    private static class Builder extends CommonBuilder<Option>
                                 implements Option {

        private final String mergeOrderLabel;
        private final Collection<IOrder> toMergeOrders;

        private Builder(final String mergeOrderLabel,
                        final Collection<IOrder> toMergeOrders) {
            this.mergeOrderLabel = mergeOrderLabel;
            this.toMergeOrders = toMergeOrders;
        }

        public Option onRemoveSLReject(final Consumer<IOrder> removeSLRejectAction) {
            eventHandlerForType.put(OrderEventType.CHANGE_SL_REJECTED, checkNotNull(removeSLRejectAction));
            return this;
        }

        public Option onRemoveTPReject(final Consumer<IOrder> removeTPRejectAction) {
            eventHandlerForType.put(OrderEventType.CHANGE_TP_REJECTED, checkNotNull(removeTPRejectAction));
            return this;
        }

        public Option onRemoveSL(final Consumer<IOrder> removedSLAction) {
            eventHandlerForType.put(OrderEventType.CHANGED_SL, checkNotNull(removedSLAction));
            return this;
        }

        public Option onRemoveTP(final Consumer<IOrder> removedTPAction) {
            eventHandlerForType.put(OrderEventType.CHANGED_TP, checkNotNull(removedTPAction));
            return this;
        }

        public Option onMergeReject(final Consumer<IOrder> mergeRejectAction) {
            eventHandlerForType.put(OrderEventType.MERGE_REJECTED, checkNotNull(mergeRejectAction));
            return this;
        }

        public Option onMerge(final Consumer<IOrder> mergedAction) {
            eventHandlerForType.put(OrderEventType.MERGE_OK, checkNotNull(mergedAction));
            return this;
        }

        public Option onMergeClose(final Consumer<IOrder> mergeClosedAction) {
            eventHandlerForType.put(OrderEventType.MERGE_CLOSE_OK, checkNotNull(mergeClosedAction));
            return this;
        }

        @Override
        public MergeProcess build() {
            return new MergeProcess(this);
        }
    }
}
