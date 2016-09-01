package com.jforex.programming.order.process.option;

import java.util.function.Consumer;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.process.CommonBuilder;

public interface CloseOption extends CommonOption {

    public CommonBuilder onCloseReject(Consumer<IOrder> closeRejectAction);

    public CommonBuilder onClose(Consumer<IOrder> closedAction);

    public CommonBuilder onPartialClose(Consumer<IOrder> partialClosedAction);
}
