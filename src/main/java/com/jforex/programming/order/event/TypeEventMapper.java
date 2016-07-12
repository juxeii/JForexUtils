package com.jforex.programming.order.event;

import static com.jforex.programming.order.OrderStaticUtil.isClosed;
import static com.jforex.programming.order.OrderStaticUtil.isConditional;
import static com.jforex.programming.order.OrderStaticUtil.isFilled;

import java.util.Map;
import java.util.function.Function;

import com.dukascopy.api.IMessage;
import com.dukascopy.api.IOrder;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

public final class TypeEventMapper {

    private TypeEventMapper() {
    }

    private static final Function<IOrder, OrderEventType> submitEvaluator =
            order -> isConditional.test(order)
                    ? OrderEventType.SUBMIT_CONDITIONAL_OK
                    : OrderEventType.SUBMIT_OK;

    private static final Function<IOrder, OrderEventType> closeEvaluator =
            order -> isFilled.test(order)
                    ? OrderEventType.PARTIAL_CLOSE_OK
                    : OrderEventType.CLOSE_OK;

    private static final Function<IOrder, OrderEventType> mergeEvaluator =
            order -> isClosed.test(order)
                    ? OrderEventType.MERGE_CLOSE_OK
                    : OrderEventType.MERGE_OK;

    private static final Function<IOrder, OrderEventType> fillEvaluator =
            order -> order.getAmount() < order.getRequestedAmount()
                    ? OrderEventType.PARTIAL_FILL_OK
                    : OrderEventType.FULLY_FILLED;

    private static final Map<IMessage.Type, Function<IOrder, OrderEventType>> orderEventByType =
            Maps.immutableEnumMap(ImmutableMap.<IMessage.Type, Function<IOrder, OrderEventType>> builder()
                    .put(IMessage.Type.NOTIFICATION,
                         order -> OrderEventType.NOTIFICATION)
                    .put(IMessage.Type.ORDER_SUBMIT_REJECTED,
                         order -> OrderEventType.SUBMIT_REJECTED)
                    .put(IMessage.Type.ORDER_SUBMIT_OK,
                         submitEvaluator)
                    .put(IMessage.Type.ORDER_FILL_REJECTED,
                         order -> OrderEventType.FILL_REJECTED)
                    .put(IMessage.Type.ORDER_FILL_OK,
                         fillEvaluator)
                    .put(IMessage.Type.ORDER_CHANGED_OK,
                         order -> OrderEventType.PARTIAL_FILL_OK)
                    .put(IMessage.Type.ORDER_CHANGED_REJECTED,
                         order -> OrderEventType.CHANGED_REJECTED)
                    .put(IMessage.Type.ORDER_CLOSE_OK,
                         closeEvaluator)
                    .put(IMessage.Type.ORDER_CLOSE_REJECTED,
                         order -> OrderEventType.CLOSE_REJECTED)
                    .put(IMessage.Type.ORDERS_MERGE_OK,
                         mergeEvaluator)
                    .put(IMessage.Type.ORDERS_MERGE_REJECTED,
                         order -> OrderEventType.MERGE_REJECTED)
                    .build());

    public static final OrderEventType map(final IOrder order,
                                           final IMessage.Type type) {
        return orderEventByType.get(type).apply(order);
    }
}
