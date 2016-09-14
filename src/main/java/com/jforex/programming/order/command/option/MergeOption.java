package com.jforex.programming.order.command.option;

import java.util.function.Consumer;

import com.dukascopy.api.IOrder;

public interface MergeOption extends CommonOption<MergeOption> {

    public MergeOption doOnMergeReject(Consumer<IOrder> mergeRejectConsumer);

    public MergeOption doOnMerge(Consumer<IOrder> mergedConsumer);

    public MergeOption doOnMergeClose(Consumer<IOrder> mergeClosedConsumer);
}
