package com.jforex.programming.order.command;

import static com.jforex.programming.order.event.OrderEventTypeData.submitEventTypeData;

import java.util.concurrent.Callable;

import com.dukascopy.api.IEngine;
import com.dukascopy.api.IOrder;
import com.jforex.programming.order.OrderParams;
import com.jforex.programming.order.call.OrderCallReason;
import com.jforex.programming.order.event.OrderEventTypeData;

public final class SubmitCommand implements OrderCallCommand {

    private final Callable<IOrder> callable;

    private static final OrderCallReason callReason = OrderCallReason.SUBMIT;
    private static final OrderEventTypeData orderEventTypeData = submitEventTypeData;

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
    public final OrderEventTypeData orderEventTypeData() {
        return orderEventTypeData;
    }

    @Override
    public final Callable<IOrder> callable() {
        return callable;
    }

    @Override
    public final OrderCallReason callReason() {
        return callReason;
    }
}
