package com.jforex.programming.order.process.option;

import java.util.function.Consumer;

import com.dukascopy.api.IOrder;

public interface TPOption<T extends TPOption<T>> extends CommonOption<T> {

    public T doOnSetTPReject(Consumer<IOrder> rejectAction);

    public T doOnSetTP(Consumer<IOrder> doneAction);
}