package com.jforex.programming.order.process.option;

import java.util.function.Consumer;

import com.dukascopy.api.IOrder;

public interface CloseOption<T extends CloseOption<T>> extends CommonOption<T> {

    public T onCloseReject(Consumer<IOrder> closeRejectAction);

    public T onClose(Consumer<IOrder> closedAction);

    public T onPartialClose(Consumer<IOrder> partialClosedAction);
}
