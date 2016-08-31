package com.jforex.programming.order.process;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.function.Consumer;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.event.OrderEventType;

@SuppressWarnings("unchecked")
public class CommonMergeProcess<T extends CommonMergeProcess<T>> extends CommonProcess<T> {

    protected final String mergeOrderLabel;

    protected CommonMergeProcess(final String mergeOrderLabel) {
        this.mergeOrderLabel = mergeOrderLabel;
    }

    public T onRemoveSLReject(final Consumer<IOrder> changeSLRejectAction) {
        eventHandlerForType.put(OrderEventType.CHANGE_SL_REJECTED, checkNotNull(changeSLRejectAction));
        return (T) this;
    }

    public T onRemoveTPReject(final Consumer<IOrder> changeTPRejectAction) {
        eventHandlerForType.put(OrderEventType.CHANGE_TP_REJECTED, checkNotNull(changeTPRejectAction));
        return (T) this;
    }

    public T onRemoveSL(final Consumer<IOrder> changedSLAction) {
        eventHandlerForType.put(OrderEventType.CHANGED_SL, checkNotNull(changedSLAction));
        return (T) this;
    }

    public T onRemoveTP(final Consumer<IOrder> changedTPAction) {
        eventHandlerForType.put(OrderEventType.CHANGED_TP, checkNotNull(changedTPAction));
        return (T) this;
    }

    public T onMergeReject(final Consumer<IOrder> mergeRejectAction) {
        eventHandlerForType.put(OrderEventType.MERGE_REJECTED, checkNotNull(mergeRejectAction));
        return (T) this;
    }

    public T onMerge(final Consumer<IOrder> mergeOKAction) {
        eventHandlerForType.put(OrderEventType.MERGE_OK, checkNotNull(mergeOKAction));
        return (T) this;
    }

    public T onMergeClose(final Consumer<IOrder> mergeCloseOKAction) {
        eventHandlerForType.put(OrderEventType.MERGE_CLOSE_OK, checkNotNull(mergeCloseOKAction));
        return (T) this;
    }
}
