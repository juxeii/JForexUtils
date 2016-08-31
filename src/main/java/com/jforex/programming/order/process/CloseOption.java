package com.jforex.programming.order.process;

import java.util.function.Consumer;

import com.dukascopy.api.IOrder;

public interface CloseOption<V extends OrderProcess> extends CommonOption<CloseOption<V>> {

    public CloseOption<V> onCloseReject(Consumer<IOrder> closeRejectAction);

    public CloseOption<V> onClose(Consumer<IOrder> closedAction);

    public CloseOption<V> onPartialClose(Consumer<IOrder> partialClosedAction);

    public V build();
}
