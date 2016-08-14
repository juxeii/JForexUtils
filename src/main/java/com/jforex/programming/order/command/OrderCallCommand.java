package com.jforex.programming.order.command;

import java.util.Set;
import java.util.concurrent.Callable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dukascopy.api.IOrder;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.jforex.programming.order.call.OrderCallReason;
import com.jforex.programming.order.event.OrderEventType;
import com.jforex.programming.order.event.OrderEventTypeData;

public abstract class OrderCallCommand {

    private final Callable<IOrder> callable;
    private final OrderEventTypeData orderEventTypeData;
    private final ImmutableSet<OrderEventType> allEventTypes;
    protected ImmutableSet<OrderEventType> doneEventTypes;
    protected ImmutableSet<OrderEventType> rejectEventTypes;
    protected ImmutableSet<OrderEventType> infoEventTypes;

    protected static final Logger logger = LogManager.getLogger(OrderCallCommand.class);

    public OrderCallCommand(final Callable<IOrder> callable,
                            final OrderEventTypeData orderEventTypeData) {
        this.callable = callable;
        this.orderEventTypeData = orderEventTypeData;

        initDoneEvents();
        initRejectEvents();
        initInfoEvents();
        allEventTypes = Sets.immutableEnumSet(Sets.union(infoEventTypes,
                                                         Sets.union(doneEventTypes, rejectEventTypes)));
    }

    protected abstract void initDoneEvents();

    protected abstract void initRejectEvents();

    protected abstract void initInfoEvents();

    public final Callable<IOrder> callable() {
        return callable;
    }

    public abstract OrderCallReason callReason();

    public final Set<OrderEventType> allEventTypes() {
        return allEventTypes;
    }

    public final Set<OrderEventType> doneEventTypes() {
        return doneEventTypes;
    }

    public final Set<OrderEventType> rejectEventTypes() {
        return rejectEventTypes;
    }

    public final OrderEventTypeData orderEventTypeData() {
        return orderEventTypeData;
    }

    public final void logOnSubscribe() {
        logger.info(subscribeLog());
    }

    public final void logOnError(final Throwable t) {
        logger.error(errorLog(t));
    }

    public final void logOnCompleted() {
        logger.info(completedLog());
    }

    protected abstract String subscribeLog();

    protected abstract String errorLog(final Throwable t);

    protected abstract String completedLog();
}
