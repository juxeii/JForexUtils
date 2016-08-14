package com.jforex.programming.order.command;

import static com.jforex.programming.order.OrderStaticUtil.isGTTSetTo;
import static com.jforex.programming.order.event.OrderEventTypeData.changeGTTEventTypeData;

import java.util.concurrent.Callable;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.call.OrderCallReason;
import com.jforex.programming.order.event.OrderEventTypeData;

public final class SetGTTCommand implements OrderChangeCommand<Long> {

    private final IOrder orderToChange;
    private final Long newGTT;
    private final Callable<IOrder> callable;

    private static final OrderCallReason callReason = OrderCallReason.CHANGE_GTT;
    private static final OrderEventTypeData orderEventTypeData = changeGTTEventTypeData;

    public SetGTTCommand(final IOrder orderToChangeGTT,
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
    public final boolean filter() {
        return !isGTTSetTo(newGTT).test(orderToChange);
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
