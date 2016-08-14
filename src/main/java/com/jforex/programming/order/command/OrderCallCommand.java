package com.jforex.programming.order.command;

import java.util.Set;
import java.util.concurrent.Callable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dukascopy.api.IOrder;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.jforex.programming.misc.JFRunnable;
import com.jforex.programming.order.OrderStaticUtil;
import com.jforex.programming.order.call.OrderCallReason;
import com.jforex.programming.order.event.OrderEventType;

public abstract class OrderCallCommand {

    protected Callable<IOrder> callable;
    protected OrderCallReason callReason;
    protected ImmutableSet<OrderEventType> doneEventTypes;
    protected ImmutableSet<OrderEventType> rejectEventTypes;
    protected ImmutableSet<OrderEventType> infoEventTypes;
    protected ImmutableSet<OrderEventType> allEventTypes;

    protected static final Logger logger = LogManager.getLogger(OrderCallCommand.class);

    public OrderCallCommand() {
        initAttributes();
        allEventTypes =
                Sets.immutableEnumSet(Sets.union(infoEventTypes,
                                                 Sets.union(doneEventTypes, rejectEventTypes)));
    }

    protected final Callable<IOrder> initCallable(final JFRunnable runnable,
                                                  final IOrder order) {
        return OrderStaticUtil.runnableToCallable(runnable, order);
    }

    protected abstract void initAttributes();

    public final Callable<IOrder> callable() {
        return callable;
    }

    public final OrderCallReason callReason() {
        return callReason;
    }

    public final Set<OrderEventType> allEventTypes() {
        return allEventTypes;
    }

    public final Set<OrderEventType> doneEventTypes() {
        return doneEventTypes;
    }

    public final Set<OrderEventType> rejectEventTypes() {
        return rejectEventTypes;
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
