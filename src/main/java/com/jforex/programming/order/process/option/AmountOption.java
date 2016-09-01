package com.jforex.programming.order.process.option;

import java.util.function.Consumer;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.process.CommonBuilder;

public interface AmountOption extends CommonOption {

    public CommonBuilder onAmountReject(Consumer<IOrder> rejectAction);

    public CommonBuilder onAmountChange(Consumer<IOrder> doneAction);
}
