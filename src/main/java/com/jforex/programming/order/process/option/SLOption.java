package com.jforex.programming.order.process.option;

import java.util.function.Consumer;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.command.SetSLCommand;

public interface SLOption extends CommonOption<SLOption> {

    public SLOption doOnSetSLReject(Consumer<IOrder> rejectAction);

    public SLOption doOnSetSL(Consumer<IOrder> doneAction);

    public SetSLCommand build();
}