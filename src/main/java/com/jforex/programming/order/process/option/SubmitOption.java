package com.jforex.programming.order.process.option;

import java.util.function.Consumer;

import com.dukascopy.api.IOrder;

public interface SubmitOption<T extends SubmitOption<T>> extends CommonOption<T> {

    public T onSubmitReject(Consumer<IOrder> submitRejectAction);

    public T onFillReject(Consumer<IOrder> fillRejectAction);

    public T onSubmitOK(Consumer<IOrder> submitOKAction);

    public T onPartialFill(Consumer<IOrder> partialFillAction);

    public T onFill(Consumer<IOrder> fillAction);
}
