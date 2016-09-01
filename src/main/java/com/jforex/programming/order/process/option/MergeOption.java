package com.jforex.programming.order.process.option;

import java.util.function.Consumer;

import com.dukascopy.api.IOrder;

public interface MergeOption<T extends MergeOption<T>> extends CommonOption<T> {

    public T onRemoveSLReject(Consumer<IOrder> removeSLRejectAction);

    public T onRemoveTPReject(Consumer<IOrder> removeTPRejectAction);

    public T onRemoveSL(Consumer<IOrder> removedSLAction);

    public T onRemoveTP(Consumer<IOrder> removedTPAction);

    public T onMergeReject(Consumer<IOrder> mergeRejectAction);

    public T onMerge(Consumer<IOrder> mergedAction);

    public T onMergeClose(Consumer<IOrder> mergeClosedAction);
}
