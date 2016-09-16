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
                            final OrderTaskDataFactory orderTaskDataFactory) {
        this.orderEventGateway = orderEventGateway;

        changeDoneByReason =
                Maps.immutableEnumMap(ImmutableMap.<OrderCallReason, Function<IOrder, OrderTaskData>> builder()
                    .put(OrderCallReason.SUBMIT, orderTaskDataFactory::forSubmit)
                    .put(OrderCallReason.MERGE, orderTaskDataFactory::forMerge)
                    .put(OrderCallReason.CLOSE, orderTaskDataFactory::forClose)
                    .put(OrderCallReason.CHANGE_LABEL, orderTaskDataFactory::forSetLabel)
                    .put(OrderCallReason.CHANGE_GTT, orderTaskDataFactory::forSetGoodTillTime)
                    .put(OrderCallReason.CHANGE_AMOUNT, orderTaskDataFactory::forSetRequestedAmount)
                    .put(OrderCallReason.CHANGE_PRICE, orderTaskDataFactory::forSetOpenPrice)
                    .put(OrderCallReason.CHANGE_SL, orderTaskDataFactory::forSetStopLossPrice)
                    .put(OrderCallReason.CHANGE_TP, orderTaskDataFactory::forSetTakeProfitPrice)
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
