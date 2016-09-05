package com.jforex.programming.order.process.option;

import java.util.function.Consumer;

import com.dukascopy.api.IOrder;

public interface SubmitOption<T extends SubmitOption<T>> extends CommonOption<T> {

    public T doOnSubmitReject(Consumer<IOrder> submitRejectAction);

    public T doOnFillReject(Consumer<IOrder> fillRejectAction);

    public T doOnSubmit(Consumer<IOrder> submitOKAction);

    public T doOnPartialFill(Consumer<IOrder> partialFillAction);

    public T doOnFill(Consumer<IOrder> fillAction);
}
