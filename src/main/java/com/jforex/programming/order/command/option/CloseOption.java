package com.jforex.programming.order.command.option;

import java.util.function.Consumer;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.command.Command;

public interface CloseOption extends CommonOption<CloseOption> {

    public CloseOption doOnCloseReject(Consumer<IOrder> rejectConsumer);

    public CloseOption doOnClose(Consumer<IOrder> closeConsumer);

    public CloseOption doOnPartialClose(Consumer<IOrder> partialCloseConsumer);

    public Command build();
}
