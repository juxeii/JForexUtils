package com.jforex.programming.order;

import java.util.Collection;

import com.jforex.programming.misc.CalculationUtil;
import com.jforex.programming.order.call.OrderCallExecutor;
import com.jforex.programming.order.call.OrderCallExecutorResult;
import com.jforex.programming.order.call.OrderCallRequest;
import com.jforex.programming.order.call.OrderChangeCall;
import com.jforex.programming.order.call.OrderSupplierCall;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.event.OrderEventGateway;
import com.jforex.programming.order.event.OrderEventType;
import com.jforex.programming.position.OrderEventTypesInfo;
import com.jforex.programming.position.PositionTaskRejectException;

import com.dukascopy.api.IEngine;
import com.dukascopy.api.IOrder;

import rx.Observable;
import rx.Subscriber;

public class OrderUtil {

    private final IEngine engine;
    private final OrderCallExecutor orderCallExecutor;
    private final OrderEventGateway orderEventGateway;

    public OrderUtil(final IEngine engine,
                     final OrderCallExecutor orderCallExecutor,
                     final OrderEventGateway orderEventGateway) {
        this.engine = engine;
        this.orderCallExecutor = orderCallExecutor;
        this.orderEventGateway = orderEventGateway;
    }

    public Observable<OrderEvent> submit(final OrderParams orderParams) {
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
        return runOrderSupplierCall(submitCall,
                                    OrderCallRequest.SUBMIT,
                                    OrderEventTypesInfo.submitEvents);
    }

    public Observable<OrderEvent> merge(final String mergeOrderLabel,
                                        final Collection<IOrder> toMergeOrders) {
        final OrderSupplierCall mergeCall = () -> engine.mergeOrders(mergeOrderLabel, toMergeOrders);
        return runOrderSupplierCall(mergeCall,
                                    OrderCallRequest.MERGE,
                                    OrderEventTypesInfo.mergeEvents);
    }

    public Observable<OrderEvent> close(final IOrder orderToClose) {
        return runChangeCall(() -> orderToClose.close(),
                             orderToClose,
                             OrderCallRequest.CLOSE,
                             OrderEventTypesInfo.closeEvents);
    }

    public Observable<OrderEvent> setLabel(final IOrder orderToChangeLabel,
                                           final String newLabel) {
        return runChangeCall(() -> orderToChangeLabel.setLabel(newLabel),
                             orderToChangeLabel,
                             OrderCallRequest.CHANGE_LABEL,
                             OrderEventTypesInfo.changeLabelEvents);
    }

    public Observable<OrderEvent> setGTT(final IOrder orderToChangeGTT,
                                         final long newGTT) {
        return runChangeCall(() -> orderToChangeGTT.setGoodTillTime(newGTT),
                             orderToChangeGTT,
                             OrderCallRequest.CHANGE_GTT,
                             OrderEventTypesInfo.changeGTTEvents);
    }

    public Observable<OrderEvent> setOpenPrice(final IOrder orderToChangeOpenPrice,
                                               final double newOpenPrice) {
        return runChangeCall(() -> orderToChangeOpenPrice.setOpenPrice(newOpenPrice),
                             orderToChangeOpenPrice,
                             OrderCallRequest.CHANGE_OPENPRICE,
                             OrderEventTypesInfo.changeOpenPriceEvents);
    }

    public Observable<OrderEvent> setAmount(final IOrder orderToChangeAmount,
                                            final double newAmount) {
        return runChangeCall(() -> orderToChangeAmount.setRequestedAmount(newAmount),
                             orderToChangeAmount,
                             OrderCallRequest.CHANGE_AMOUNT,
                             OrderEventTypesInfo.changeAmountEvents);
    }

    public Observable<OrderEvent> setSL(final IOrder orderToChangeSL,
                                        final double newSL) {
        return runChangeCall(() -> orderToChangeSL.setStopLossPrice(newSL),
                             orderToChangeSL,
                             OrderCallRequest.CHANGE_SL,
                             OrderEventTypesInfo.changeSLEvents);
    }

    public Observable<OrderEvent> setTP(final IOrder orderToChangeTP,
                                        final double newTP) {
        return runChangeCall(() -> orderToChangeTP.setTakeProfitPrice(newTP),
                             orderToChangeTP,
                             OrderCallRequest.CHANGE_TP,
                             OrderEventTypesInfo.changeTPEvents);
    }

    public Observable<OrderEvent> setSLWithPips(final IOrder orderToChangeSL,
                                                final double referencePrice,
                                                final double pips) {
        final double newSL = CalculationUtil.addPips(orderToChangeSL.getInstrument(),
                                                     referencePrice,
                                                     orderToChangeSL.isLong() ? -pips : pips);
        return setSL(orderToChangeSL, newSL);
    }

    public Observable<OrderEvent> setTPWithPips(final IOrder orderToChangeTP,
                                                final double referencePrice,
                                                final double pips) {
        final double newTP = CalculationUtil.addPips(orderToChangeTP.getInstrument(),
                                                     referencePrice,
                                                     orderToChangeTP.isLong() ? pips : -pips);
        return setTP(orderToChangeTP, newTP);
    }

    private Observable<OrderEvent> runOrderSupplierCall(final OrderSupplierCall orderSupplierCall,
                                                        final OrderCallRequest orderCallRequest,
                                                        final OrderEventTypesInfo orderEventTypesInfo) {
        final OrderCallExecutorResult orderExecutorResult = createResult(orderSupplierCall, orderCallRequest);
        return createObs(orderExecutorResult, orderEventTypesInfo);
    }

    private Observable<OrderEvent> runChangeCall(final OrderChangeCall orderChangeCall,
                                                 final IOrder orderToChange,
                                                 final OrderCallRequest orderCallRequest,
                                                 final OrderEventTypesInfo orderEventTypesInfo) {
        final OrderSupplierCall orderSupplierCall = () -> {
            orderChangeCall.change();
            return orderToChange;
        };
        return runOrderSupplierCall(orderSupplierCall, orderCallRequest, orderEventTypesInfo);
    }

    private OrderCallExecutorResult createResult(final OrderSupplierCall orderSupplierCall,
                                                 final OrderCallRequest orderCallRequest) {
        final OrderCallExecutorResult orderExecutorResult = orderCallExecutor.run(orderSupplierCall);
        registerOrderCallRequest(orderExecutorResult, orderCallRequest);
        return orderExecutorResult;
    }

    public Observable<OrderEvent> createObs(final OrderCallExecutorResult orderExecutorResult,
                                            final OrderEventTypesInfo orderEventTypesInfo) {
        return orderExecutorResult.exceptionOpt().isPresent()
                ? Observable.error(orderExecutorResult.exceptionOpt().get())
                : Observable.create(subscriber -> {
                    orderEventGateway.observable()
                            .filter(orderEvent -> orderEvent.order() == orderExecutorResult.orderOpt().get())
                            .filter(orderEvent -> orderEventTypesInfo.all().contains(orderEvent.type()))
                            .subscribe(orderEvent -> evaluateOrderEvent(orderEvent, orderEventTypesInfo, subscriber));
                });
    }

    private final void evaluateOrderEvent(final OrderEvent orderEvent,
                                          final OrderEventTypesInfo orderEventTypesInfo,
                                          final Subscriber<? super OrderEvent> subscriber) {
        final OrderEventType orderEventType = orderEvent.type();
        if (orderEventTypesInfo.isRejectType(orderEventType))
            subscriber.onError(new PositionTaskRejectException("", orderEvent));
        else {
            subscriber.onNext(orderEvent);
            if (orderEventTypesInfo.isDoneType(orderEventType))
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
