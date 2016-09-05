package com.jforex.programming.order.process.option;

import java.util.function.Consumer;

import com.dukascopy.api.IOrder;

public interface CloseOption<T extends CloseOption<T>> extends CommonOption<T> {

    public T doOnCloseReject(Consumer<IOrder> closeRejectAction);

    public T doOnClose(Consumer<IOrder> closedAction);

    public T doOnPartialClose(Consumer<IOrder> partialClosedAction);
}
