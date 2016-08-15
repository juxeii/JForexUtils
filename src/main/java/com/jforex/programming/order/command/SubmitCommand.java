package com.jforex.programming.order.command;

import static com.jforex.programming.order.event.OrderEventType.FILL_REJECTED;
import static com.jforex.programming.order.event.OrderEventType.FULLY_FILLED;
import static com.jforex.programming.order.event.OrderEventType.NOTIFICATION;
import static com.jforex.programming.order.event.OrderEventType.PARTIAL_FILL_OK;
import static com.jforex.programming.order.event.OrderEventType.SUBMIT_CONDITIONAL_OK;
import static com.jforex.programming.order.event.OrderEventType.SUBMIT_OK;
import static com.jforex.programming.order.event.OrderEventType.SUBMIT_REJECTED;

import java.util.EnumSet;
import java.util.concurrent.Callable;

import com.dukascopy.api.IEngine;
import com.dukascopy.api.IOrder;
import com.jforex.programming.order.OrderParams;
import com.jforex.programming.order.call.OrderCallReason;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.event.OrderEventTypeData;

public final class SubmitCommand implements OrderCallCommand {

    private final Callable<IOrder> callable;

    private static final OrderCallReason callReason = OrderCallReason.SUBMIT;
    private static final OrderEventTypeData orderEventTypeData =
            new OrderEventTypeData(EnumSet.of(FULLY_FILLED, SUBMIT_CONDITIONAL_OK),
                                   EnumSet.of(FILL_REJECTED, SUBMIT_REJECTED),
                                   EnumSet.of(NOTIFICATION, SUBMIT_OK, PARTIAL_FILL_OK));

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
    }

    @Override
    public final Callable<IOrder> callable() {
        return callable;
    }

    @Override
    public final OrderCallReason callReason() {
        return callReason;
    }

    @Override
    public boolean isEventForCommand(final OrderEvent orderEvent) {
        return orderEventTypeData
            .allEventTypes()
            .contains(orderEvent.type());
    }

    @Override
    public boolean isDoneEvent(final OrderEvent orderEvent) {
        return orderEventTypeData
            .doneEventTypes()
            .contains(orderEvent.type());
    }

    @Override
    public boolean isRejectEvent(final OrderEvent orderEvent) {
        return orderEventTypeData
            .rejectEventTypes()
            .contains(orderEvent.type());
    }
}
