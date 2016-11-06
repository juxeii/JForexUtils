package com.jforex.programming.order.task.params;

import java.util.function.Consumer;

import com.dukascopy.api.IOrder;

public interface OrderConsumer extends Consumer<IOrder> {
}
