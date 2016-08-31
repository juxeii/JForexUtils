package com.jforex.programming.order.process;

import java.util.function.Consumer;

import com.dukascopy.api.IOrder;

public interface CloseOption extends CommonOption {

    public CommonBuilder onCloseReject(Consumer<IOrder> closeRejectAction);

    public CommonBuilder onClose(Consumer<IOrder> closedAction);

    public CommonBuilder onPartialClose(Consumer<IOrder> partialClosedAction);
}
