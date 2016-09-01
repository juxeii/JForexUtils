package com.jforex.programming.order.process.option;

import java.util.function.Consumer;

import com.dukascopy.api.IOrder;

public interface OpenPriceOption<T extends OpenPriceOption<T>> extends CommonOption<T> {

    public T onOpenPriceReject(Consumer<IOrder> rejectAction);

    public T onOpenPriceChange(Consumer<IOrder> doneAction);
}
