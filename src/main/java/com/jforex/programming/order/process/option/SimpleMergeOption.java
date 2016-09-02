package com.jforex.programming.order.process.option;

import java.util.function.Consumer;

import com.dukascopy.api.IOrder;

public interface SimpleMergeOption<T extends SimpleMergeOption<T>> extends CommonOption<T> {

    public T onMergeReject(Consumer<IOrder> mergeRejectAction);

    public T onMerge(Consumer<IOrder> mergedAction);

    public T onMergeClose(Consumer<IOrder> mergeClosedAction);
}
