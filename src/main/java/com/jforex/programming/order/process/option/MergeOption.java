package com.jforex.programming.order.process.option;

import java.util.function.Consumer;

import com.dukascopy.api.IOrder;

public interface MergeOption<T extends MergeOption<T>> extends CommonOption<T> {

    public T doOnMergeReject(Consumer<IOrder> mergeRejectAction);

    public T doOnMerge(Consumer<IOrder> mergedAction);

    public T doOnMergeClose(Consumer<IOrder> mergeClosedAction);
}
