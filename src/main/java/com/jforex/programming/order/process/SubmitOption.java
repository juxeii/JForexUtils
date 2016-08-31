package com.jforex.programming.order.process;

import java.util.function.Consumer;

import com.dukascopy.api.IOrder;

public interface SubmitOption extends CommonOption {

    public CommonBuilder onSubmitReject(Consumer<IOrder> submitRejectAction);

    public CommonBuilder onFillReject(Consumer<IOrder> fillRejectAction);

    public CommonBuilder onSubmitOK(Consumer<IOrder> submitOKAction);

    public CommonBuilder onPartialFill(Consumer<IOrder> partialFillAction);

    public CommonBuilder onFill(Consumer<IOrder> fillAction);
}
