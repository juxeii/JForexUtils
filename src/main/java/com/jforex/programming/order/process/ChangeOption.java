package com.jforex.programming.order.process;

import java.util.function.Consumer;

import com.dukascopy.api.IOrder;

public interface ChangeOption<V extends OrderProcess> extends CommonOption<ChangeOption<V>> {

    public ChangeOption<V> onReject(Consumer<IOrder> rejectAction);

    public ChangeOption<V> onOK(Consumer<IOrder> okAction);

    public V build();
}
