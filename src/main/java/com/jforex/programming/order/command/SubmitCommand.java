package com.jforex.programming.order.command;

import com.dukascopy.api.IEngine;
import com.dukascopy.api.Instrument;
import com.jforex.programming.order.OrderParams;
import com.jforex.programming.order.event.OrderEventTypeData;

public final class SubmitCommand extends OrderCallCommand {

    private final String orderLabel;
    private final Instrument instrument;

    public SubmitCommand(final OrderParams orderParams,
                         final IEngine engine) {
        super(() -> engine.submitOrder(orderParams.label(),
                                       orderParams.instrument(),
                                       orderParams.orderCommand(),
                                       orderParams.amount(),
                                       orderParams.price(),
                                       orderParams.slippage(),
                                       orderParams.stopLossPrice(),
                                       orderParams.takeProfitPrice(),
                                       orderParams.goodTillTime(),
                                       orderParams.comment()),
              OrderEventTypeData.submitData);

        orderLabel = orderParams.label();
        instrument = orderParams.instrument();
    }

    @Override
    protected final String subscribeLog() {
        return "Start submit task with label " + orderLabel + " for " + instrument;
    }

    @Override
    protected final String errorLog(final Throwable t) {
        return "Submit task with label " + orderLabel + " for " + instrument + " failed!Exception: " + t.getMessage();
    }

    @Override
    protected final String completedLog() {
        return "Submit task with label " + orderLabel + " for " + instrument + " was successful.";
    }
}
