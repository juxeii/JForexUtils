package com.jforex.programming.order.process.option;

import java.util.function.Consumer;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.command.SetLabelCommand;

public interface LabelOption extends CommonOption<LabelOption> {

    public LabelOption doOnSetLabelReject(Consumer<IOrder> rejectAction);

    public LabelOption doOnSetLabel(Consumer<IOrder> doneAction);

    public SetLabelCommand build();
}