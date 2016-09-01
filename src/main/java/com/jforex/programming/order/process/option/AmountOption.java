package com.jforex.programming.order.process.option;

import java.util.function.Consumer;

import com.dukascopy.api.IOrder;

public interface AmountOption<T extends AmountOption<T>> extends CommonOption<T> {

    public T onAmountReject(Consumer<IOrder> rejectAction);

    public T onAmountChange(Consumer<IOrder> doneAction);
}
