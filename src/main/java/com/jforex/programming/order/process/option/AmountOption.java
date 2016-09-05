package com.jforex.programming.order.process.option;

import java.util.function.Consumer;

import com.dukascopy.api.IOrder;

public interface AmountOption<T extends AmountOption<T>> extends CommonOption<T> {

    public T doOnSetAmountReject(Consumer<IOrder> rejectAction);

    public T doOnSetAmount(Consumer<IOrder> doneAction);
}
