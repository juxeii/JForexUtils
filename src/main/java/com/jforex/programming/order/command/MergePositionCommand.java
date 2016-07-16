package com.jforex.programming.order.command;

import java.util.Set;

import com.jforex.programming.order.event.OrderEventTypeData;
import com.jforex.programming.position.RestoreSLTPData;

import com.dukascopy.api.IEngine;
import com.dukascopy.api.IOrder;
import com.dukascopy.api.Instrument;

public class MergePositionCommand extends OrderCallCommand {

    private final Set<IOrder> toMergeOrders;
    private final String mergeOrderLabel;
    private final Instrument instrument;
    private final RestoreSLTPData restoreSLTPData;

    public MergePositionCommand(final Set<IOrder> toMergeOrders,
                                final String mergeOrderLabel,
                                final Instrument instrument,
                                final RestoreSLTPData restoreSLTPData,
                                final IEngine engine) {
        super(() -> engine.mergeOrders(mergeOrderLabel, toMergeOrders),
              OrderEventTypeData.mergeData);

        this.toMergeOrders = toMergeOrders;
        this.mergeOrderLabel = mergeOrderLabel;
        this.instrument = instrument;
        this.restoreSLTPData = restoreSLTPData;
    }

    public Set<IOrder> toMergeOrders() {
        return toMergeOrders;
    }

    public String mergeOrderLabel() {
        return mergeOrderLabel;
    }

    public Instrument instrument() {
        return instrument;
    }

    public RestoreSLTPData restoreSLTPData() {
        return restoreSLTPData;
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
