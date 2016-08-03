package com.jforex.programming.order.command;

import java.util.Collection;

import com.dukascopy.api.IEngine;
import com.dukascopy.api.IOrder;
import com.dukascopy.api.Instrument;
import com.jforex.programming.order.event.OrderEventTypeData;

public final class MergeCommand extends OrderCallCommand {

    private final String mergeOrderLabel;
    private final Instrument instrument;

    public MergeCommand(final String mergeOrderLabel,
                        final Collection<IOrder> toMergeOrders,
                        final IEngine engine) {
        super(() -> engine.mergeOrders(mergeOrderLabel, toMergeOrders),
              OrderEventTypeData.mergeData);

        this.mergeOrderLabel = mergeOrderLabel;
        instrument = toMergeOrders.iterator().next().getInstrument();
    }

    @Override
    protected final String subscribeLog() {
        return "Starting to merge with label " + mergeOrderLabel
                + " for position " + instrument + ".";
    }

    @Override
    protected final String errorLog(final Throwable t) {
        return "Merging with label " + mergeOrderLabel
                + " for position " + instrument + " failed! Exception: " + t.getMessage();
    }

    @Override
    protected final String completedLog() {
        return "Merging with label " + mergeOrderLabel
                + " for position " + instrument + " was successful.";
    }
}
