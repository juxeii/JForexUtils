package com.jforex.programming.order.command;

import static com.jforex.programming.order.OrderStaticUtil.isAmountSetTo;
import static com.jforex.programming.order.event.OrderEventTypeData.changeAmountEventTypeData;

import java.util.concurrent.Callable;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.call.OrderCallReason;
import com.jforex.programming.order.event.OrderEventTypeData;

public final class SetAmountCommand implements OrderChangeCommand<Double> {

    private final IOrder orderToChangeAmount;
    private final double currentValue;
    private final double newValue;
    private final String valueName;
    private final Callable<IOrder> callable;

    private static final OrderCallReason callReason = OrderCallReason.CHANGE_AMOUNT;
    private static final OrderEventTypeData orderEventTypeData = changeAmountEventTypeData;

    public SetAmountCommand(final IOrder orderToChangeAmountAmount,
                            final double newAmount) {
        this.orderToChangeAmount = orderToChangeAmountAmount;
        callable = () -> {
            orderToChangeAmountAmount.setRequestedAmount(newAmount);
            return orderToChangeAmountAmount;
        };
        currentValue = orderToChangeAmountAmount.getRequestedAmount();
        newValue = newAmount;
        valueName = "amount";
    }

    @Override
    public final Callable<IOrder> callable() {
        return callable;
    }

    @Override
    public final boolean filter() {
        return !isAmountSetTo(newValue).test(orderToChangeAmount);
    }

    @Override
    public final IOrder order() {
        return orderToChangeAmount;
    }

    @Override
    public final Double currentValue() {
        return currentValue;
    }

    @Override
    public final Double newValue() {
        return newValue;
    }

    @Override
    public final String valueName() {
        return valueName;
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
