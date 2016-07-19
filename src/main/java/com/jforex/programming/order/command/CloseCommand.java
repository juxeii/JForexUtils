package com.jforex.programming.order.command;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.event.OrderEventTypeData;

public class CloseCommand extends OrderChangeCommand<IOrder.State> {

    public CloseCommand(final IOrder orderToClose) {
        super(orderToClose,
              () -> orderToClose.close(),
              OrderEventTypeData.closeData,
              orderToClose.getState(),
              IOrder.State.CLOSED,
              "order state");
    }
}
