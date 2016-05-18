package com.jforex.programming.order;

import java.util.Collection;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dukascopy.api.IEngine;
import com.dukascopy.api.IOrder;
import com.dukascopy.api.Instrument;
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
import com.jforex.programming.position.Position;
import com.jforex.programming.position.PositionFactory;
import com.jforex.programming.position.RestoreSLTPPolicy;

import rx.Completable;
import rx.Observable;
import rx.Subscriber;

public class OrderUtil {

    private final IEngine engine;
    private final OrderCallExecutor orderCallExecutor;
    private final OrderEventGateway orderEventGateway;
    private final PositionFactory positionFactory;

    private static final Logger logger = LogManager.getLogger(OrderUtil.class);

    public OrderUtil(final IEngine engine,
                     final OrderCallExecutor orderCallExecutor,
                     final OrderEventGateway orderEventGateway,
                     final PositionFactory positionFactory) {
        this.engine = engine;
        this.orderCallExecutor = orderCallExecutor;
        this.orderEventGateway = orderEventGateway;
        this.positionFactory = positionFactory;
    }

    public Position position(final Instrument instrument) {
        return positionFactory.forInstrument(instrument);
    }

    public Observable<OrderEvent> submitOrder(final OrderParams orderParams) {
        final Instrument instrument = orderParams.instrument();
        logger.debug("Start submit task with label " + orderParams.label() + " for " + instrument + " position.");
        final OrderSupplierCall submitCall = () -> engine.submitOrder(orderParams.label(),
                                                                      orderParams.instrument(),
                                                                      orderParams.orderCommand(),
                                                                      orderParams.amount(),
                                                                      orderParams.price(),
                                                                      orderParams.slippage(),
                                                                      orderParams.stopLossPrice(),
                                                                      orderParams.takeProfitPrice(),
                                                                      orderParams.goodTillTime(),
                                                                      orderParams.comment());

        final Position position = positionFactory.forInstrument(orderParams.instrument());
        return runOrderSupplierCall(submitCall, OrderEventTypeData.submitData)
                .doOnNext(orderEvent -> position.addOrder(orderEvent.order()))
                .doOnError(e -> logger.error("Submit " + orderParams.label() + " for position "
                        + instrument + " failed!"))
                .doOnCompleted(() -> logger.debug("Submit " + orderParams.label() + " for position "
                        + instrument + " was successful."));
    }

    public Observable<OrderEvent> mergeOrders(final String mergeOrderLabel,
                                              final Collection<IOrder> toMergeOrders) {
        final OrderSupplierCall mergeCall = () -> engine.mergeOrders(mergeOrderLabel, toMergeOrders);
        final Position position = positionFactory.forInstrument(toMergeOrders.iterator().next().getInstrument());
        return runOrderSupplierCall(mergeCall, OrderEventTypeData.mergeData)
                .doOnNext(orderEvent -> position.addOrder(orderEvent.order()));
    }

    public Completable mergePositionOrders(final String mergeOrderLabel,
                                           final Instrument instrument,
                                           final RestoreSLTPPolicy restoreSLTPPolicy) {
        final Position position = positionFactory.forInstrument(instrument);
        return position.merge(mergeOrderLabel, restoreSLTPPolicy);
    }

    public Completable closePosition(final Instrument instrument) {
        final Position position = positionFactory.forInstrument(instrument);
        return position.close();
    }

    public Observable<OrderEvent> close(final IOrder orderToClose) {
        return runChangeCall(() -> orderToClose.close(),
                             orderToClose,
                             OrderEventTypeData.closeData);
    }

    public Observable<OrderEvent> setLabel(final IOrder orderToChangeLabel,
                                           final String newLabel) {
        return runChangeCall(() -> orderToChangeLabel.setLabel(newLabel),
                             orderToChangeLabel,
                             OrderEventTypeData.changeLabelData);
    }

    public Observable<OrderEvent> setGoodTillTime(final IOrder orderToChangeGTT,
                                                  final long newGTT) {
        return runChangeCall(() -> orderToChangeGTT.setGoodTillTime(newGTT),
                             orderToChangeGTT,
                             OrderEventTypeData.changeGTTData);
    }

    public Observable<OrderEvent> setOpenPrice(final IOrder orderToChangeOpenPrice,
                                               final double newOpenPrice) {
        return runChangeCall(() -> orderToChangeOpenPrice.setOpenPrice(newOpenPrice),
                             orderToChangeOpenPrice,
                             OrderEventTypeData.changeOpenPriceData);
    }

    public Observable<OrderEvent> setRequestedAmount(final IOrder orderToChangeAmount,
                                                     final double newAmount) {
        return runChangeCall(() -> orderToChangeAmount.setRequestedAmount(newAmount),
                             orderToChangeAmount,
                             OrderEventTypeData.changeAmountData);
    }

    public Observable<OrderEvent> setStopLossPrice(final IOrder orderToChangeSL,
                                                   final double newSL) {
        return runChangeCall(() -> orderToChangeSL.setStopLossPrice(newSL),
                             orderToChangeSL,
                             OrderEventTypeData.changeSLData);
    }

    public Observable<OrderEvent> setTakeProfitPrice(final IOrder orderToChangeTP,
                                                     final double newTP) {
        return runChangeCall(() -> orderToChangeTP.setTakeProfitPrice(newTP),
                             orderToChangeTP,
                             OrderEventTypeData.changeTPData);
    }

    private Observable<OrderEvent> runOrderSupplierCall(final OrderSupplierCall orderSupplierCall,
                                                        final OrderEventTypeData orderEventTypeData) {
        final OrderCallExecutorResult orderExecutorResult =
                createResult(orderSupplierCall, orderEventTypeData.callRequest());
        return createObs(orderExecutorResult, orderEventTypeData);
    }

    private Observable<OrderEvent> runChangeCall(final OrderChangeCall orderChangeCall,
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
