package com.jforex.programming.order.command;

import static com.jforex.programming.order.OrderStaticUtil.isOpenPriceSetTo;
import static com.jforex.programming.order.event.OrderEventType.CHANGED_PRICE;
import static com.jforex.programming.order.event.OrderEventType.CHANGE_PRICE_REJECTED;
import static com.jforex.programming.order.event.OrderEventType.NOTIFICATION;

import java.util.EnumSet;
import java.util.concurrent.Callable;
import java.util.function.BooleanSupplier;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.call.OrderCallReason;
import com.jforex.programming.order.event.OrderEventTypeData;

public final class SetOpenPriceCommandData implements OrderChangeCommandData<Double> {

    private final Callable<IOrder> callable;
    private final BooleanSupplier isValueNotSet;

    private static final OrderEventTypeData orderEventTypeData =
            new OrderEventTypeData(EnumSet.of(CHANGED_PRICE),
                                   EnumSet.of(CHANGE_PRICE_REJECTED),
                                   EnumSet.of(NOTIFICATION));

    public SetOpenPriceCommandData(final IOrder orderToChangeOpenPrice,
                                   final double newOpenPrice) {
        callable = () -> {
            orderToChangeOpenPrice.setOpenPrice(newOpenPrice);
            return orderToChangeOpenPrice;
        };
        isValueNotSet = () -> !isOpenPriceSetTo(newOpenPrice).test(orderToChangeOpenPrice);
    }

    @Override
    public final Callable<IOrder> callable() {
        return callable;
    }

    @Override
    public final boolean isValueNotSet() {
        return isValueNotSet.getAsBoolean();
    }

    @Override
    public final OrderCallReason callReason() {
        return OrderCallReason.CHANGE_PRICE;
    }

    @Override
    public OrderEventTypeData orderEventTypeData() {
        return orderEventTypeData;
    }
}
