package com.jforex.programming.order;

import static com.jforex.programming.order.event.OrderEventType.CHANGED_AMOUNT;
import static com.jforex.programming.order.event.OrderEventType.CHANGED_GTT;
import static com.jforex.programming.order.event.OrderEventType.CHANGED_LABEL;
import static com.jforex.programming.order.event.OrderEventType.CHANGED_PRICE;
import static com.jforex.programming.order.event.OrderEventType.CHANGED_SL;
import static com.jforex.programming.order.event.OrderEventType.CHANGED_TP;
import static com.jforex.programming.order.event.OrderEventType.CHANGE_AMOUNT_REJECTED;
import static com.jforex.programming.order.event.OrderEventType.CHANGE_GTT_REJECTED;
import static com.jforex.programming.order.event.OrderEventType.CHANGE_LABEL_REJECTED;
import static com.jforex.programming.order.event.OrderEventType.CHANGE_PRICE_REJECTED;
import static com.jforex.programming.order.event.OrderEventType.CHANGE_SL_REJECTED;
import static com.jforex.programming.order.event.OrderEventType.CHANGE_TP_REJECTED;
import static com.jforex.programming.order.event.OrderEventType.CLOSE_OK;
import static com.jforex.programming.order.event.OrderEventType.CLOSE_REJECTED;
import static com.jforex.programming.order.event.OrderEventType.FILL_REJECTED;
import static com.jforex.programming.order.event.OrderEventType.FULLY_FILLED;
import static com.jforex.programming.order.event.OrderEventType.MERGE_CLOSE_OK;
import static com.jforex.programming.order.event.OrderEventType.MERGE_OK;
import static com.jforex.programming.order.event.OrderEventType.MERGE_REJECTED;
import static com.jforex.programming.order.event.OrderEventType.NOTIFICATION;
import static com.jforex.programming.order.event.OrderEventType.PARTIAL_CLOSE_OK;
import static com.jforex.programming.order.event.OrderEventType.PARTIAL_FILL_OK;
import static com.jforex.programming.order.event.OrderEventType.SUBMIT_CONDITIONAL_OK;
import static com.jforex.programming.order.event.OrderEventType.SUBMIT_OK;
import static com.jforex.programming.order.event.OrderEventType.SUBMIT_REJECTED;

import java.util.EnumSet;
import java.util.Map;

import com.dukascopy.api.IOrder;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.jforex.programming.order.call.OrderCallReason;
import com.jforex.programming.order.event.OrderEventTypeData;

public class OrderTaskDataFactory {

    private final Map<OrderCallReason, Function<IOrder, OrderTaskData>> changeDoneByReason;

    public OrderTaskDataFactory() {
        changeDoneByReason =
                Maps.immutableEnumMap(ImmutableMap.<OrderCallReason, Function<IOrder, OrderTaskData>> builder()
                    .put(OrderCallReason.SUBMIT, this::forSubmit)
                    .put(OrderCallReason.MERGE, this::forMerge)
                    .put(OrderCallReason.CLOSE, this::forClose)
                    .put(OrderCallReason.CHANGE_LABEL, this::forSetLabel)
                    .put(OrderCallReason.CHANGE_GTT, this::forSetGoodTillTime)
                    .put(OrderCallReason.CHANGE_AMOUNT, this::forSetRequestedAmount)
                    .put(OrderCallReason.CHANGE_PRICE, this::forSetOpenPrice)
                    .put(OrderCallReason.CHANGE_SL, this::forSetStopLossPrice)
                    .put(OrderCallReason.CHANGE_TP, this::forSetTakeProfitPrice)
                    .build());
    }

    public OrderTaskData forCallReason(final IOrder order,
                                       final OrderCallReason callReason) {
        return changeDoneByReason.get(callReason).apply(order);
    }

    public OrderTaskData forSubmit(final IOrder order) {
        final OrderEventTypeData typeData =
                new OrderEventTypeData(EnumSet.of(FULLY_FILLED, SUBMIT_CONDITIONAL_OK),
                                       EnumSet.of(FILL_REJECTED, SUBMIT_REJECTED),
                                       EnumSet.of(NOTIFICATION, SUBMIT_OK, PARTIAL_FILL_OK));
        return new OrderTaskData(order,
                                 OrderCallReason.SUBMIT,
                                 typeData);
    }

    public OrderTaskData forMerge(final IOrder order) {
        final OrderEventTypeData typeData =
                new OrderEventTypeData(EnumSet.of(MERGE_OK, MERGE_CLOSE_OK),
                                       EnumSet.of(MERGE_REJECTED),
                                       EnumSet.of(NOTIFICATION));
        return new OrderTaskData(order,
                                 OrderCallReason.MERGE,
                                 typeData);
    }

    public OrderTaskData forClose(final IOrder order) {
        final OrderEventTypeData typeData =
                new OrderEventTypeData(EnumSet.of(CLOSE_OK),
                                       EnumSet.of(CLOSE_REJECTED),
                                       EnumSet.of(NOTIFICATION, PARTIAL_CLOSE_OK));
        return new OrderTaskData(order,
                                 OrderCallReason.CLOSE,
                                 typeData);
    }

    public OrderTaskData forSetLabel(final IOrder order) {
        final OrderEventTypeData typeData =
                new OrderEventTypeData(EnumSet.of(CHANGED_LABEL),
                                       EnumSet.of(CHANGE_LABEL_REJECTED),
                                       EnumSet.of(NOTIFICATION));
        return new OrderTaskData(order,
                                 OrderCallReason.CHANGE_LABEL,
                                 typeData);
    }

    public OrderTaskData forSetGoodTillTime(final IOrder order) {
        final OrderEventTypeData typeData =
                new OrderEventTypeData(EnumSet.of(CHANGED_GTT),
                                       EnumSet.of(CHANGE_GTT_REJECTED),
                                       EnumSet.of(NOTIFICATION));
        return new OrderTaskData(order,
                                 OrderCallReason.CHANGE_GTT,
                                 typeData);
    }

    public OrderTaskData forSetRequestedAmount(final IOrder order) {
        final OrderEventTypeData typeData =
                new OrderEventTypeData(EnumSet.of(CHANGED_AMOUNT),
                                       EnumSet.of(CHANGE_AMOUNT_REJECTED),
                                       EnumSet.of(NOTIFICATION));
        return new OrderTaskData(order,
                                 OrderCallReason.CHANGE_AMOUNT,
                                 typeData);
    }

    public OrderTaskData forSetOpenPrice(final IOrder order) {
        final OrderEventTypeData typeData =
                new OrderEventTypeData(EnumSet.of(CHANGED_PRICE),
                                       EnumSet.of(CHANGE_PRICE_REJECTED),
                                       EnumSet.of(NOTIFICATION));
        return new OrderTaskData(order,
                                 OrderCallReason.CHANGE_PRICE,
                                 typeData);
    }

    public OrderTaskData forSetStopLossPrice(final IOrder order) {
        final OrderEventTypeData typeData =
                new OrderEventTypeData(EnumSet.of(CHANGED_SL),
                                       EnumSet.of(CHANGE_SL_REJECTED),
                                       EnumSet.of(NOTIFICATION));
        return new OrderTaskData(order,
                                 OrderCallReason.CHANGE_SL,
                                 typeData);
    }

    public OrderTaskData forSetTakeProfitPrice(final IOrder order) {
        final OrderEventTypeData typeData =
                new OrderEventTypeData(EnumSet.of(CHANGED_TP),
                                       EnumSet.of(CHANGE_TP_REJECTED),
                                       EnumSet.of(NOTIFICATION));
        return new OrderTaskData(order,
                                 OrderCallReason.CHANGE_TP,
                                 typeData);
    }
}
