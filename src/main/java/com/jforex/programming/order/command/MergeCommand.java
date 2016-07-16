package com.jforex.programming.order.command;

import java.util.Collection;

import com.jforex.programming.order.event.OrderEventTypeData;

import com.dukascopy.api.IEngine;
import com.dukascopy.api.IOrder;
import com.dukascopy.api.Instrument;

public class MergeCommand extends OrderCallCommand {

    protected final String mergeOrderLabel;
    protected final Instrument instrument;

    public MergeCommand(final String mergeOrderLabel,
                        final Collection<IOrder> toMergeOrders,
                        final IEngine engine) {
        super(() -> engine.mergeOrders(mergeOrderLabel, toMergeOrders),
              OrderEventTypeData.mergeData);

        this.mergeOrderLabel = mergeOrderLabel;
        instrument = toMergeOrders.iterator().next().getInstrument();
    }

    @Override
    protected String subscribeLog() {
        return "Starting to merge with label " + mergeOrderLabel
                + " for position " + instrument + ".";
    }

    @Override
    protected String errorLog(final Throwable e) {
        return "Merging with label " + mergeOrderLabel
                + " for position " + instrument + " failed! Exception: " + e.getMessage();
    }

    @Override
    protected String completedLog() {
        return "Merging with label " + mergeOrderLabel
                + " for position " + instrument + " was successful.";
    }
}
