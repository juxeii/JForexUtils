package com.jforex.programming.order.process;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Map;
import java.util.function.Consumer;

import com.dukascopy.api.IOrder;
import com.google.common.collect.Maps;
import com.jforex.programming.order.event.OrderEventType;
import com.jforex.programming.order.process.option.CommonOption;

@SuppressWarnings("unchecked")
public abstract class CommonBuilder<T extends CommonOption> implements CommonOption<T> {

    protected Consumer<Throwable> errorAction = o -> {};
    protected int noOfRetries;
    protected long delayInMillis;
    protected Map<OrderEventType, Consumer<IOrder>> eventHandlerForType = Maps.newEnumMap(OrderEventType.class);

    @Override
    public T onError(final Consumer<Throwable> errorAction) {
        this.errorAction = checkNotNull(errorAction);
        return (T) this;
    }

    @Override
    public T doRetries(final int noOfRetries, final long delayInMillis) {
        this.noOfRetries = noOfRetries;
        this.delayInMillis = delayInMillis;
        return (T) this;
    }

    public T onSubmitReject(final Consumer<IOrder> submitRejectAction) {
        eventHandlerForType.put(OrderEventType.SUBMIT_REJECTED, checkNotNull(submitRejectAction));
        return (T) this;
    }

    public T onFillReject(final Consumer<IOrder> fillRejectAction) {
        eventHandlerForType.put(OrderEventType.FILL_REJECTED, checkNotNull(fillRejectAction));
        return (T) this;
    }

    public T onSubmitOK(final Consumer<IOrder> submitOKAction) {
        eventHandlerForType.put(OrderEventType.SUBMIT_OK, checkNotNull(submitOKAction));
        eventHandlerForType.put(OrderEventType.SUBMIT_CONDITIONAL_OK, checkNotNull(submitOKAction));
        return (T) this;
    }

    public T onPartialFill(final Consumer<IOrder> partialFillAction) {
        eventHandlerForType.put(OrderEventType.PARTIAL_FILL_OK, checkNotNull(partialFillAction));
        return (T) this;
    }

    public T onFill(final Consumer<IOrder> fillAction) {
        eventHandlerForType.put(OrderEventType.FULLY_FILLED, checkNotNull(fillAction));
        return (T) this;
    }

    public T onRemoveSLReject(final Consumer<IOrder> removeSLRejectAction) {
        eventHandlerForType.put(OrderEventType.CHANGE_SL_REJECTED, checkNotNull(removeSLRejectAction));
        return (T) this;
    }

    public T onRemoveTPReject(final Consumer<IOrder> removeTPRejectAction) {
        eventHandlerForType.put(OrderEventType.CHANGE_TP_REJECTED, checkNotNull(removeTPRejectAction));
        return (T) this;
    }

    public T onRemoveSL(final Consumer<IOrder> removedSLAction) {
        eventHandlerForType.put(OrderEventType.CHANGED_SL, checkNotNull(removedSLAction));
        return (T) this;
    }

    public T onRemoveTP(final Consumer<IOrder> removedTPAction) {
        eventHandlerForType.put(OrderEventType.CHANGED_TP, checkNotNull(removedTPAction));
        return (T) this;
    }

    public T onMergeReject(final Consumer<IOrder> mergeRejectAction) {
        eventHandlerForType.put(OrderEventType.MERGE_REJECTED, checkNotNull(mergeRejectAction));
        return (T) this;
    }

    public T onMerge(final Consumer<IOrder> mergedAction) {
        eventHandlerForType.put(OrderEventType.MERGE_OK, checkNotNull(mergedAction));
        return (T) this;
    }

    public T onMergeClose(final Consumer<IOrder> mergeClosedAction) {
        eventHandlerForType.put(OrderEventType.MERGE_CLOSE_OK, checkNotNull(mergeClosedAction));
        return (T) this;
    }

    public T onCloseReject(final Consumer<IOrder> closeRejectAction) {
        eventHandlerForType.put(OrderEventType.CLOSE_REJECTED, checkNotNull(closeRejectAction));
        return (T) this;
    }

    public T onClose(final Consumer<IOrder> closedAction) {
        eventHandlerForType.put(OrderEventType.CLOSE_OK, checkNotNull(closedAction));
        return (T) this;
    }

    public T onPartialClose(final Consumer<IOrder> partialClosedAction) {
        eventHandlerForType.put(OrderEventType.PARTIAL_CLOSE_OK, checkNotNull(partialClosedAction));
        return (T) this;
    }

    public T onTPReject(final Consumer<IOrder> rejectAction) {
        eventHandlerForType.put(OrderEventType.CHANGE_TP_REJECTED, checkNotNull(rejectAction));
        return (T) this;
    }

    public T onTPChange(final Consumer<IOrder> doneAction) {
        eventHandlerForType.put(OrderEventType.CHANGED_TP, checkNotNull(doneAction));
        return (T) this;
    }

    public T onSLReject(final Consumer<IOrder> rejectAction) {
        eventHandlerForType.put(OrderEventType.CHANGE_SL_REJECTED, checkNotNull(rejectAction));
        return (T) this;
    }

    public T onSLChange(final Consumer<IOrder> doneAction) {
        eventHandlerForType.put(OrderEventType.CHANGED_SL, checkNotNull(doneAction));
        return (T) this;
    }

    public T onOpenPriceReject(final Consumer<IOrder> rejectAction) {
        eventHandlerForType.put(OrderEventType.CHANGE_PRICE_REJECTED, checkNotNull(rejectAction));
        return (T) this;
    }

    public T onOpenPriceChange(final Consumer<IOrder> doneAction) {
        eventHandlerForType.put(OrderEventType.CHANGED_PRICE, checkNotNull(doneAction));
        return (T) this;
    }

    public T onLabelReject(final Consumer<IOrder> rejectAction) {
        eventHandlerForType.put(OrderEventType.CHANGE_LABEL_REJECTED, checkNotNull(rejectAction));
        return (T) this;
    }

    public T onLabelChange(final Consumer<IOrder> doneAction) {
        eventHandlerForType.put(OrderEventType.CHANGED_LABEL, checkNotNull(doneAction));
        return (T) this;
    }

    public T onGTTReject(final Consumer<IOrder> rejectAction) {
        eventHandlerForType.put(OrderEventType.CHANGE_GTT_REJECTED, checkNotNull(rejectAction));
        return (T) this;
    }

    public T onGTTChange(final Consumer<IOrder> doneAction) {
        eventHandlerForType.put(OrderEventType.CHANGED_GTT, checkNotNull(doneAction));
        return (T) this;
    }

    public T onAmountReject(final Consumer<IOrder> rejectAction) {
        eventHandlerForType.put(OrderEventType.CHANGE_AMOUNT_REJECTED, checkNotNull(rejectAction));
        return (T) this;
    }

    public T onAmountChange(final Consumer<IOrder> okAction) {
        eventHandlerForType.put(OrderEventType.CHANGED_AMOUNT, checkNotNull(okAction));
        return (T) this;
    }
}
