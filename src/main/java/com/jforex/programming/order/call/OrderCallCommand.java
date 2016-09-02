package com.jforex.programming.order.call;

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
import java.util.concurrent.Callable;

import com.dukascopy.api.IOrder;
import com.google.common.collect.ImmutableMap;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.event.OrderEventTypeData;

public class OrderCallCommand {

    private final Callable<IOrder> callable;
    private final OrderCallReason callReason;
    private final OrderEventTypeData orderEventTypeData;

    private static final OrderEventTypeData submitTypeData =
            new OrderEventTypeData(EnumSet.of(FULLY_FILLED, SUBMIT_CONDITIONAL_OK),
                                   EnumSet.of(FILL_REJECTED, SUBMIT_REJECTED),
                                   EnumSet.of(NOTIFICATION, SUBMIT_OK, PARTIAL_FILL_OK));

    private static final OrderEventTypeData mergeTypeData =
            new OrderEventTypeData(EnumSet.of(MERGE_OK, MERGE_CLOSE_OK),
                                   EnumSet.of(MERGE_REJECTED),
                                   EnumSet.of(NOTIFICATION));

    private static final OrderEventTypeData closeTypeData =
            new OrderEventTypeData(EnumSet.of(CLOSE_OK),
                                   EnumSet.of(CLOSE_REJECTED),
                                   EnumSet.of(NOTIFICATION, PARTIAL_CLOSE_OK));

    private static final OrderEventTypeData changeLabelTypeData =
            new OrderEventTypeData(EnumSet.of(CHANGED_LABEL),
                                   EnumSet.of(CHANGE_LABEL_REJECTED),
                                   EnumSet.of(NOTIFICATION));

    private static final OrderEventTypeData changeGTTTypeData =
            new OrderEventTypeData(EnumSet.of(CHANGED_GTT),
                                   EnumSet.of(CHANGE_GTT_REJECTED),
                                   EnumSet.of(NOTIFICATION));

    private static final OrderEventTypeData changeAmountTypeData =
            new OrderEventTypeData(EnumSet.of(CHANGED_AMOUNT),
                                   EnumSet.of(CHANGE_AMOUNT_REJECTED),
                                   EnumSet.of(NOTIFICATION));

    private static final OrderEventTypeData changeOpenPriceTypeData =
            new OrderEventTypeData(EnumSet.of(CHANGED_PRICE),
                                   EnumSet.of(CHANGE_PRICE_REJECTED),
                                   EnumSet.of(NOTIFICATION));

    private static final OrderEventTypeData changeSLTypeData =
            new OrderEventTypeData(EnumSet.of(CHANGED_SL),
                                   EnumSet.of(CHANGE_SL_REJECTED),
                                   EnumSet.of(NOTIFICATION));

    private static final OrderEventTypeData changeTPTypeData =
            new OrderEventTypeData(EnumSet.of(CHANGED_TP),
                                   EnumSet.of(CHANGE_TP_REJECTED),
                                   EnumSet.of(NOTIFICATION));

    private static final ImmutableMap<OrderCallReason, OrderEventTypeData> typeDataByReason =
            ImmutableMap.<OrderCallReason, OrderEventTypeData> builder()
                .put(OrderCallReason.SUBMIT, submitTypeData)
                .put(OrderCallReason.MERGE, mergeTypeData)
                .put(OrderCallReason.CLOSE, closeTypeData)
                .put(OrderCallReason.CHANGE_LABEL, changeLabelTypeData)
                .put(OrderCallReason.CHANGE_GTT, changeGTTTypeData)
                .put(OrderCallReason.CHANGE_AMOUNT, changeAmountTypeData)
                .put(OrderCallReason.CHANGE_PRICE, changeOpenPriceTypeData)
                .put(OrderCallReason.CHANGE_SL, changeSLTypeData)
                .put(OrderCallReason.CHANGE_TP, changeTPTypeData)
                .build();

    public OrderCallCommand(final Callable<IOrder> callable,
                            final OrderCallReason callReason) {
        this.callable = callable;
        this.callReason = callReason;
        orderEventTypeData = typeDataByReason.get(callReason);
    }

    public final Callable<IOrder> callable() {
        return callable;
    }

    public final OrderCallReason callReason() {
        return callReason;
    }

    public final boolean isEventForCommand(final OrderEvent orderEvent) {
        return orderEventTypeData
            .allEventTypes()
            .contains(orderEvent.type());
    }

    public final boolean isDoneEvent(final OrderEvent orderEvent) {
        return orderEventTypeData
            .doneEventTypes()
            .contains(orderEvent.type());
    }

    public final boolean isRejectEvent(final OrderEvent orderEvent) {
        return orderEventTypeData
            .rejectEventTypes()
            .contains(orderEvent.type());
    }

    public final boolean isFinishEvent(final OrderEvent orderEvent) {
        return isDoneEvent(orderEvent) || isRejectEvent(orderEvent);
    }
}
