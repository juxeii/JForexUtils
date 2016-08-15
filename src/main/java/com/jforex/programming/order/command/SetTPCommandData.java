package com.jforex.programming.order.command;

import static com.jforex.programming.order.OrderStaticUtil.isTPSetTo;
import static com.jforex.programming.order.event.OrderEventType.CHANGED_TP;
import static com.jforex.programming.order.event.OrderEventType.CHANGE_TP_REJECTED;
import static com.jforex.programming.order.event.OrderEventType.NOTIFICATION;

import java.util.EnumSet;
import java.util.concurrent.Callable;
import java.util.function.BooleanSupplier;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.call.OrderCallReason;
import com.jforex.programming.order.event.OrderEventTypeData;

public final class SetTPCommandData implements OrderChangeCommandData<Double> {

    private final Callable<IOrder> callable;
    private final BooleanSupplier isValueNotSet;

    private static final OrderEventTypeData orderEventTypeData =
            new OrderEventTypeData(EnumSet.of(CHANGED_TP),
                                   EnumSet.of(CHANGE_TP_REJECTED),
                                   EnumSet.of(NOTIFICATION));

    public SetTPCommandData(final IOrder orderToChangeTP,
                            final double newTP) {
        callable = () -> {
            orderToChangeTP.setTakeProfitPrice(newTP);
            return orderToChangeTP;
        };
        isValueNotSet = () -> !isTPSetTo(newTP).test(orderToChangeTP);
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
        return OrderCallReason.CHANGE_TP;
    }

    @Override
    public OrderEventTypeData orderEventTypeData() {
        return orderEventTypeData;
    }
}
