package com.jforex.programming.order.command.option;

import java.util.function.Consumer;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.command.MergeCommand;

public interface MergeOption extends CommonOption<MergeOption> {

    public MergeOption doOnMergeReject(Consumer<IOrder> mergeRejectAction);

    public MergeOption doOnMerge(Consumer<IOrder> mergedAction);

    public MergeOption doOnMergeClose(Consumer<IOrder> mergeClosedAction);

    public MergeCommand build();
}
