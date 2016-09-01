package com.jforex.programming.order.process.option;

import java.util.function.Consumer;

import com.dukascopy.api.IOrder;

public interface LabelOption<T extends LabelOption<T>> extends CommonOption<T> {

    public T onLabelReject(Consumer<IOrder> rejectAction);

    public T onLabelChange(Consumer<IOrder> doneAction);
}