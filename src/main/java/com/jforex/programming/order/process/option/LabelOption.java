package com.jforex.programming.order.process.option;

import java.util.function.Consumer;

import com.dukascopy.api.IOrder;

public interface LabelOption extends CommonOption {

    public LabelOption onLabelReject(Consumer<IOrder> rejectAction);

    public LabelOption onLabelChange(Consumer<IOrder> doneAction);
}