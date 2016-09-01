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

    public T onSubmitOK(final Consumer<IOrder> submitOKAction) {
        eventHandlerForType.put(OrderEventType.SUBMIT_OK, checkNotNull(submitOKAction));
        eventHandlerForType.put(OrderEventType.SUBMIT_CONDITIONAL_OK, checkNotNull(submitOKAction));
        return (T) this;
    }

    public T onSubmitReject(final Consumer<IOrder> submitRejectAction) {
        return registerActionHandler(OrderEventType.SUBMIT_REJECTED, submitRejectAction);
    }

    public T onFillReject(final Consumer<IOrder> fillRejectAction) {
        return registerActionHandler(OrderEventType.FILL_REJECTED, fillRejectAction);
    }

    public T onPartialFill(final Consumer<IOrder> partialFillAction) {
        return registerActionHandler(OrderEventType.PARTIAL_FILL_OK, partialFillAction);
    }

    public T onFill(final Consumer<IOrder> fillAction) {
        return registerActionHandler(OrderEventType.FULLY_FILLED, fillAction);
    }

    public T onRemoveSLReject(final Consumer<IOrder> removeSLRejectAction) {
        return registerActionHandler(OrderEventType.CHANGE_SL_REJECTED, removeSLRejectAction);
    }

    public T onRemoveTPReject(final Consumer<IOrder> removeTPRejectAction) {
        return registerActionHandler(OrderEventType.CHANGE_TP_REJECTED, removeTPRejectAction);
    }

    public T onRemoveSL(final Consumer<IOrder> removedSLAction) {
        return registerActionHandler(OrderEventType.CHANGED_SL, removedSLAction);
    }

    public T onRemoveTP(final Consumer<IOrder> removedTPAction) {
        return registerActionHandler(OrderEventType.CHANGED_TP, removedTPAction);
    }

    public T onMergeReject(final Consumer<IOrder> mergeRejectAction) {
        return registerActionHandler(OrderEventType.MERGE_REJECTED, mergeRejectAction);
    }

    public T onMerge(final Consumer<IOrder> mergedAction) {
        return registerActionHandler(OrderEventType.MERGE_OK, mergedAction);
    }

    public T onMergeClose(final Consumer<IOrder> mergeClosedAction) {
        return registerActionHandler(OrderEventType.MERGE_CLOSE_OK, mergeClosedAction);
    }

    public T onCloseReject(final Consumer<IOrder> closeRejectAction) {
        return registerActionHandler(OrderEventType.CLOSE_REJECTED, closeRejectAction);
    }

    public T onClose(final Consumer<IOrder> closedAction) {
        return registerActionHandler(OrderEventType.CLOSE_OK, closedAction);
    }

    public T onPartialClose(final Consumer<IOrder> partialClosedAction) {
        return registerActionHandler(OrderEventType.PARTIAL_CLOSE_OK, partialClosedAction);
    }

    public T onTPReject(final Consumer<IOrder> rejectAction) {
        return registerActionHandler(OrderEventType.CHANGE_TP_REJECTED, rejectAction);
    }

    public T onTPChange(final Consumer<IOrder> doneAction) {
        return registerActionHandler(OrderEventType.CHANGED_TP, doneAction);
    }

    public T onSLReject(final Consumer<IOrder> rejectAction) {
        return registerActionHandler(OrderEventType.CHANGE_SL_REJECTED, rejectAction);
    }

    public T onSLChange(final Consumer<IOrder> doneAction) {
        return registerActionHandler(OrderEventType.CHANGED_SL, doneAction);
    }

    public T onOpenPriceReject(final Consumer<IOrder> rejectAction) {
        return registerActionHandler(OrderEventType.CHANGE_PRICE_REJECTED, rejectAction);
    }

    public T onOpenPriceChange(final Consumer<IOrder> doneAction) {
        return registerActionHandler(OrderEventType.CHANGED_PRICE, doneAction);
    }

    public T onLabelReject(final Consumer<IOrder> rejectAction) {
        return registerActionHandler(OrderEventType.CHANGE_LABEL_REJECTED, rejectAction);
    }

    public T onLabelChange(final Consumer<IOrder> doneAction) {
        return registerActionHandler(OrderEventType.CHANGED_LABEL, doneAction);
    }

    public T onGTTReject(final Consumer<IOrder> rejectAction) {
        return registerActionHandler(OrderEventType.CHANGE_GTT_REJECTED, rejectAction);
    }

    public T onGTTChange(final Consumer<IOrder> doneAction) {
        return registerActionHandler(OrderEventType.CHANGED_GTT, doneAction);
    }

    public T onAmountReject(final Consumer<IOrder> rejectAction) {
        return registerActionHandler(OrderEventType.CHANGE_AMOUNT_REJECTED, rejectAction);
    }

    public T onAmountChange(final Consumer<IOrder> okAction) {
        return registerActionHandler(OrderEventType.CHANGED_AMOUNT, okAction);
    }

    private T registerActionHandler(final OrderEventType type,
                                    final Consumer<IOrder> action) {
        eventHandlerForType.put(type, checkNotNull(action));
        return (T) this;
    }
}
