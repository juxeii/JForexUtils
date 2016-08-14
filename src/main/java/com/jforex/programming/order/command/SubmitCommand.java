package com.jforex.programming.order.command;

import static com.jforex.programming.order.event.OrderEventType.FILL_REJECTED;
import static com.jforex.programming.order.event.OrderEventType.FULLY_FILLED;
import static com.jforex.programming.order.event.OrderEventType.NOTIFICATION;
import static com.jforex.programming.order.event.OrderEventType.PARTIAL_FILL_OK;
import static com.jforex.programming.order.event.OrderEventType.SUBMIT_CONDITIONAL_OK;
import static com.jforex.programming.order.event.OrderEventType.SUBMIT_OK;
import static com.jforex.programming.order.event.OrderEventType.SUBMIT_REJECTED;

import java.util.Set;
import java.util.concurrent.Callable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dukascopy.api.IEngine;
import com.dukascopy.api.IOrder;
import com.dukascopy.api.Instrument;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.jforex.programming.order.OrderParams;
import com.jforex.programming.order.call.OrderCallReason;
import com.jforex.programming.order.event.OrderEventType;

public final class SubmitCommand implements OrderCallCommand {

    private final String orderLabel;
    private final Instrument instrument;
    private final Callable<IOrder> callable;

    private static final OrderCallReason callReason = OrderCallReason.SUBMIT;
    private static final ImmutableSet<OrderEventType> doneEventTypes =
            Sets.immutableEnumSet(FULLY_FILLED, SUBMIT_CONDITIONAL_OK);
    private static final ImmutableSet<OrderEventType> rejectEventTypes =
            Sets.immutableEnumSet(FILL_REJECTED, SUBMIT_REJECTED);
    private static final ImmutableSet<OrderEventType> infoEventTypes =
            Sets.immutableEnumSet(NOTIFICATION, SUBMIT_OK, PARTIAL_FILL_OK);
    private static final ImmutableSet<OrderEventType> allEventTypes =
            Sets.immutableEnumSet(Sets.union(infoEventTypes, Sets.union(doneEventTypes, rejectEventTypes)));
    private static final Logger logger = LogManager.getLogger(SubmitCommand.class);

    public SubmitCommand(final OrderParams orderParams,
                         final IEngine engine) {
        orderLabel = orderParams.label();
        instrument = orderParams.instrument();
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
    }

    @Override
    public Set<OrderEventType> allEventTypes() {
        return allEventTypes;
    }

    @Override
    public Set<OrderEventType> doneEventTypes() {
        return doneEventTypes;
    }

    @Override
    public Set<OrderEventType> rejectEventTypes() {
        return rejectEventTypes;
    }

    @Override
    public Callable<IOrder> callable() {
        return callable;
    }

    @Override
    public OrderCallReason callReason() {
        return callReason;
    }

    @Override
    public void logOnSubscribe() {
        logger.info("Start submit task with label " + orderLabel + " for " + instrument);
    }

    @Override
    public void logOnError(final Throwable t) {
        logger.error("Submit task with label " + orderLabel + " for " + instrument
                + " failed!Exception: " + t.getMessage());
    }

    @Override
    public void logOnCompleted() {
        logger.info("Submit task with label " + orderLabel + " for " + instrument + " was successful.");
    }
}
