package com.jforex.programming.order.process;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Map;
import java.util.function.Consumer;

import com.dukascopy.api.IOrder;
import com.google.common.collect.Maps;
import com.jforex.programming.order.event.OrderEventType;
import com.jforex.programming.order.process.option.AmountOption;
import com.jforex.programming.order.process.option.CloseOption;
import com.jforex.programming.order.process.option.GTTOption;
import com.jforex.programming.order.process.option.LabelOption;
import com.jforex.programming.order.process.option.MergeOption;
import com.jforex.programming.order.process.option.OpenPriceOption;
import com.jforex.programming.order.process.option.SLOption;
import com.jforex.programming.order.process.option.SubmitOption;
import com.jforex.programming.order.process.option.TPOption;

public abstract class CommonBuilder implements
                                    SubmitOption,
                                    MergeOption,
                                    CloseOption,
                                    LabelOption,
                                    GTTOption,
                                    AmountOption,
                                    OpenPriceOption,
                                    SLOption,
                                    TPOption {

    protected Consumer<Throwable> errorAction = o -> {};
    protected int noOfRetries;
    protected long delayInMillis;
    protected Map<OrderEventType, Consumer<IOrder>> eventHandlerForType = Maps.newEnumMap(OrderEventType.class);

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
        return this;
    }

    @Override
    public CommonBuilder onClose(final Consumer<IOrder> closedAction) {
        eventHandlerForType.put(OrderEventType.CLOSE_OK, checkNotNull(closedAction));
        return this;
    }

    @Override
    public CommonBuilder onPartialClose(final Consumer<IOrder> partialClosedAction) {
        eventHandlerForType.put(OrderEventType.PARTIAL_CLOSE_OK, checkNotNull(partialClosedAction));
        return this;
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
        return onSLReject(removeSLRejectAction);
    }

    @Override
    public CommonBuilder onRemoveTPReject(final Consumer<IOrder> removeTPRejectAction) {
        return onTPReject(removeTPRejectAction);
    }

    @Override
    public CommonBuilder onRemoveSL(final Consumer<IOrder> removedSLAction) {
        return onSLChange(removedSLAction);
    }

    @Override
    public CommonBuilder onRemoveTP(final Consumer<IOrder> removedTPAction) {
        return onSLChange(removedTPAction);
    }

    @Override
    public CommonBuilder onMergeReject(final Consumer<IOrder> mergeRejectAction) {
        eventHandlerForType.put(OrderEventType.MERGE_REJECTED, checkNotNull(mergeRejectAction));
        return this;
    }

    @Override
    public CommonBuilder onMerge(final Consumer<IOrder> mergedAction) {
        eventHandlerForType.put(OrderEventType.MERGE_OK, checkNotNull(mergedAction));
        return this;
    }

    @Override
    public CommonBuilder onMergeClose(final Consumer<IOrder> mergeClosedAction) {
        eventHandlerForType.put(OrderEventType.MERGE_CLOSE_OK, checkNotNull(mergeClosedAction));
        return this;
    }

    @Override
    public CommonBuilder onSubmitReject(final Consumer<IOrder> submitRejectAction) {
        eventHandlerForType.put(OrderEventType.SUBMIT_REJECTED, checkNotNull(submitRejectAction));
        return this;
    }

    @Override
    public CommonBuilder onFillReject(final Consumer<IOrder> fillRejectAction) {
        eventHandlerForType.put(OrderEventType.FILL_REJECTED, checkNotNull(fillRejectAction));
        return this;
    }

    @Override
    public CommonBuilder onSubmitOK(final Consumer<IOrder> submitOKAction) {
        eventHandlerForType.put(OrderEventType.SUBMIT_OK, checkNotNull(submitOKAction));
        eventHandlerForType.put(OrderEventType.SUBMIT_CONDITIONAL_OK, checkNotNull(submitOKAction));
        return this;
    }

    @Override
    public CommonBuilder onPartialFill(final Consumer<IOrder> partialFillAction) {
        eventHandlerForType.put(OrderEventType.PARTIAL_FILL_OK, checkNotNull(partialFillAction));
        return this;
    }

    @Override
    public CommonBuilder onFill(final Consumer<IOrder> fillAction) {
        eventHandlerForType.put(OrderEventType.FULLY_FILLED, checkNotNull(fillAction));
        return this;
    }

    @Override
    public CommonBuilder onTPReject(final Consumer<IOrder> rejectAction) {
        eventHandlerForType.put(OrderEventType.CHANGE_TP_REJECTED, checkNotNull(rejectAction));
        return this;
    }

    @Override
    public CommonBuilder onTPChange(final Consumer<IOrder> doneAction) {
        eventHandlerForType.put(OrderEventType.CHANGED_TP, checkNotNull(doneAction));
        return this;
    }

    @Override
    public CommonBuilder onSLReject(final Consumer<IOrder> rejectAction) {
        eventHandlerForType.put(OrderEventType.CHANGE_SL_REJECTED, checkNotNull(rejectAction));
        return this;
    }

    @Override
    public CommonBuilder onSLChange(final Consumer<IOrder> doneAction) {
        eventHandlerForType.put(OrderEventType.CHANGED_SL, checkNotNull(doneAction));
        return this;
    }

    @Override
    public CommonBuilder onGTTReject(final Consumer<IOrder> rejectAction) {
        eventHandlerForType.put(OrderEventType.CHANGE_GTT_REJECTED, checkNotNull(rejectAction));
        return this;
    }

    @Override
    public CommonBuilder onGTTChange(final Consumer<IOrder> doneAction) {
        eventHandlerForType.put(OrderEventType.CHANGED_GTT, checkNotNull(doneAction));
        return this;
    }

    @Override
    public CommonBuilder onLabelReject(final Consumer<IOrder> rejectAction) {
        eventHandlerForType.put(OrderEventType.CHANGE_LABEL_REJECTED, checkNotNull(rejectAction));
        return this;
    }

    @Override
    public CommonBuilder onLabelChange(final Consumer<IOrder> doneAction) {
        eventHandlerForType.put(OrderEventType.CHANGED_LABEL, checkNotNull(doneAction));
        return this;
    }
}
