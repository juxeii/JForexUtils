package com.jforex.programming.order.command.option;

import java.util.function.Consumer;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.command.SetLabelCommand;

public interface SetLabelOption extends CommonOption<SetLabelOption> {

    public SetLabelOption doOnSetLabelReject(Consumer<IOrder> rejectConsumer);

    public SetLabelOption doOnSetLabel(Consumer<IOrder> doneConsumer);

    public SetLabelCommand build();
}
