package com.jforex.programming.order.process.option;

import java.util.function.Consumer;

import com.dukascopy.api.IOrder;

public interface SLOption<T extends SLOption<T>> extends CommonOption<T> {

    public T doOnSetSLReject(Consumer<IOrder> rejectAction);

    public T doOnSetSL(Consumer<IOrder> doneAction);
}