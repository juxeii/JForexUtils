package com.jforex.programming.order.process.option;

import java.util.function.Consumer;

import com.dukascopy.api.IOrder;

public interface MergeOption<T extends MergeOption<T>> extends SimpleMergeOption<T> {

    public T onRemoveSLReject(Consumer<IOrder> removeSLRejectAction);

    public T onRemoveTPReject(Consumer<IOrder> removeTPRejectAction);

    public T onRemoveSL(Consumer<IOrder> removedSLAction);

    public T onRemoveTP(Consumer<IOrder> removedTPAction);
}
