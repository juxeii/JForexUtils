package com.jforex.programming.order.process.option;

import java.util.function.Consumer;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.process.CloseProcess;

public interface CloseOption extends CommonOption<CloseOption> {

    public CloseOption onCloseReject(Consumer<IOrder> closeRejectAction);

    public CloseOption onClose(Consumer<IOrder> closedAction);

    public CloseOption onPartialClose(Consumer<IOrder> partialClosedAction);

    public CloseProcess build();
}
