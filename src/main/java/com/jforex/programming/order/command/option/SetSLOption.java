package com.jforex.programming.order.command.option;

import java.util.function.Consumer;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.command.SetSLCommand;

public interface SetSLOption extends CommonOption<SetSLOption> {

    public SetSLOption doOnSetSLReject(Consumer<IOrder> rejectConsumer);

    public SetSLOption doOnSetSL(Consumer<IOrder> doneConsumer);

    public SetSLCommand build();
}
