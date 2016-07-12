package com.jforex.programming.order;

import static com.jforex.programming.order.event.OrderEventTypeSets.endOfOrderEventTypes;

import java.util.concurrent.Callable;

import com.jforex.programming.misc.JFRunnable;
import com.jforex.programming.order.call.OrderCallExecutor;
import com.jforex.programming.order.call.OrderCallReason;
import com.jforex.programming.order.call.OrderCallRejectException;
import com.jforex.programming.order.call.OrderCallRequest;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.event.OrderEventGateway;
import com.jforex.programming.order.event.OrderEventType;
import com.jforex.programming.order.event.OrderEventTypeData;

import com.dukascopy.api.IOrder;

import rx.Observable;
import rx.Subscriber;

public class OrderUtilHandler {

    private final OrderCallExecutor orderCallExecutor;
    private final OrderEventGateway orderEventGateway;

    public OrderUtilHandler(final OrderCallExecutor orderCallExecutor,
                            final OrderEventGateway orderEventGateway) {
        this.orderCallExecutor = orderCallExecutor;
        this.orderEventGateway = orderEventGateway;
    }

    public Observable<OrderEvent> changeObservable(final JFRunnable orderRunnable,
                                                   final IOrder orderToChange,
                                                   final OrderEventTypeData orderEventTypeData) {
        final Callable<IOrder> orderCallable = () -> {
            orderRunnable.run();
            return orderToChange;
        };
        return submitObservable(orderCallable, orderEventTypeData);
    }

    public Observable<OrderEvent> submitObservable(final Callable<IOrder> orderCallable,
                                                   final OrderEventTypeData orderEventTypeData) {
        return orderCallExecutor
                .callObservable(orderCallable)
                .doOnNext(order -> registerOrder(order, orderEventTypeData.callReason()))
                .flatMap(order -> createObservable(order, orderEventTypeData));
    }

    private void registerOrder(final IOrder order,
                               final OrderCallReason orderCallReason) {
        final OrderCallRequest orderCallRequest = new OrderCallRequest(order, orderCallReason);
        orderEventGateway.registerOrderCallRequest(orderCallRequest);
    }

    private final Observable<OrderEvent> createObservable(final IOrder order,
                                                          final OrderEventTypeData orderEventTypeData) {
        return Observable.create(subscriber -> {
            orderEventGateway.observable()
                    .filter(orderEvent -> orderEvent.order() == order)
                    .filter(orderEvent -> orderEventTypeData.all().contains(orderEvent.type()))
                    .takeUntil(orderEvent -> endOfOrderEventTypes.contains(orderEvent.type()))
                    .subscribe(orderEvent -> evaluateOrderEvent(orderEvent, orderEventTypeData, subscriber));
        });
    }

    private final void evaluateOrderEvent(final OrderEvent orderEvent,
                                          final OrderEventTypeData orderEventTypeData,
                                          final Subscriber<? super OrderEvent> subscriber) {
        final OrderEventType orderEventType = orderEvent.type();
        if (!subscriber.isUnsubscribed())
            if (orderEventTypeData.isRejectType(orderEventType))
                subscriber.onError(new OrderCallRejectException("", orderEvent));
            else {
                subscriber.onNext(orderEvent);
                if (orderEventTypeData.isDoneType(orderEventType))
                    subscriber.onCompleted();
            }
    }
}
