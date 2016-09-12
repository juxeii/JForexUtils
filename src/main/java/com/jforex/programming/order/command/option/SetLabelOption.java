package com.jforex.programming.order.command.option;

import java.util.function.Consumer;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.command.SetLabelCommand;

public interface SetLabelOption extends CommonOption<SetLabelOption> {

    public SetLabelOption doOnSetLabelReject(Consumer<IOrder> rejectAction);

    public SetLabelOption doOnSetLabel(Consumer<IOrder> doneAction);

    public SetLabelCommand build();
}