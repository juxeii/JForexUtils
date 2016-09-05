package com.jforex.programming.order.process.option;

import java.util.function.Consumer;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.command.SubmitCommand;

public interface SubmitOption extends CommonOption<SubmitOption> {

    public SubmitOption doOnSubmitReject(Consumer<IOrder> submitRejectAction);

    public SubmitOption doOnFillReject(Consumer<IOrder> fillRejectAction);

    public SubmitOption doOnSubmit(Consumer<IOrder> submitOKAction);

    public SubmitOption doOnPartialFill(Consumer<IOrder> partialFillAction);

    public SubmitOption doOnFill(Consumer<IOrder> fillAction);

    public SubmitCommand build();
}
