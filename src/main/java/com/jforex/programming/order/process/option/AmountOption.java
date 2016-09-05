package com.jforex.programming.order.process.option;

import java.util.function.Consumer;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.command.SetAmountCommand;

public interface AmountOption extends CommonOption<AmountOption> {

    public AmountOption doOnSetAmountReject(Consumer<IOrder> rejectAction);

    public AmountOption doOnSetAmount(Consumer<IOrder> doneAction);

    public SetAmountCommand build();
}
