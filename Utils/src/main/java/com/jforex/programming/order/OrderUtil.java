package com.jforex.programming.order;

import static com.jforex.programming.order.event.OrderEventTypeSets.endOfOrderEventTypes;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dukascopy.api.IEngine;
import com.dukascopy.api.IOrder;
import com.jforex.programming.order.call.OrderCreateCall;
import com.jforex.programming.order.call.OrderCallExecutor;
import com.jforex.programming.order.call.OrderCallRequest;
import com.jforex.programming.order.call.OrderCallResult;
import com.jforex.programming.order.call.OrderChangeCall;
import com.jforex.programming.order.call.OrderExecutorResult;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.event.OrderEventConsumer;
import com.jforex.programming.order.event.OrderEventGateway;
import com.jforex.programming.order.event.OrderEventType;

import rx.Observable.Transformer;

public class OrderUtil {

    private final IEngine engine;
    private final OrderCallExecutor orderCallExecutor;
    private final OrderEventGateway orderEventGateway;

    private final static Logger logger = LogManager.getLogger(OrderUtil.class);

    public OrderUtil(final IEngine engine,
                     final OrderCallExecutor orderCallExecutor,
                     final OrderEventGateway orderEventGateway) {
        this.engine = engine;
        this.orderCallExecutor = orderCallExecutor;
        this.orderEventGateway = orderEventGateway;
    }

    public OrderCallResult submit(final OrderParams orderParams) {
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
        return callResultForCreate(submitCall, OrderCallRequest.SUBMIT);
    }

    public OrderCallResult submit(final OrderParams orderParams,
                                  final OrderEventConsumer orderEventConsumer) {
        final OrderCallResult orderCallResult = submit(orderParams);
        return registerConsumer(orderCallResult, orderEventConsumer);
    }

    public OrderCallResult submit(final OrderParams orderParams,
                                  final Map<OrderEventType, OrderEventConsumer> orderEventConsumerMap) {
        final OrderCallResult orderCallResult = submit(orderParams);
        return registerConsumerMap(orderCallResult, orderEventConsumerMap);
    }

    public OrderCallResult merge(final String mergeOrderLabel,
                                 final Collection<IOrder> toMergeOrders) {
        final OrderCreateCall mergeCall = () -> engine.mergeOrders(mergeOrderLabel, toMergeOrders);
        return callResultForCreate(mergeCall, OrderCallRequest.MERGE);
    }

    public OrderCallResult merge(final String mergeOrderLabel,
                                 final Collection<IOrder> toMergeOrders,
                                 final OrderEventConsumer orderEventConsumer) {
        final OrderCallResult orderCallResult = merge(mergeOrderLabel, toMergeOrders);
        return registerConsumer(orderCallResult, orderEventConsumer);
    }

    public OrderCallResult merge(final String mergeOrderLabel,
                                 final Collection<IOrder> toMergeOrders,
                                 final Map<OrderEventType, OrderEventConsumer> orderEventConsumerMap) {
        final OrderCallResult orderCallResult = merge(mergeOrderLabel, toMergeOrders);
        return registerConsumerMap(orderCallResult, orderEventConsumerMap);
    }

    private OrderCallResult registerConsumer(final OrderCallResult orderCallResult,
                                             final OrderEventConsumer orderEventConsumer) {
        if (!orderCallResult.exceptionOpt().isPresent())
            registerConsumer(orderCallResult.orderOpt().get(), orderEventConsumer);
        return orderCallResult;
    }

    private OrderCallResult registerConsumerMap(final OrderCallResult orderCallResult,
                                                final Map<OrderEventType, OrderEventConsumer> orderEventConsumerMap) {
        if (!orderCallResult.exceptionOpt().isPresent())
            registerConsumerMap(orderCallResult.orderOpt().get(), orderEventConsumerMap);
        return orderCallResult;
    }

    public OrderCallResult close(final IOrder orderToClose) {
        return callResultForChange(() -> orderToClose.close(),
                                   orderToClose,
                                   OrderCallRequest.CLOSE);
    }

    public OrderCallResult changeLabel(final IOrder orderToChangeLabel,
                                       final String newLabel) {
        return callResultForChange(() -> orderToChangeLabel.setLabel(newLabel),
                                   orderToChangeLabel,
                                   OrderCallRequest.CHANGE_LABEL);
    }

    public OrderCallResult changeGTT(final IOrder orderToChangeGTT,
                                     final long newGTT) {
        return callResultForChange(() -> orderToChangeGTT.setGoodTillTime(newGTT),
                                   orderToChangeGTT,
                                   OrderCallRequest.CHANGE_GTT);
    }

    public OrderCallResult changeOpenPrice(final IOrder orderToChangeOpenPrice,
                                           final double newOpenPrice) {
        return callResultForChange(() -> orderToChangeOpenPrice.setOpenPrice(newOpenPrice),
                                   orderToChangeOpenPrice,
                                   OrderCallRequest.CHANGE_OPENPRICE);
    }

    public OrderCallResult changeAmount(final IOrder orderToChangeAmount,
                                        final double newAmount) {
        return callResultForChange(() -> orderToChangeAmount.setRequestedAmount(newAmount),
                                   orderToChangeAmount,
                                   OrderCallRequest.CHANGE_AMOUNT);
    }

    public OrderCallResult changeSL(final IOrder orderToChangeSL,
                                    final double newSL) {
        return callResultForChange(() -> orderToChangeSL.setStopLossPrice(newSL),
                                   orderToChangeSL,
                                   OrderCallRequest.CHANGE_SL);
    }

    public OrderCallResult changeTP(final IOrder orderToChangeTP,
                                    final double newTP) {
        return callResultForChange(() -> orderToChangeTP.setTakeProfitPrice(newTP),
                                   orderToChangeTP,
                                   OrderCallRequest.CHANGE_TP);
    }

    private OrderCallResult callResultForCreate(final OrderCreateCall orderCall,
                                                final OrderCallRequest orderCallRequest) {
        return createAndRegisterCallResult(orderCall, Optional.empty(), orderCallRequest);
    }

    private OrderCallResult callResultForChange(final OrderChangeCall orderChangeCall,
                                                final IOrder orderToChange,
                                                final OrderCallRequest orderCallRequest) {
        final OrderCreateCall orderCall = orderCallFromOrderChange(orderToChange, orderChangeCall);
        return createAndRegisterCallResult(orderCall, Optional.of(orderToChange), orderCallRequest);
    }

    private OrderCallResult createAndRegisterCallResult(final OrderCreateCall orderCall,
                                                        final Optional<IOrder> orderToChangeOpt,
                                                        final OrderCallRequest orderCallRequest) {
        final OrderCallResult orderCallResult = callResultFromExecutorResult(orderCall,
                                                                             orderToChangeOpt,
                                                                             orderCallRequest);
        return registerCallResult(orderCallResult);
    }

    private OrderCallResult callResultFromExecutorResult(final OrderCreateCall orderCall,
                                                         final Optional<IOrder> orderToChangeOpt,
                                                         final OrderCallRequest orderCallRequest) {
        final OrderExecutorResult orderExecutorResult = orderCallExecutor.run(orderCall);
        final IOrder orderFromExecution = orderExecutorResult.orderOpt().orElse(orderToChangeOpt.orElse(null));
        return new OrderCallResult(Optional.ofNullable(orderFromExecution),
                                   orderExecutorResult.exceptionOpt(),
                                   orderCallRequest);
    }

    private OrderCallResult registerCallResult(final OrderCallResult orderCallResult) {
        if (!orderCallResult.exceptionOpt().isPresent())
            orderEventGateway.onOrderCallResult(orderCallResult);
        return orderCallResult;
    }

    private final OrderCreateCall orderCallFromOrderChange(final IOrder orderToChange,
                                                     final OrderChangeCall orderChangeCall) {
        return () -> {
            orderChangeCall.run();
            return orderToChange;
        };
    }

    private void registerConsumer(final IOrder order,
                                  final OrderEventConsumer orderEventConsumer) {
        registerOnObservable(order, orderEventConsumer::onOrderEvent);
        logger.info("Subscribed order events for " + order.getInstrument() + " with label " + order.getLabel());
    }

    private void registerConsumerMap(final IOrder order,
                                     final Map<OrderEventType, OrderEventConsumer> orderEventConsumerMap) {
        registerOnObservable(order,
                             oe -> {
                                 if (orderEventConsumerMap.containsKey(oe.type()))
                                     orderEventConsumerMap.get(oe.type()).onOrderEvent(oe);
                             });
        logger.info("Subscribed order events map for " + order.getInstrument() + " with label " + order.getLabel());
    }

    private void registerOnObservable(final IOrder order,
                                      final Consumer<OrderEvent> orderEventConsumer) {
        orderEventGateway.observable()
                         .compose(applyOrderFilter(order))
                         .subscribe(orderEventConsumer::accept);
    }

    Transformer<OrderEvent, OrderEvent> applyOrderFilter(final IOrder order) {
        return observable -> observable.filter(oe -> oe.order().equals(order))
                                       .takeUntil(oe -> endOfOrderEventTypes.contains(oe.type()));
    }
}
