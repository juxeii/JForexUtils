package com.jforex.programming.order.process;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.function.Consumer;

import com.dukascopy.api.IOrder;
import com.dukascopy.api.Instrument;
import com.jforex.programming.order.event.OrderEventType;
import com.jforex.programming.order.process.option.MergeOption;

public class MergePositionProcess extends CommonProcess {

    private final String mergeOrderLabel;
    private final Instrument instrument;

    public interface Option extends MergeOption<Option> {

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

    public static final Option forParams(final String mergeOrderLabel,
                                         final Instrument instrument) {
        return new Builder(checkNotNull(mergeOrderLabel), checkNotNull(instrument));
    }

    private static class Builder extends CommonBuilder<Option>
                                 implements Option {

        private final String mergeOrderLabel;
        private final Instrument instrument;

        private Builder(final String mergeOrderLabel,
                        final Instrument instrument) {
            this.mergeOrderLabel = mergeOrderLabel;
            this.instrument = instrument;
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
        public MergePositionProcess build() {
            return new MergePositionProcess(this);
        }
    }
}
