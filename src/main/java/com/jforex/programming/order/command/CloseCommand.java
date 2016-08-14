package com.jforex.programming.order.command;

import static com.jforex.programming.order.OrderStaticUtil.isClosed;
import static com.jforex.programming.order.event.OrderEventTypeData.closeEventTypeData;

import java.util.concurrent.Callable;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.call.OrderCallReason;
import com.jforex.programming.order.event.OrderEventTypeData;

public final class CloseCommand implements OrderChangeCommand<IOrder.State> {

    private final IOrder orderToClose;
    private final Callable<IOrder> callable;

    private static final OrderCallReason callReason = OrderCallReason.CLOSE;
    private static final OrderEventTypeData orderEventTypeData = closeEventTypeData;

    public CloseCommand(final IOrder orderToClose) {
        this.orderToClose = orderToClose;
        callable = () -> {
            orderToClose.close();
            return orderToClose;
        };
    }

    @Override
    public final Callable<IOrder> callable() {
        return callable;
    }

    @Override
    public final boolean filter() {
        return !isClosed.test(orderToClose);
    }

    @Override
    public final OrderEventTypeData orderEventTypeData() {
        return orderEventTypeData;
    }

    @Override
    public final OrderCallReason callReason() {
        return callReason;
    }
}
