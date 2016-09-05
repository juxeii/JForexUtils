package com.jforex.programming.order.process.option;

import java.util.function.Consumer;

import com.dukascopy.api.IOrder;

public interface OpenPriceOption<T extends OpenPriceOption<T>> extends CommonOption<T> {

    public T doOnSetOpenPriceReject(Consumer<IOrder> rejectAction);

    public T doOnSetOpenPrice(Consumer<IOrder> doneAction);
}
