package com.jforex.programming.order;

import java.util.function.Function;

import com.dukascopy.api.IOrder;

public interface OrderToEventTransformer extends
                                         Function<IOrder, OrderEventTransformer> {
}
