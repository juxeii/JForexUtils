package com.jforex.programming.order.process;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.function.Consumer;

import com.dukascopy.api.IOrder;
import com.dukascopy.api.Instrument;
import com.jforex.programming.order.event.OrderEventType;

public class MergePositionProcess extends OrderProcess {

    private final String mergeOrderLabel;
    private final Instrument instrument;

    public interface MergeOption extends CommonOption<MergeOption> {

        public MergeOption onRemoveSLReject(Consumer<IOrder> removeSLRejectAction);

        public MergeOption onRemoveTPReject(Consumer<IOrder> removeTPRejectAction);

        public MergeOption onRemoveSL(Consumer<IOrder> removedSLAction);

        public MergeOption onRemoveTP(Consumer<IOrder> removedTPAction);

        public MergeOption onMergeReject(Consumer<IOrder> mergeRejectAction);

        public MergeOption onMerge(Consumer<IOrder> mergedAction);

        public MergeOption onMergeClose(Consumer<IOrder> mergeClosedAction);

        public MergePositionProcess build();
    }

    private MergePositionProcess(final Builder builder) {
        super(builder);
        mergeOrderLabel = builder.mergeOrderLabel;
        instrument = builder.instrument;
    }

    public final String mergeOrderLabel() {
        return mergeOrderLabel;
    }

    public final Instrument instrument() {
        return instrument;
    }

    public static final MergeOption forParams(final String mergeOrderLabel,
                                              final Instrument instrument) {
        return new Builder(checkNotNull(mergeOrderLabel), checkNotNull(instrument));
    }

    private static class Builder extends CommonProcess<Builder> implements MergeOption {

        private final String mergeOrderLabel;
        private final Instrument instrument;

        private Builder(final String mergeOrderLabel,
                        final Instrument instrument) {
            this.mergeOrderLabel = mergeOrderLabel;
            this.instrument = instrument;
        }

        @Override
        public MergeOption onRemoveSLReject(final Consumer<IOrder> changeSLRejectAction) {
            eventHandlerForType.put(OrderEventType.CHANGE_SL_REJECTED, checkNotNull(changeSLRejectAction));
            return this;
        }

        @Override
        public MergeOption onRemoveTPReject(final Consumer<IOrder> changeTPRejectAction) {
            eventHandlerForType.put(OrderEventType.CHANGE_TP_REJECTED, checkNotNull(changeTPRejectAction));
            return this;
        }

        @Override
        public MergeOption onRemoveSL(final Consumer<IOrder> changedSLAction) {
            eventHandlerForType.put(OrderEventType.CHANGED_SL, checkNotNull(changedSLAction));
            return this;
        }

        @Override
        public MergeOption onRemoveTP(final Consumer<IOrder> changedTPAction) {
            eventHandlerForType.put(OrderEventType.CHANGED_TP, checkNotNull(changedTPAction));
            return this;
        }

        @Override
        public MergeOption onMergeReject(final Consumer<IOrder> mergeRejectAction) {
            eventHandlerForType.put(OrderEventType.MERGE_REJECTED, checkNotNull(mergeRejectAction));
            return this;
        }

        @Override
        public MergeOption onMerge(final Consumer<IOrder> mergeOKAction) {
            eventHandlerForType.put(OrderEventType.MERGE_OK, checkNotNull(mergeOKAction));
            return this;
        }

        @Override
        public MergeOption onMergeClose(final Consumer<IOrder> mergeCloseOKAction) {
            eventHandlerForType.put(OrderEventType.MERGE_CLOSE_OK, checkNotNull(mergeCloseOKAction));
            return this;
        }

        @Override
        public MergePositionProcess build() {
            return new MergePositionProcess(this);
        }
    }
}
