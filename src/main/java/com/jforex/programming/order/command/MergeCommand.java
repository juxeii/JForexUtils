package com.jforex.programming.order.command;

import static com.jforex.programming.order.event.OrderEventType.MERGE_CLOSE_OK;
import static com.jforex.programming.order.event.OrderEventType.MERGE_OK;
import static com.jforex.programming.order.event.OrderEventType.MERGE_REJECTED;
import static com.jforex.programming.order.event.OrderEventType.NOTIFICATION;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.Callable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dukascopy.api.IEngine;
import com.dukascopy.api.IOrder;
import com.dukascopy.api.Instrument;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.jforex.programming.order.call.OrderCallReason;
import com.jforex.programming.order.event.OrderEventType;

public final class MergeCommand implements OrderCallCommand {

    private final String mergeOrderLabel;
    private final Instrument instrument;
    private final Callable<IOrder> callable;

    private static final OrderCallReason callReason = OrderCallReason.MERGE;
    private static final ImmutableSet<OrderEventType> doneEventTypes =
            Sets.immutableEnumSet(MERGE_OK, MERGE_CLOSE_OK);
    private static final ImmutableSet<OrderEventType> rejectEventTypes =
            Sets.immutableEnumSet(MERGE_REJECTED);
    private static final ImmutableSet<OrderEventType> infoEventTypes =
            Sets.immutableEnumSet(NOTIFICATION);
    private static final ImmutableSet<OrderEventType> allEventTypes =
            Sets.immutableEnumSet(Sets.union(infoEventTypes, Sets.union(doneEventTypes, rejectEventTypes)));
    private static final Logger logger = LogManager.getLogger(MergeCommand.class);

    public MergeCommand(final String mergeOrderLabel,
                        final Collection<IOrder> toMergeOrders,
                        final IEngine engine) {
        this.mergeOrderLabel = mergeOrderLabel;
        instrument = toMergeOrders.iterator().next().getInstrument();
        callable = () -> engine.mergeOrders(mergeOrderLabel, toMergeOrders);
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
        logger.info("Starting to merge with label " + mergeOrderLabel
                + " for position " + instrument + ".");
    }

    @Override
    public void logOnError(final Throwable t) {
        logger.error("Merging with label " + mergeOrderLabel
                + " for position " + instrument + " failed! Exception: " + t.getMessage());
    }

    @Override
    public void logOnCompleted() {
        logger.info("Merging with label " + mergeOrderLabel
                + " for position " + instrument + " was successful.");
    }
}
