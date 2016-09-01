package com.jforex.programming.order.process.option;

import java.util.function.Consumer;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.process.SetLabelProcess;

public interface LabelOption extends CommonOption<LabelOption> {

    public LabelOption onLabelReject(Consumer<IOrder> rejectAction);

    public LabelOption onLabelChange(Consumer<IOrder> doneAction);

    public SetLabelProcess build();
}