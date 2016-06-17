package com.jforex.programming.order.event;

import static com.jforex.programming.order.OrderStaticUtil.isClosed;
import static com.jforex.programming.order.OrderStaticUtil.isConditional;
import static com.jforex.programming.order.OrderStaticUtil.isFilled;

import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Function;

import com.dukascopy.api.IMessage;
import com.dukascopy.api.IMessage.Reason;
import com.dukascopy.api.IOrder;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.jforex.programming.order.OrderMessageData;
import com.jforex.programming.order.call.OrderCallReason;
import com.jforex.programming.order.call.OrderCallRequest;

public final class OrderEventTypeEvaluator {

    private final static Queue<OrderCallRequest> changeRequestQueue = new ConcurrentLinkedQueue<>();

    private final static Function<IOrder, OrderEventType> submitEvaluator =
            order -> isConditional.test(order)
                    ? OrderEventType.SUBMIT_CONDITIONAL_OK
                    : OrderEventType.SUBMIT_OK;

    private final static Function<IOrder, OrderEventType> closeEvaluator =
            order -> isFilled.test(order)
                    ? OrderEventType.PARTIAL_CLOSE_OK
                    : OrderEventType.CLOSE_OK;

    private final static Function<IOrder, OrderEventType> mergeEvaluator =
            order -> isClosed.test(order)
                    ? OrderEventType.MERGE_CLOSE_OK
                    : OrderEventType.MERGE_OK;

    private final static Function<IOrder, OrderEventType> fillEvaluator =
            order -> order.getAmount() < order.getRequestedAmount()
                    ? OrderEventType.PARTIAL_FILL_OK
                    : OrderEventType.FULL_FILL_OK;

    private final static Map<IMessage.Type, Function<IOrder, OrderEventType>> orderEventByType =
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
                         order -> OrderEventType.CHANGE_REJECTED)
                    .put(IMessage.Type.ORDER_CLOSE_OK,
                         closeEvaluator)
                    .put(IMessage.Type.ORDER_CLOSE_REJECTED,
                         order -> OrderEventType.CLOSE_REJECTED)
                    .put(IMessage.Type.ORDERS_MERGE_OK,
                         mergeEvaluator)
                    .put(IMessage.Type.ORDERS_MERGE_REJECTED,
                         order -> OrderEventType.MERGE_REJECTED)
                    .build());

    private final static Map<IMessage.Reason, OrderEventType> orderEventByReason =
            Maps.immutableEnumMap(ImmutableMap.<IMessage.Reason, OrderEventType> builder()
                    .put(IMessage.Reason.ORDER_FULLY_FILLED,
                         OrderEventType.FULL_FILL_OK)
                    .put(IMessage.Reason.ORDER_CLOSED_BY_MERGE,
                         OrderEventType.CLOSED_BY_MERGE)
                    .put(IMessage.Reason.ORDER_CLOSED_BY_SL,
                         OrderEventType.CLOSED_BY_SL)
                    .put(IMessage.Reason.ORDER_CLOSED_BY_TP,
                         OrderEventType.CLOSED_BY_TP)
                    .put(IMessage.Reason.ORDER_CHANGED_SL,
                         OrderEventType.SL_CHANGE_OK)
                    .put(IMessage.Reason.ORDER_CHANGED_TP,
                         OrderEventType.TP_CHANGE_OK)
                    .put(IMessage.Reason.ORDER_CHANGED_AMOUNT,
                         OrderEventType.REQUESTED_AMOUNT_CHANGE_OK)
                    .put(IMessage.Reason.ORDER_CHANGED_PRICE,
                         OrderEventType.OPENPRICE_CHANGE_OK)
                    .put(IMessage.Reason.ORDER_CHANGED_GTT,
                         OrderEventType.GTT_CHANGE_OK)
                    .put(IMessage.Reason.ORDER_CHANGED_LABEL,
                         OrderEventType.LABEL_CHANGE_OK)
                    .build());

    private final static Map<OrderCallReason, OrderEventType> changeRejectEventByRequest =
            Maps.immutableEnumMap(ImmutableMap.<OrderCallReason, OrderEventType> builder()
                    .put(OrderCallReason.CHANGE_REQUESTED_AMOUNT,
                         OrderEventType.CHANGE_AMOUNT_REJECTED)
                    .put(OrderCallReason.CHANGE_LABEL,
                         OrderEventType.CHANGE_LABEL_REJECTED)
                    .put(OrderCallReason.CHANGE_GTT,
                         OrderEventType.CHANGE_GTT_REJECTED)
                    .put(OrderCallReason.CHANGE_OPEN_PRICE,
                         OrderEventType.CHANGE_OPENPRICE_REJECTED)
                    .put(OrderCallReason.CHANGE_SL,
                         OrderEventType.CHANGE_SL_REJECTED)
                    .put(OrderCallReason.CHANGE_TP,
                         OrderEventType.CHANGE_TP_REJECTED)
                    .build());

    private final static Set<IMessage.Type> changeEventTypes =
            Sets.immutableEnumSet(IMessage.Type.ORDER_CHANGED_OK,
                                  IMessage.Type.ORDER_CHANGED_REJECTED);

    private final static Set<OrderCallReason> changeReasons =
            Sets.immutableEnumSet(OrderCallReason.CHANGE_GTT,
                                  OrderCallReason.CHANGE_LABEL,
                                  OrderCallReason.CHANGE_OPEN_PRICE,
                                  OrderCallReason.CHANGE_REQUESTED_AMOUNT,
                                  OrderCallReason.CHANGE_SL,
                                  OrderCallReason.CHANGE_TP);

    private OrderEventTypeEvaluator() {
    }

    public final static void registerOrderCallRequest(final OrderCallRequest orderCallRequest) {
        if (changeReasons.contains(orderCallRequest.reason()))
            changeRequestQueue.add(orderCallRequest);
    }

    public final static OrderEventType get(final OrderMessageData orderMessageData) {
        return isNotForChangeReason(orderMessageData)
                ? evaluate(orderMessageData)
                : OrderEventTypeEvaluator.getForChangeReason(orderMessageData, changeRequestQueue.poll().reason());
    }

    private final static boolean isNotForChangeReason(final OrderMessageData orderMessageData) {
        return changeRequestQueue.isEmpty()
                || changeRequestQueue.peek().order() != orderMessageData.order()
                || !changeEventTypes.contains(orderMessageData.messageType());
    }

    private final static OrderEventType getForChangeReason(final OrderMessageData orderEventData,
                                                           final OrderCallReason orderCallReason) {
        final OrderEventType orderEventType = evaluate(orderEventData);
        return refineWithCallReason(orderEventType, orderCallReason);
    }

    private final static OrderEventType refineWithCallReason(final OrderEventType orderEventType,
                                                             final OrderCallReason orderCallRequest) {
        return orderEventType == OrderEventType.CHANGE_REJECTED
                ? changeRejectEventByRequest.get(orderCallRequest)
                : orderEventType;
    }

    private final static OrderEventType evaluate(final OrderMessageData orderEventData) {
        return isEventByReason(orderEventData.messageReasons())
                ? eventByReason(orderEventData.messageReasons())
                : eventByType(orderEventData);
    }

    private final static boolean isEventByReason(final Set<Reason> reasons) {
        return reasons.size() == 1;
    }

    private final static OrderEventType eventByType(final OrderMessageData orderEventData) {
        return orderEventByType.get(orderEventData.messageType()).apply(orderEventData.order());
    }

    private final static OrderEventType eventByReason(final Set<Reason> reasons) {
        return orderEventByReason.get(reasons.iterator().next());
    }
}
