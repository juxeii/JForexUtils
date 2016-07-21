package com.jforex.programming.order.command;

import java.util.Collection;

import com.dukascopy.api.IEngine;
import com.dukascopy.api.IOrder;
import com.dukascopy.api.Instrument;
import com.jforex.programming.order.event.OrderEventTypeData;

public class MergeCommand extends OrderCallCommand {

    protected final String mergeOrderLabel;
    protected final Instrument instrument;

    public MergeCommand(final String mergeOrderLabel,
                        final Collection<IOrder> toMergeOrders,
                        final IEngine engine) {
        this.mergeOrderLabel = mergeOrderLabel;
        callable = () -> engine.mergeOrders(mergeOrderLabel, toMergeOrders);
        orderEventTypeData = OrderEventTypeData.mergeData;
        System.out.println("Hallo1 " + toMergeOrders.size());
        instrument = toMergeOrders.iterator().next().getInstrument();
        System.out.println("Hallo2");
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
