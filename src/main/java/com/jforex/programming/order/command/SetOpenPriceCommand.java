package com.jforex.programming.order.command;

import static com.jforex.programming.order.OrderStaticUtil.isOpenPriceSetTo;
import static com.jforex.programming.order.event.OrderEventType.CHANGED_PRICE;
import static com.jforex.programming.order.event.OrderEventType.CHANGE_PRICE_REJECTED;
import static com.jforex.programming.order.event.OrderEventType.NOTIFICATION;

import com.dukascopy.api.IOrder;
import com.google.common.collect.Sets;
import com.jforex.programming.order.call.OrderCallReason;

public final class SetOpenPriceCommand extends OrderChangeCommand<Double> {

    public SetOpenPriceCommand(final IOrder orderToChangeOpenPrice,
                               final double newOpenPrice) {
        super(orderToChangeOpenPrice,
              () -> orderToChangeOpenPrice.setOpenPrice(newOpenPrice),
              orderToChangeOpenPrice.getOpenPrice(),
              newOpenPrice,
              "open price");
    }

    @Override
    protected void initEventTypes() {
        doneEventTypes =
                Sets.immutableEnumSet(CHANGED_PRICE);
        rejectEventTypes =
                Sets.immutableEnumSet(CHANGE_PRICE_REJECTED);
        infoEventTypes =
                Sets.immutableEnumSet(NOTIFICATION);
    }

    @Override
    public final boolean filter() {
        return !isOpenPriceSetTo(newValue).test(orderToChange);
    }

    @Override
    public OrderCallReason callReason() {
        return OrderCallReason.CHANGE_PRICE;
    }
}
