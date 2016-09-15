package com.jforex.programming.order;

import java.util.Map;

import com.dukascopy.api.IOrder;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.jforex.programming.order.call.OrderCallReason;
import com.jforex.programming.order.call.OrderCallRequest;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.event.OrderEventGateway;

import io.reactivex.Observable;

public class OrderUtilHandler {

    private final OrderEventGateway orderEventGateway;
    private final Map<OrderCallReason, Function<IOrder, OrderTaskData>> changeDoneByReason;

    public OrderUtilHandler(final OrderEventGateway orderEventGateway,
                            final OrderTaskDataFactory orderTaskDataProvider) {
        this.orderEventGateway = orderEventGateway;

        changeDoneByReason =
                Maps.immutableEnumMap(ImmutableMap.<OrderCallReason, Function<IOrder, OrderTaskData>> builder()
                    .put(OrderCallReason.SUBMIT, orderTaskDataProvider::forSubmit)
                    .put(OrderCallReason.MERGE, orderTaskDataProvider::forMerge)
                    .put(OrderCallReason.CLOSE, orderTaskDataProvider::forClose)
                    .put(OrderCallReason.CHANGE_LABEL, orderTaskDataProvider::forSetLabel)
                    .put(OrderCallReason.CHANGE_GTT, orderTaskDataProvider::forSetGoodTillTime)
                    .put(OrderCallReason.CHANGE_AMOUNT, orderTaskDataProvider::forSetRequestedAmount)
                    .put(OrderCallReason.CHANGE_PRICE, orderTaskDataProvider::forSetOpenPrice)
                    .put(OrderCallReason.CHANGE_SL, orderTaskDataProvider::forSetStopLossPrice)
                    .put(OrderCallReason.CHANGE_TP, orderTaskDataProvider::forSetTakeProfitPrice)
                    .build());
    }

    public Observable<OrderEvent> callObservable(final IOrder order,
                                                 final OrderCallReason callReason) {
        return Observable
            .just(changeDoneByReason.get(callReason).apply(order))
            .doOnNext(this::registerOrder)
            .flatMap(this::gatewayObservable);
    }

    private final void registerOrder(final OrderTaskData taskData) {
        final OrderCallRequest orderCallRequest = new OrderCallRequest(taskData.order(), taskData.callReason());
        orderEventGateway.registerOrderCallRequest(orderCallRequest);
    }

    private final Observable<OrderEvent> gatewayObservable(final OrderTaskData taskData) {
        return orderEventGateway
            .observable()
            .filter(orderEvent -> orderEvent.order().equals(taskData.order()))
            .filter(orderEvent -> taskData.isEventTypeForCommand(orderEvent.type()))
            .takeUntil((final OrderEvent orderEvent) -> taskData.isFinishEventType(orderEvent.type()));
    }
}
