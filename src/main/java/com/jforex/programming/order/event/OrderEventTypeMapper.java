package com.jforex.programming.order.event;

import static com.jforex.programming.order.OrderStaticUtil.isClosed;
import static com.jforex.programming.order.OrderStaticUtil.isFilled;

import java.util.Map;
import java.util.function.Function;

import com.dukascopy.api.IMessage;
import com.dukascopy.api.IOrder;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.jforex.programming.order.call.OrderCallReason;

public final class OrderEventTypeMapper {

    private OrderEventTypeMapper() {
    }

    public static final Map<OrderCallReason, OrderEventType> changeDoneByReason =
            Maps.immutableEnumMap(ImmutableMap.<OrderCallReason, OrderEventType> builder()
                .put(OrderCallReason.CHANGE_AMOUNT, OrderEventType.CHANGED_AMOUNT)
                .put(OrderCallReason.CHANGE_LABEL, OrderEventType.CHANGED_LABEL)
                .put(OrderCallReason.CHANGE_GTT, OrderEventType.CHANGED_GTT)
                .put(OrderCallReason.CHANGE_PRICE, OrderEventType.CHANGED_PRICE)
                .put(OrderCallReason.CHANGE_SL, OrderEventType.CHANGED_SL)
                .put(OrderCallReason.CHANGE_TP, OrderEventType.CHANGED_TP)
                .build());

    public static final Map<OrderCallReason, OrderEventType> changeRejectEventByCallReason =
            Maps.immutableEnumMap(ImmutableMap.<OrderCallReason, OrderEventType> builder()
                .put(OrderCallReason.CHANGE_AMOUNT, OrderEventType.CHANGE_AMOUNT_REJECTED)
                .put(OrderCallReason.CHANGE_LABEL, OrderEventType.CHANGE_LABEL_REJECTED)
                .put(OrderCallReason.CHANGE_GTT, OrderEventType.CHANGE_GTT_REJECTED)
                .put(OrderCallReason.CHANGE_PRICE, OrderEventType.CHANGE_PRICE_REJECTED)
                .put(OrderCallReason.CHANGE_SL, OrderEventType.CHANGE_SL_REJECTED)
                .put(OrderCallReason.CHANGE_TP, OrderEventType.CHANGE_TP_REJECTED)
                .build());

    private static final Map<IMessage.Reason, OrderEventType> orderEventByMessageReason =
            Maps.immutableEnumMap(ImmutableMap.<IMessage.Reason, OrderEventType> builder()
                .put(IMessage.Reason.ORDER_FULLY_FILLED, OrderEventType.FULLY_FILLED)
                .put(IMessage.Reason.ORDER_CLOSED_BY_MERGE, OrderEventType.CLOSED_BY_MERGE)
                .put(IMessage.Reason.ORDER_CLOSED_BY_SL, OrderEventType.CLOSED_BY_SL)
                .put(IMessage.Reason.ORDER_CLOSED_BY_TP, OrderEventType.CLOSED_BY_TP)
                .put(IMessage.Reason.ORDER_CHANGED_SL, OrderEventType.CHANGED_SL)
                .put(IMessage.Reason.ORDER_CHANGED_TP, OrderEventType.CHANGED_TP)
                .put(IMessage.Reason.ORDER_CHANGED_AMOUNT, OrderEventType.CHANGED_AMOUNT)
                .put(IMessage.Reason.ORDER_CHANGED_PRICE, OrderEventType.CHANGED_PRICE)
                .put(IMessage.Reason.ORDER_CHANGED_GTT, OrderEventType.CHANGED_GTT)
                .put(IMessage.Reason.ORDER_CHANGED_LABEL, OrderEventType.CHANGED_LABEL)
                .build());

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

    private static final Map<IMessage.Type, Function<IOrder, OrderEventType>> orderEventByMessageType =
            Maps.immutableEnumMap(ImmutableMap.<IMessage.Type, Function<IOrder, OrderEventType>> builder()
                .put(IMessage.Type.NOTIFICATION,
                     order -> OrderEventType.NOTIFICATION)
                .put(IMessage.Type.ORDER_SUBMIT_REJECTED,
                     order -> OrderEventType.SUBMIT_REJECTED)
                .put(IMessage.Type.ORDER_SUBMIT_OK,
                     order -> OrderEventType.SUBMIT_OK)
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

    public static final OrderEventType byMessageType(final IMessage.Type messageType,
                                                     final IOrder order) {
        return orderEventByMessageType
            .get(messageType)
            .apply(order);
    }

    public static final OrderEventType byMessageReason(final IMessage.Reason messageReason) {
        return orderEventByMessageReason.get(messageReason);
    }

    public static final OrderEventType byChangeCallReason(final OrderCallReason orderCallReason) {
        return changeRejectEventByCallReason.get(orderCallReason);
    }
}
