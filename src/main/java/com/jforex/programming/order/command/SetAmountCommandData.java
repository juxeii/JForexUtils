package com.jforex.programming.order.command;

import static com.jforex.programming.order.OrderStaticUtil.isAmountSetTo;
import static com.jforex.programming.order.event.OrderEventType.CHANGED_AMOUNT;
import static com.jforex.programming.order.event.OrderEventType.CHANGE_AMOUNT_REJECTED;
import static com.jforex.programming.order.event.OrderEventType.NOTIFICATION;

import java.util.EnumSet;
import java.util.concurrent.Callable;
import java.util.function.BooleanSupplier;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.call.OrderCallReason;
import com.jforex.programming.order.event.OrderEventTypeData;

public final class SetAmountCommandData implements OrderChangeCommandData<Double> {

    private final Callable<IOrder> callable;
    private final BooleanSupplier isValueNotSet;

    private static final OrderEventTypeData orderEventTypeData =
            new OrderEventTypeData(EnumSet.of(CHANGED_AMOUNT),
                                   EnumSet.of(CHANGE_AMOUNT_REJECTED),
                                   EnumSet.of(NOTIFICATION));

    public SetAmountCommandData(final IOrder orderToChangeAmount,
                                final double newAmount) {
        callable = () -> {
            orderToChangeAmount.setRequestedAmount(newAmount);
            return orderToChangeAmount;
        };
        isValueNotSet = () -> !isAmountSetTo(newAmount).test(orderToChangeAmount);
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
        return OrderCallReason.CHANGE_AMOUNT;
    }

    @Override
    public final OrderEventTypeData orderEventTypeData() {
        return orderEventTypeData;
    }
}
