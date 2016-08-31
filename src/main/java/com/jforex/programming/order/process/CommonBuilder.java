package com.jforex.programming.order.process;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Map;
import java.util.function.Consumer;

import com.dukascopy.api.IOrder;
import com.google.common.collect.Maps;
import com.jforex.programming.order.event.OrderEventType;

public class CommonBuilder implements
        SubmitOption,
        MergeOption,
        AmountOption,
        OpenPriceOption,
        CloseOption {

    protected Consumer<Throwable> errorAction = o -> {};
    protected int noOfRetries;
    protected long delayInMillis;
    protected Map<OrderEventType, Consumer<IOrder>> eventHandlerForType = Maps.newEnumMap(OrderEventType.class);

    @Override
    public <T> T build() {
        return null;
    }

    @Override
    public CommonBuilder onError(final Consumer<Throwable> errorAction) {
        this.errorAction = checkNotNull(errorAction);
        return this;
    }

    @Override
    public CommonBuilder doRetries(final int noOfRetries,
                                   final long delayInMillis) {
        this.noOfRetries = noOfRetries;
        this.delayInMillis = delayInMillis;
        return this;
    }

    @Override
    public CommonBuilder onCloseReject(final Consumer<IOrder> closeRejectAction) {
        eventHandlerForType.put(OrderEventType.CLOSE_REJECTED, checkNotNull(closeRejectAction));
        return null;
    }

    @Override
    public CommonBuilder onClose(final Consumer<IOrder> closedAction) {
        eventHandlerForType.put(OrderEventType.CLOSE_OK, checkNotNull(closedAction));
        return null;
    }

    @Override
    public CommonBuilder onPartialClose(final Consumer<IOrder> partialClosedAction) {
        eventHandlerForType.put(OrderEventType.PARTIAL_CLOSE_OK, checkNotNull(partialClosedAction));
        return null;
    }

    @Override
    public CommonBuilder onAmountReject(final Consumer<IOrder> rejectAction) {
        eventHandlerForType.put(OrderEventType.CHANGE_AMOUNT_REJECTED, checkNotNull(rejectAction));
        return this;
    }

    @Override
    public CommonBuilder onAmountChange(final Consumer<IOrder> okAction) {
        eventHandlerForType.put(OrderEventType.CHANGED_AMOUNT, checkNotNull(okAction));
        return this;
    }

    @Override
    public CommonBuilder onOpenPriceReject(final Consumer<IOrder> rejectAction) {
        eventHandlerForType.put(OrderEventType.CHANGE_PRICE_REJECTED, checkNotNull(rejectAction));
        return this;
    }

    @Override
    public CommonBuilder onOpenPriceChange(final Consumer<IOrder> doneAction) {
        eventHandlerForType.put(OrderEventType.CHANGED_PRICE, checkNotNull(doneAction));
        return this;
    }

    @Override
    public CommonBuilder onRemoveSLReject(final Consumer<IOrder> removeSLRejectAction) {
        eventHandlerForType.put(OrderEventType.CHANGE_SL_REJECTED, checkNotNull(removeSLRejectAction));
        return this;
    }

    @Override
    public CommonBuilder onRemoveTPReject(final Consumer<IOrder> removeTPRejectAction) {
        eventHandlerForType.put(OrderEventType.CHANGE_TP_REJECTED, checkNotNull(removeTPRejectAction));
        return this;
    }

    @Override
    public CommonBuilder onRemoveSL(final Consumer<IOrder> removedSLAction) {
        eventHandlerForType.put(OrderEventType.CHANGED_SL,
                                checkNotNull(removedSLAction));
        return this;
    }

    @Override
    public CommonBuilder onRemoveTP(final Consumer<IOrder> removedTPAction) {
        eventHandlerForType.put(OrderEventType.CHANGED_TP,
                                checkNotNull(removedTPAction));
        return this;
    }

    @Override
    public CommonBuilder onMergeReject(final Consumer<IOrder> mergeRejectAction) {
        eventHandlerForType.put(OrderEventType.MERGE_REJECTED,
                                checkNotNull(mergeRejectAction));
        return this;
    }

    @Override
    public CommonBuilder onMerge(final Consumer<IOrder> mergedAction) {
        eventHandlerForType.put(OrderEventType.MERGE_OK,
                                checkNotNull(mergedAction));
        return this;
    }

    @Override
    public CommonBuilder onMergeClose(final Consumer<IOrder> mergeClosedAction) {
        eventHandlerForType.put(OrderEventType.MERGE_CLOSE_OK,
                                checkNotNull(mergeClosedAction));
        return this;
    }

    @Override
    public CommonBuilder onSubmitReject(final Consumer<IOrder> submitRejectAction) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public CommonBuilder onFillReject(final Consumer<IOrder> fillRejectAction) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public CommonBuilder onSubmitOK(final Consumer<IOrder> submitOKAction) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public CommonBuilder onPartialFill(final Consumer<IOrder> partialFillAction) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public CommonBuilder onFill(final Consumer<IOrder> fillAction) {
        // TODO Auto-generated method stub
        return null;
    }

    public void putMergeReject(final Consumer<IOrder> mergeRejectAction) {

    }

    public void putMerge(final Consumer<IOrder> mergeOKAction) {

    }

    public void putMergeClose(final Consumer<IOrder> mergeCloseOKAction) {

    }

    public void putSubmitReject(final Consumer<IOrder> submitRejectAction) {
        eventHandlerForType.put(OrderEventType.SUBMIT_REJECTED,
                                checkNotNull(submitRejectAction));
    }

    public void putFillReject(final Consumer<IOrder> fillRejectAction) {
        eventHandlerForType.put(OrderEventType.FILL_REJECTED,
                                checkNotNull(fillRejectAction));
    }

    public void putSubmitOK(final Consumer<IOrder> submitOKAction) {
        eventHandlerForType.put(OrderEventType.SUBMIT_OK,
                                checkNotNull(submitOKAction));
        eventHandlerForType.put(OrderEventType.SUBMIT_CONDITIONAL_OK,
                                checkNotNull(submitOKAction));
    }

    public void putPartialFill(final Consumer<IOrder> partialFillAction) {
        eventHandlerForType.put(OrderEventType.PARTIAL_FILL_OK,
                                checkNotNull(partialFillAction));
    }

    public void putFill(final Consumer<IOrder> fillAction) {
        eventHandlerForType.put(OrderEventType.FULLY_FILLED,
                                checkNotNull(fillAction));
    }
}
