package com.jforex.programming.order.command;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Collection;

import com.dukascopy.api.IOrder;

public class MergeOrdersCommand {

    private final Collection<IOrder> toMergeOrders;
    private final MergeCommand mergeCommand;

    public MergeOrdersCommand(final Collection<IOrder> toMergeOrders,
                              final MergeCommand mergeCommand) {
        this.toMergeOrders = checkNotNull(toMergeOrders);
        this.mergeCommand = checkNotNull(mergeCommand);
    }

    public final Collection<IOrder> toMergeOrders() {
        return toMergeOrders;
    }

    public final MergeCommand mergeCommand() {
        return mergeCommand;
    }
}
