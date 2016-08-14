package com.jforex.programming.order.command;

import static com.jforex.programming.order.OrderStaticUtil.isGTTSetTo;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.call.OrderCallReason;
import com.jforex.programming.order.event.OrderEventTypeData;

public final class SetGTTCommand extends OrderChangeCommand<Long> {

    public SetGTTCommand(final IOrder orderToChangeGTT,
                         final long newGTT) {
        super(orderToChangeGTT,
              () -> orderToChangeGTT.setGoodTillTime(newGTT),
              OrderEventTypeData.changeGTTData,
              orderToChangeGTT.getGoodTillTime(),
              newGTT,
              "open price");
    }

    @Override
    public final boolean filter() {
        return !isGTTSetTo(newValue).test(orderToChange);
    }

    @Override
    public OrderCallReason callReason() {
        return OrderCallReason.CHANGE_GTT;
    }
}
