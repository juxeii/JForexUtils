package com.jforex.programming.order.process.option;

import java.util.function.Consumer;

import com.dukascopy.api.IOrder;

public interface AmountOption extends CommonOption {

    public AmountOption onAmountReject(Consumer<IOrder> rejectAction);

    public AmountOption onAmountChange(Consumer<IOrder> doneAction);
}
