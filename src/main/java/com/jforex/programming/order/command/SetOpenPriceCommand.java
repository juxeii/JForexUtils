package com.jforex.programming.order.command;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.event.OrderEventTypeData;

public class SetOpenPriceCommand extends OrderChangeCommand<Double> {

    public SetOpenPriceCommand(final IOrder orderToChangeOpenPrice,
                               final double newOpenPrice) {
        super(orderToChangeOpenPrice,
              () -> orderToChangeOpenPrice.setOpenPrice(newOpenPrice),
              OrderEventTypeData.changeOpenPriceData,
              orderToChangeOpenPrice.getOpenPrice(),
              newOpenPrice,
              "open price");
    }
}
