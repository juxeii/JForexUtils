package com.jforex.programming.order.command.option;

import java.util.function.Consumer;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.command.SetTPCommand;

public interface SetTPOption extends CommonOption<SetTPOption> {

    public SetTPOption doOnSetTPReject(Consumer<IOrder> rejectConsumer);

    public SetTPOption doOnSetTP(Consumer<IOrder> doneConsumer);

    public SetTPCommand build();
}
