package com.jforex.programming.order.command;

import static com.jforex.programming.order.OrderStaticUtil.isGTTSetTo;
import static com.jforex.programming.order.event.OrderEventType.CHANGED_GTT;
import static com.jforex.programming.order.event.OrderEventType.CHANGE_GTT_REJECTED;
import static com.jforex.programming.order.event.OrderEventType.NOTIFICATION;

import java.util.EnumSet;
import java.util.concurrent.Callable;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.call.OrderCallReason;
import com.jforex.programming.order.event.OrderEventTypeData;

public final class SetGTTCommandData implements OrderChangeCommandData<Long> {

    private final IOrder orderToChange;
    private final Long newGTT;
    private final Callable<IOrder> callable;

    private static final OrderEventTypeData orderEventTypeData =
            new OrderEventTypeData(EnumSet.of(CHANGED_GTT),
                                   EnumSet.of(CHANGE_GTT_REJECTED),
                                   EnumSet.of(NOTIFICATION));

    public SetGTTCommandData(final IOrder orderToChangeGTT,
                             final long newGTT) {
        orderToChange = orderToChangeGTT;
        this.newGTT = newGTT;
        callable = () -> {
            orderToChangeGTT.setGoodTillTime(newGTT);
            return orderToChangeGTT;
        };
    }

    @Override
    public final Callable<IOrder> callable() {
        return callable;
    }

    @Override
    public final boolean isValueNotSet() {
        return !isGTTSetTo(newGTT).test(orderToChange);
    }

    @Override
    public final OrderCallReason callReason() {
        return OrderCallReason.CHANGE_GTT;
    }

    @Override
    public OrderEventTypeData orderEventTypeData() {
        return orderEventTypeData;
    }
}
