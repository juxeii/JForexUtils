package com.jforex.programming.order.process.option;

import java.util.function.Consumer;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.command.CloseCommand;

public interface CloseOption extends CommonOption<CloseOption> {

    public CloseOption doOnCloseReject(Consumer<IOrder> closeRejectAction);

    public CloseOption doOnClose(Consumer<IOrder> closedAction);

    public CloseOption doOnPartialClose(Consumer<IOrder> partialClosedAction);

    public CloseCommand build();
}
