package com.jforex.programming.order.process.option;

import java.util.function.Consumer;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.process.SetAmountProcess;

public interface AmountOption extends CommonOption<AmountOption> {

    public AmountOption onAmountReject(Consumer<IOrder> rejectAction);

    public AmountOption onAmountChange(Consumer<IOrder> doneAction);

    public SetAmountProcess build();
}
