package com.jforex.programming.order;

import com.jforex.programming.order.call.OrderCallExecutor;
import com.jforex.programming.order.call.OrderCallExecutorResult;
import com.jforex.programming.order.call.OrderCallRejectException;
import com.jforex.programming.order.call.OrderCallRequest;
import com.jforex.programming.order.call.OrderChangeCall;
import com.jforex.programming.order.call.OrderSupplierCall;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.event.OrderEventGateway;
import com.jforex.programming.order.event.OrderEventType;
import com.jforex.programming.order.event.OrderEventTypeData;

import com.dukascopy.api.IOrder;

import rx.Observable;
import rx.Subscriber;
import rx.observables.ConnectableObservable;

public class OrderUtilHandler {

    private final OrderCallExecutor orderCallExecutor;
    private final OrderEventGateway orderEventGateway;

    public OrderUtilHandler(final OrderCallExecutor orderCallExecutor,
                            final OrderEventGateway orderEventGateway) {
        this.orderCallExecutor = orderCallExecutor;
        this.orderEventGateway = orderEventGateway;
    }

    public Observable<OrderEvent> runOrderSupplierCall(final OrderSupplierCall orderSupplierCall,
                                                       final OrderEventTypeData orderEventTypeData) {
        final OrderCallExecutorResult orderExecutorResult =
                createResult(orderSupplierCall, orderEventTypeData.callRequest());
        final ConnectableObservable<OrderEvent> observable =
                createObs(orderExecutorResult, orderEventTypeData).replay();
        observable.connect();
        return observable;
    }

    public Observable<OrderEvent> runOrderChangeCall(final OrderChangeCall orderChangeCall,
                                                     final IOrder orderToChange,
                                                     final OrderEventTypeData orderEventTypeData) {
        final OrderSupplierCall orderSupplierCall = () -> {
            orderChangeCall.change();
            return orderToChange;
        };
        return runOrderSupplierCall(orderSupplierCall, orderEventTypeData);
    }

    private OrderCallExecutorResult createResult(final OrderSupplierCall orderSupplierCall,
                                                 final OrderCallRequest orderCallRequest) {
        final OrderCallExecutorResult orderExecutorResult = orderCallExecutor.run(orderSupplierCall);
        registerOrderCallRequest(orderExecutorResult, orderCallRequest);
        return orderExecutorResult;
    }

    private Observable<OrderEvent> createObs(final OrderCallExecutorResult orderExecutorResult,
                                             final OrderEventTypeData orderEventTypeData) {
        return orderExecutorResult.exceptionOpt().isPresent()
                ? Observable.error(orderExecutorResult.exceptionOpt().get())
                : Observable.create(subscriber -> {
                    orderEventGateway.observable()
                            .filter(orderEvent -> orderEvent.order() == orderExecutorResult.orderOpt().get())
                            .filter(orderEvent -> orderEventTypeData.all().contains(orderEvent.type()))
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

    private void registerOrderCallRequest(final OrderCallExecutorResult orderExecutorResult,
                                          final OrderCallRequest orderCallRequest) {
        if (orderExecutorResult.orderOpt().isPresent())
            orderEventGateway.registerOrderRequest(orderExecutorResult.orderOpt().get(),
                                                   orderCallRequest);
    }
}
