package com.jforex.programming.order.process.option;

import java.util.function.Consumer;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.command.SetTPCommand;

public interface TPOption extends CommonOption<TPOption> {

    public TPOption doOnSetTPReject(Consumer<IOrder> rejectAction);

    public TPOption doOnSetTP(Consumer<IOrder> doneAction);

    public SetTPCommand build();
}