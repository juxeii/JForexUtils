package com.jforex.programming.order.process.option;

import java.util.function.Consumer;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.process.ClosePositionProcess;

public interface ClosePositionOption extends CommonOption<ClosePositionOption> {

    public ClosePositionOption onCloseReject(Consumer<IOrder> closeRejectAction);

    public ClosePositionOption onClose(Consumer<IOrder> closedAction);

    public ClosePositionOption onPartialClose(Consumer<IOrder> partialClosedAction);

    public ClosePositionProcess build();
}
