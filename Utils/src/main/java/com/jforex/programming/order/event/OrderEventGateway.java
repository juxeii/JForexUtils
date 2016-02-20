package com.jforex.programming.order.event;

import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;

import com.dukascopy.api.IOrder;
import com.google.common.collect.MapMaker;
import com.google.common.collect.Sets;
import com.jforex.programming.order.OrderMessageData;
import com.jforex.programming.order.call.OrderCallRequest;

import rx.Observable;
import rx.Subscriber;
import rx.subscriptions.Subscriptions;

public class OrderEventGateway {

    private final Observable<OrderEvent> orderEventObservable;
    private final Set<Subscriber<? super OrderEvent>> orderEventSubscriber = Sets.newConcurrentHashSet();
    private final ConcurrentMap<IOrder, Queue<OrderCallRequest>> callRequestByOrder =
            new MapMaker().weakKeys().makeMap();

    public OrderEventGateway() {
        orderEventObservable = Observable.create(subsciber -> subscribe(subsciber));
    }

    public Observable<OrderEvent> observable() {
        return orderEventObservable;
    }

    public void registerOrderRequest(final IOrder order,
                                     final OrderCallRequest orderCallRequest) {
        callRequestByOrder.putIfAbsent(order, new ConcurrentLinkedQueue<>());
        callRequestByOrder.get(order).add(orderCallRequest);
    }

    public void onOrderMessageData(final OrderMessageData orderMessageData) {
        final IOrder order = orderMessageData.order();
        final OrderEventType orderEventType = orderEventTypeFromData(orderMessageData);
        orderEventSubscriber.forEach(consumer -> consumer.onNext(new OrderEvent(order, orderEventType)));
    }

    private final void subscribe(final Subscriber<? super OrderEvent> subscriber) {
        subscriber.add(Subscriptions.create(() -> orderEventSubscriber.remove(subscriber)));
        orderEventSubscriber.add(subscriber);
    }

    private final OrderEventType orderEventTypeFromData(final OrderMessageData orderMessageData) {
        return callRequestByOrder.containsKey(orderMessageData.order())
                ? orderEventWithQueuePresent(orderMessageData)
                : OrderEventTypeEvaluator.get(orderMessageData, Optional.empty());
    }

    private final OrderEventType orderEventWithQueuePresent(final OrderMessageData orderMessageData) {
        final IOrder order = orderMessageData.order();
        Optional<OrderCallRequest> callRequestOpt = Optional.empty();
        if (!callRequestByOrder.get(order).isEmpty())
            callRequestOpt = Optional.of(callRequestByOrder.get(order).poll());
        else
            callRequestByOrder.remove(order);

        return OrderEventTypeEvaluator.get(orderMessageData, callRequestOpt);
    }
}
