package com.jforex.programming.order.process.option;

import java.util.function.Consumer;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.command.SetTPCommand;

public interface SetTPOption extends CommonOption<SetTPOption> {

    public SetTPOption doOnSetTPReject(Consumer<IOrder> rejectAction);

    public SetTPOption doOnSetTP(Consumer<IOrder> doneAction);

    public SetTPCommand build();
}