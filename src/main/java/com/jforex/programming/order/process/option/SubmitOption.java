package com.jforex.programming.order.process.option;

import java.util.function.Consumer;

import com.dukascopy.api.IOrder;

public interface SubmitOption extends CommonOption {

    public SubmitOption onSubmitReject(Consumer<IOrder> submitRejectAction);

    public SubmitOption onFillReject(Consumer<IOrder> fillRejectAction);

    public SubmitOption onSubmitOK(Consumer<IOrder> submitOKAction);

    public SubmitOption onPartialFill(Consumer<IOrder> partialFillAction);

    public SubmitOption onFill(Consumer<IOrder> fillAction);
}
