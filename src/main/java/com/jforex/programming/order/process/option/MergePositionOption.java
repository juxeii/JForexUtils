package com.jforex.programming.order.process.option;

import java.util.function.Consumer;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.process.MergePositionProcess;

public interface MergePositionOption extends CommonOption<MergePositionOption> {

    public MergePositionOption onRemoveSLReject(Consumer<IOrder> removeSLRejectAction);

    public MergePositionOption onRemoveTPReject(Consumer<IOrder> removeTPRejectAction);

    public MergePositionOption onRemoveSL(Consumer<IOrder> removedSLAction);

    public MergePositionOption onRemoveTP(Consumer<IOrder> removedTPAction);

    public MergePositionOption onMergeReject(Consumer<IOrder> mergeRejectAction);

    public MergePositionOption onMerge(Consumer<IOrder> mergedAction);

    public MergePositionOption onMergeClose(Consumer<IOrder> mergeClosedAction);

    public MergePositionProcess build();
}
