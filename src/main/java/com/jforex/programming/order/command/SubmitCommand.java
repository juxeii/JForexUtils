package com.jforex.programming.order.command;

import static com.jforex.programming.order.event.OrderEventType.FILL_REJECTED;
import static com.jforex.programming.order.event.OrderEventType.FULLY_FILLED;
import static com.jforex.programming.order.event.OrderEventType.NOTIFICATION;
import static com.jforex.programming.order.event.OrderEventType.PARTIAL_FILL_OK;
import static com.jforex.programming.order.event.OrderEventType.SUBMIT_CONDITIONAL_OK;
import static com.jforex.programming.order.event.OrderEventType.SUBMIT_OK;
import static com.jforex.programming.order.event.OrderEventType.SUBMIT_REJECTED;

import com.dukascopy.api.IEngine;
import com.dukascopy.api.Instrument;
import com.google.common.collect.Sets;
import com.jforex.programming.order.OrderParams;
import com.jforex.programming.order.call.OrderCallReason;

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
                                       orderParams.comment()));

        orderLabel = orderParams.label();
        instrument = orderParams.instrument();
    }

    @Override
    protected void initEventTypes() {
        doneEventTypes =
                Sets.immutableEnumSet(FULLY_FILLED, SUBMIT_CONDITIONAL_OK);
        rejectEventTypes =
                Sets.immutableEnumSet(FILL_REJECTED, SUBMIT_REJECTED);
        infoEventTypes =
                Sets.immutableEnumSet(NOTIFICATION, SUBMIT_OK, PARTIAL_FILL_OK);
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

    @Override
    public OrderCallReason callReason() {
        return OrderCallReason.SUBMIT;
    }
}
