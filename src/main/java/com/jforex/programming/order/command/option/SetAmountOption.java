package com.jforex.programming.order.command.option;

import java.util.function.Consumer;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.command.SetAmountCommand;

public interface SetAmountOption extends CommonOption<SetAmountOption> {

    public SetAmountOption doOnSetAmountReject(Consumer<IOrder> rejectAction);

    public SetAmountOption doOnSetAmount(Consumer<IOrder> doneAction);

    public SetAmountCommand build();
}
