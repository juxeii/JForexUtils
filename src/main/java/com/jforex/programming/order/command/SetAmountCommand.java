package com.jforex.programming.order.command;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.event.OrderEventTypeData;

public class SetAmountCommand extends OrderChangeCommand<Double> {

    public SetAmountCommand(final IOrder orderToChangeAmount,
                            final double newAmount) {
        super(orderToChangeAmount,
              () -> orderToChangeAmount.setRequestedAmount(newAmount),
              OrderEventTypeData.changeAmountData,
              orderToChangeAmount.getRequestedAmount(),
              newAmount,
              "amount");
    }
}
