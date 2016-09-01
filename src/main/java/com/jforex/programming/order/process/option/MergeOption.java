package com.jforex.programming.order.process.option;

import java.util.function.Consumer;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.process.MergeProcess;

public interface MergeOption extends CommonOption<MergeOption> {

    public MergeOption onRemoveSLReject(Consumer<IOrder> removeSLRejectAction);

    public MergeOption onRemoveTPReject(Consumer<IOrder> removeTPRejectAction);

    public MergeOption onRemoveSL(Consumer<IOrder> removedSLAction);

    public MergeOption onRemoveTP(Consumer<IOrder> removedTPAction);

    public MergeOption onMergeReject(Consumer<IOrder> mergeRejectAction);

    public MergeOption onMerge(Consumer<IOrder> mergedAction);

    public MergeOption onMergeClose(Consumer<IOrder> mergeClosedAction);

    public MergeProcess build();
}
