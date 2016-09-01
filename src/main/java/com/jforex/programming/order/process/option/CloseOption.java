package com.jforex.programming.order.process.option;

import java.util.function.Consumer;

import com.dukascopy.api.IOrder;

public interface CloseOption extends CommonOption {

    public CloseOption onCloseReject(Consumer<IOrder> closeRejectAction);

    public CloseOption onClose(Consumer<IOrder> closedAction);

    public CloseOption onPartialClose(Consumer<IOrder> partialClosedAction);
}
