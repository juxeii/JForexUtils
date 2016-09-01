package com.jforex.programming.order.process.option;

import java.util.function.Consumer;

import com.dukascopy.api.IOrder;

public interface TPOption<T extends TPOption<T>> extends CommonOption<T> {

    public T onTPReject(Consumer<IOrder> rejectAction);

    public T onTPChange(Consumer<IOrder> doneAction);
}