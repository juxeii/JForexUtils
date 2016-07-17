package com.jforex.programming.order.command;

import com.jforex.programming.order.event.OrderEventTypeData;

import com.dukascopy.api.IOrder;

public class SetAmountCommand extends OrderChangeCommand<Double> {

    public SetAmountCommand(final IOrder orderToChangeAmount,
                            final double newAmount) {
        super(orderToChangeAmount, () -> orderToChangeAmount.setRequestedAmount(newAmount));

        orderEventTypeData = OrderEventTypeData.changeAmountData;
        currentValue = orderToChangeAmount.getRequestedAmount();
        newValue = newAmount;
        valueName = "amount";
    }
}
