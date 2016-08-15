package com.jforex.programming.order.command;

import static com.jforex.programming.order.OrderStaticUtil.isClosed;
import static com.jforex.programming.order.event.OrderEventType.CLOSE_OK;
import static com.jforex.programming.order.event.OrderEventType.CLOSE_REJECTED;
import static com.jforex.programming.order.event.OrderEventType.NOTIFICATION;
import static com.jforex.programming.order.event.OrderEventType.PARTIAL_CLOSE_OK;

import java.util.EnumSet;
import java.util.concurrent.Callable;
import java.util.function.BooleanSupplier;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.call.OrderCallReason;
import com.jforex.programming.order.event.OrderEventTypeData;

public final class CloseCommandData implements OrderChangeCommandData<IOrder.State> {

    private final Callable<IOrder> callable;
    private final BooleanSupplier isValueNotSet;

    private static final OrderEventTypeData orderEventTypeData =
            new OrderEventTypeData(EnumSet.of(CLOSE_OK),
                                   EnumSet.of(CLOSE_REJECTED),
                                   EnumSet.of(NOTIFICATION, PARTIAL_CLOSE_OK));

    public CloseCommandData(final IOrder orderToClose) {
        callable = () -> {
            orderToClose.close();
            return orderToClose;
        };
        isValueNotSet = () -> !isClosed.test(orderToClose);
    }

    @Override
    public final Callable<IOrder> callable() {
        return callable;
    }

    @Override
    public final boolean isValueNotSet() {
        return isValueNotSet.getAsBoolean();
    }

    @Override
    public final OrderCallReason callReason() {
        return OrderCallReason.CLOSE;
    }

    @Override
    public OrderEventTypeData orderEventTypeData() {
        return orderEventTypeData;
    }
}
