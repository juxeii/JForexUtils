package com.jforex.programming.order.command.option;

import java.util.function.Consumer;

import com.dukascopy.api.IOrder;

public interface ChangeOption extends CommonOption<ChangeOption> {

    public ChangeOption doOnReject(Consumer<IOrder> rejectConsumer);

    public ChangeOption doOnChange(Consumer<IOrder> changeConsumer);
}
