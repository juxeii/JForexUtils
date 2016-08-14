package com.jforex.programming.order.command;

import static com.jforex.programming.order.OrderStaticUtil.isOpenPriceSetTo;
import static com.jforex.programming.order.event.OrderEventType.CHANGED_PRICE;
import static com.jforex.programming.order.event.OrderEventType.NOTIFICATION;

import com.dukascopy.api.IOrder;
import com.google.common.collect.Sets;
import com.jforex.programming.order.call.OrderCallReason;
import com.jforex.programming.order.event.OrderEventTypeData;

public final class SetOpenPriceCommand extends OrderChangeCommand<Double> {

    public SetOpenPriceCommand(final IOrder orderToChangeOpenPrice,
                               final double newOpenPrice) {
        super(orderToChangeOpenPrice,
              () -> orderToChangeOpenPrice.setOpenPrice(newOpenPrice),
              OrderEventTypeData.changeOpenPriceData,
              orderToChangeOpenPrice.getOpenPrice(),
              newOpenPrice,
              "open price");
    }

    @Override
    public final boolean filter() {
        return !isOpenPriceSetTo(newValue).test(orderToChange);
    }

    @Override
    public OrderCallReason callReason() {
        return OrderCallReason.CHANGE_PRICE;
    }

    @Override
    protected void initDoneEvents() {
        doneEventTypes = Sets.immutableEnumSet(CHANGED_PRICE);
    }

    @Override
    protected void initRejectEvents() {
        rejectEventTypes = Sets.immutableEnumSet(CHANGED_PRICE);
    }

    @Override
    protected void initInfoEvents() {
        infoEventTypes = Sets.immutableEnumSet(NOTIFICATION);
    }
}
