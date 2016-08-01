package com.jforex.programming.order.command;

import com.dukascopy.api.IEngine;
import com.dukascopy.api.Instrument;
import com.jforex.programming.order.OrderParams;
import com.jforex.programming.order.event.OrderEventTypeData;

public class SubmitCommand extends OrderCallCommand {

    private final String orderLabel;
    private final Instrument instrument;

    public SubmitCommand(final OrderParams orderParams,
                         final IEngine engine) {
        callable = () -> engine.submitOrder(orderParams.label(),
                                            orderParams.instrument(),
                                            orderParams.orderCommand(),
                                            orderParams.amount(),
                                            orderParams.price(),
                                            orderParams.slippage(),
                                            orderParams.stopLossPrice(),
                                            orderParams.takeProfitPrice(),
                                            orderParams.goodTillTime(),
                                            orderParams.comment());
        orderEventTypeData = OrderEventTypeData.submitData;
        orderLabel = orderParams.label();
        instrument = orderParams.instrument();
    }

    @Override
    protected String subscribeLog() {
        return "Start submit task with label "
                + orderLabel + " for " + instrument;
    }

    @Override
    protected String errorLog(final Throwable t) {
        return "Submit task with label " + orderLabel + " for "
                + instrument + " failed!Exception: " + t.getMessage();
    }

    @Override
    protected String completedLog() {
        return "Submit task with label " + orderLabel + " for "
                + instrument + " was successful.";
    }
}
