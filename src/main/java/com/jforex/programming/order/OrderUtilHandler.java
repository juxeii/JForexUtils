package com.jforex.programming.order;

import static com.jforex.programming.order.event.OrderEventTypeSets.endOfOrderEventTypes;

import com.dukascopy.api.IOrder;

import com.jforex.programming.misc.RunnableWithJFException;
import com.jforex.programming.misc.RxUtil;
import com.jforex.programming.order.call.OrderCallExecutor;
import com.jforex.programming.order.call.OrderCallExecutorResult;
import com.jforex.programming.order.call.OrderCallRejectException;
import com.jforex.programming.order.call.OrderCallRequest;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.event.OrderEventGateway;
import com.jforex.programming.order.event.OrderEventType;
import com.jforex.programming.order.event.OrderEventTypeData;

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

    public Observable<OrderEvent> runOrderSupplierCall(final OrderSupplier orderSupplierCall,
                                                       final OrderEventTypeData orderEventTypeData) {
        final OrderCallExecutorResult orderExecutorResult =
                createResult(orderSupplierCall, orderEventTypeData.callRequest());
        return RxUtil.connectObservable(createObs(orderExecutorResult, orderEventTypeData));
    }

    public Observable<OrderEvent> runOrderChangeCall(final RunnableWithJFException orderChangeCall,
                                                     final IOrder orderToChange,
                                                     final OrderEventTypeData orderEventTypeData) {
        final OrderSupplier orderSupplierCall = () -> {
            orderChangeCall.run();
            return orderToChange;
        };
        return runOrderSupplierCall(orderSupplierCall, orderEventTypeData);
    }

    private final OrderCallExecutorResult createResult(final OrderSupplier orderSupplierCall,
                                                       final OrderCallRequest orderCallRequest) {
        final OrderCallExecutorResult orderExecutorResult = orderCallExecutor.run(orderSupplierCall);
        registerOrderCallRequest(orderExecutorResult, orderCallRequest);
        return orderExecutorResult;
    }

    private final Observable<OrderEvent> createObs(final OrderCallExecutorResult orderExecutorResult,
                                                   final OrderEventTypeData orderEventTypeData) {
        return orderExecutorResult.maybeException().isPresent()
                ? Observable.error(orderExecutorResult.maybeException().get())
                : Observable.create(subscriber -> {
                    orderEventGateway.observable()
                            .filter(orderEvent -> orderEvent.order() == orderExecutorResult.maybeOrder().get())
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

    private final void registerOrderCallRequest(final OrderCallExecutorResult orderExecutorResult,
                                                final OrderCallRequest orderCallRequest) {
        if (orderExecutorResult.maybeOrder().isPresent())
            orderEventGateway.registerOrderRequest(orderExecutorResult.maybeOrder().get(),
                                                   orderCallRequest);
    }
}
