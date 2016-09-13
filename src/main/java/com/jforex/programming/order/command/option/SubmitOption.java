package com.jforex.programming.order.command.option;

import java.util.function.Consumer;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.command.SubmitCommand;

public interface SubmitOption extends CommonOption<SubmitOption> {

    public SubmitOption doOnSubmitReject(Consumer<IOrder> submitRejectConsumer);

    public SubmitOption doOnFillReject(Consumer<IOrder> fillRejectConsumer);

    public SubmitOption doOnSubmit(Consumer<IOrder> submitConsumer);

    public SubmitOption doOnPartialFill(Consumer<IOrder> partialFillConsumer);

    public SubmitOption doOnFill(Consumer<IOrder> fillConsumer);

    public SubmitCommand build();
}
