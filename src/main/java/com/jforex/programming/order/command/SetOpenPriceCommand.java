package com.jforex.programming.order.command;

import static com.jforex.programming.order.OrderStaticUtil.isOpenPriceSetTo;
import static com.jforex.programming.order.event.OrderEventTypeData.changeOpenPriceEventTypeData;

import java.util.concurrent.Callable;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.call.OrderCallReason;
import com.jforex.programming.order.event.OrderEventTypeData;

public final class SetOpenPriceCommand implements OrderChangeCommand<Double> {

    private IOrder orderToChangeOpenPrice;
    private double currentValue;
    private double newValue;
    private String valueName;
    private final Callable<IOrder> callable;

    private static final OrderCallReason callReason = OrderCallReason.CHANGE_PRICE;
    private static final OrderEventTypeData orderEventTypeData = changeOpenPriceEventTypeData;

    public SetOpenPriceCommand(final IOrder orderToChangeOpenPrice,
                               final double newOpenPrice) {
        this.orderToChangeOpenPrice = orderToChangeOpenPrice;
        callable = () -> {
            orderToChangeOpenPrice.setOpenPrice(newOpenPrice);
            return orderToChangeOpenPrice;
        };
        currentValue = orderToChangeOpenPrice.getOpenPrice();
        newValue = newOpenPrice;
        valueName = "open price";
    }

    @Override
    public OrderEventTypeData orderEventTypeData() {
        return orderEventTypeData;
    }

    @Override
    public Callable<IOrder> callable() {
        return callable;
    }

    @Override
    public OrderCallReason callReason() {
        return callReason;
    }

    @Override
    public final boolean filter() {
        return !isOpenPriceSetTo(newValue).test(orderToChangeOpenPrice);
    }

    @Override
    public IOrder order() {
        return orderToChangeOpenPrice;
    }

    @Override
    public Double currentValue() {
        return currentValue;
    }

    @Override
    public Double newValue() {
        return newValue;
    }

    @Override
    public String valueName() {
        return valueName;
    }
}
