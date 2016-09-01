package com.jforex.programming.order.process.option;

import java.util.function.Consumer;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.process.SubmitAndMergePositionProcess;

public interface SubmitAndMergeOption extends CommonOption<SubmitAndMergeOption> {

    public SubmitAndMergeOption onSubmitReject(Consumer<IOrder> submitRejectAction);

    public SubmitAndMergeOption onFillReject(Consumer<IOrder> fillRejectAction);

    public SubmitAndMergeOption onSubmitOK(Consumer<IOrder> submitOKAction);

    public SubmitAndMergeOption onPartialFill(Consumer<IOrder> partialFillAction);

    public SubmitAndMergeOption onFill(Consumer<IOrder> fillAction);

    public SubmitAndMergeOption onRemoveSLReject(Consumer<IOrder> removeSLRejectAction);

    public SubmitAndMergeOption onRemoveTPReject(Consumer<IOrder> removeTPRejectAction);

    public SubmitAndMergeOption onRemoveSL(Consumer<IOrder> removedSLAction);

    public SubmitAndMergeOption onRemoveTP(Consumer<IOrder> removedTPAction);

    public SubmitAndMergeOption onMergeReject(Consumer<IOrder> mergeRejectAction);

    public SubmitAndMergeOption onMerge(Consumer<IOrder> mergedAction);

    public SubmitAndMergeOption onMergeClose(Consumer<IOrder> mergeClosedAction);

    public SubmitAndMergePositionProcess build();
}
