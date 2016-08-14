package com.jforex.programming.order.command;

import static com.jforex.programming.order.OrderStaticUtil.isAmountSetTo;
import static com.jforex.programming.order.event.OrderEventTypeData.changeAmountEventTypeData;

import java.util.concurrent.Callable;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.call.OrderCallReason;
import com.jforex.programming.order.event.OrderEventTypeData;

public final class SetAmountCommand implements OrderChangeCommand<Double> {

    private final IOrder orderToChangeAmount;
    private final double newAmount;
    private final Callable<IOrder> callable;

    private static final OrderCallReason callReason = OrderCallReason.CHANGE_AMOUNT;
    private static final OrderEventTypeData orderEventTypeData = changeAmountEventTypeData;

    public SetAmountCommand(final IOrder orderToChangeAmountAmount,
                            final double newAmount) {
        this.orderToChangeAmount = orderToChangeAmountAmount;
        this.newAmount = newAmount;
        callable = () -> {
            orderToChangeAmountAmount.setRequestedAmount(newAmount);
            return orderToChangeAmountAmount;
        };
    }

    @Override
    public final Callable<IOrder> callable() {
        return callable;
    }

    @Override
    public final boolean filter() {
        return !isAmountSetTo(newAmount).test(orderToChangeAmount);
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
