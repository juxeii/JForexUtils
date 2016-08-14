package com.jforex.programming.order.command;

import static com.jforex.programming.order.OrderStaticUtil.isOpenPriceSetTo;
import static com.jforex.programming.order.event.OrderEventTypeData.changeOpenPriceEventTypeData;

import java.util.concurrent.Callable;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.call.OrderCallReason;
import com.jforex.programming.order.event.OrderEventTypeData;

public final class SetOpenPriceCommand implements OrderChangeCommand<Double> {

    private final IOrder orderToChangeOpenPrice;
    private final double newOpenPrice;
    private final Callable<IOrder> callable;

    private static final OrderCallReason callReason = OrderCallReason.CHANGE_PRICE;
    private static final OrderEventTypeData orderEventTypeData = changeOpenPriceEventTypeData;

    public SetOpenPriceCommand(final IOrder orderToChangeOpenPrice,
                               final double newOpenPrice) {
        this.orderToChangeOpenPrice = orderToChangeOpenPrice;
        this.newOpenPrice = newOpenPrice;
        callable = () -> {
            orderToChangeOpenPrice.setOpenPrice(newOpenPrice);
            return orderToChangeOpenPrice;
        };
    }

    @Override
    public final Callable<IOrder> callable() {
        return callable;
    }

    @Override
    public final boolean filter() {
        return !isOpenPriceSetTo(newOpenPrice).test(orderToChangeOpenPrice);
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
