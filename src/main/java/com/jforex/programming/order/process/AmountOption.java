package com.jforex.programming.order.process;

import java.util.function.Consumer;

import com.dukascopy.api.IOrder;

public interface AmountOption extends CommonOption {

    public CommonBuilder onAmountReject(Consumer<IOrder> rejectAction);

    public CommonBuilder onAmountChange(Consumer<IOrder> doneAction);
}
