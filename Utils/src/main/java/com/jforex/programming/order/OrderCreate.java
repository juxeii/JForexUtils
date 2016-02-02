package com.jforex.programming.order;

import static com.jforex.programming.order.event.OrderEventTypeSets.endOfOrderEventTypes;

import java.util.Collection;
import java.util.Map;
import java.util.function.Consumer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dukascopy.api.IEngine;
import com.dukascopy.api.IOrder;
import com.jforex.programming.order.call.OrderCallExecutor;
import com.jforex.programming.order.call.OrderCallExecutorResult;
import com.jforex.programming.order.call.OrderCallRequest;
import com.jforex.programming.order.call.OrderCreateCall;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.event.OrderEventConsumer;
import com.jforex.programming.order.event.OrderEventGateway;
import com.jforex.programming.order.event.OrderEventType;

public class OrderCreate {

    private final IEngine engine;
    private final OrderCallExecutor orderCallExecutor;
    private final OrderEventGateway orderEventGateway;

    private final static Logger logger = LogManager.getLogger(OrderCreate.class);

    public OrderCreate(final IEngine engine,
                       final OrderCallExecutor orderCallExecutor,
                       final OrderEventGateway orderEventGateway) {
        this.engine = engine;
        this.orderCallExecutor = orderCallExecutor;
        this.orderEventGateway = orderEventGateway;
    }

    public OrderCreateResult submit(final OrderParams orderParams) {
        final OrderCreateCall submitCall = () -> engine.submitOrder(orderParams.label(),
                                                                    orderParams.instrument(),
                                                                    orderParams.orderCommand(),
                                                                    orderParams.amount(),
                                                                    orderParams.price(),
                                                                    orderParams.slippage(),
                                                                    orderParams.stopLossPrice(),
                                                                    orderParams.takeProfitPrice(),
                                                                    orderParams.goodTillTime(),
                                                                    orderParams.comment());
        return createResult(submitCall, OrderCallRequest.SUBMIT);
    }

    public OrderCreateResult submit(final OrderParams orderParams,
                                    final OrderEventConsumer orderEventConsumer) {
        final OrderCreateResult orderCreateResult = submit(orderParams);
        registerConsumer(orderCreateResult, orderEventConsumer);
        return orderCreateResult;
    }

    public OrderCreateResult submit(final OrderParams orderParams,
                                    final Map<OrderEventType, OrderEventConsumer> orderEventConsumerMap) {
        final OrderCreateResult orderCreateResult = submit(orderParams);
        registerConsumerMap(orderCreateResult, orderEventConsumerMap);
        return orderCreateResult;
    }

    public OrderCreateResult merge(final String mergeOrderLabel,
                                   final Collection<IOrder> toMergeOrders) {
        final OrderCreateCall mergeCall = () -> engine.mergeOrders(mergeOrderLabel, toMergeOrders);
        return createResult(mergeCall, OrderCallRequest.MERGE);
    }

    public OrderCreateResult merge(final String mergeOrderLabel,
                                   final Collection<IOrder> toMergeOrders,
                                   final OrderEventConsumer orderEventConsumer) {
        final OrderCreateResult orderCreateResult = merge(mergeOrderLabel, toMergeOrders);
        registerConsumer(orderCreateResult, orderEventConsumer);
        return orderCreateResult;
    }

    public OrderCreateResult merge(final String mergeOrderLabel,
                                   final Collection<IOrder> toMergeOrders,
                                   final Map<OrderEventType, OrderEventConsumer> orderEventConsumerMap) {
        final OrderCreateResult orderCreateResult = merge(mergeOrderLabel, toMergeOrders);
        registerConsumerMap(orderCreateResult, orderEventConsumerMap);
        return orderCreateResult;
    }

    private void registerConsumer(final OrderCreateResult orderCreateResult,
                                  final OrderEventConsumer orderEventConsumer) {
        if (!orderCreateResult.exceptionOpt().isPresent()) {
            final IOrder createdOrder = orderCreateResult.orderOpt().get();
            registerOnObservable(createdOrder, orderEventConsumer::onOrderEvent);
            logger.info("Subscribed order events for " + createdOrder.getInstrument()
                    + " with label " + createdOrder.getLabel());
        }
    }

    private void registerConsumerMap(final OrderCreateResult orderCreateResult,
                                     final Map<OrderEventType, OrderEventConsumer> orderEventConsumerMap) {
        if (!orderCreateResult.exceptionOpt().isPresent()) {
            final IOrder createdOrder = orderCreateResult.orderOpt().get();
            registerOnObservable(createdOrder,
                                 orderEvent -> {
                                     final OrderEventType orderEventType = orderEvent.type();
                                     if (orderEventConsumerMap.containsKey(orderEventType))
                                         orderEventConsumerMap.get(orderEventType).onOrderEvent(orderEvent);
                                 });
            logger.info("Subscribed order events map for " + createdOrder.getInstrument()
                    + " with label " + createdOrder.getLabel());
        }
    }

    private OrderCreateResult createResult(final OrderCreateCall orderCreateCall,
                                           final OrderCallRequest orderCallRequest) {
        final OrderCallExecutorResult orderExecutorResult = orderCallExecutor.run(orderCreateCall);
        final OrderCreateResult orderCreateResult = new OrderCreateResult(orderExecutorResult.orderOpt(),
                                                                          orderExecutorResult.exceptionOpt(),
                                                                          orderCallRequest);
        registerOrderCallRequest(orderCreateResult);
        return orderCreateResult;
    }

    private void registerOrderCallRequest(final OrderCreateResult orderCreateResult) {
        if (!orderCreateResult.exceptionOpt().isPresent())
            orderEventGateway.registerOrderRequest(orderCreateResult.orderOpt().get(),
                                                   orderCreateResult.callRequest());
    }

    private void registerOnObservable(final IOrder order,
                                      final Consumer<OrderEvent> orderEventConsumer) {
        orderEventGateway.observable()
                         .filter(orderEvent -> orderEvent.order().equals(order))
                         .takeUntil(orderEvent -> endOfOrderEventTypes.contains(orderEvent.type()))
                         .subscribe(orderEventConsumer::accept);
    }
}
