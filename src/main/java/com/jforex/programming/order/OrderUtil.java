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

import com.dukascopy.api.IEngine;
import com.dukascopy.api.IOrder;

import rx.Observable;

public class OrderUtil {

    private final IEngine engine;
    private final OrderCallExecutor orderCallExecutor;
    private final OrderEventGateway orderEventGateway;

    private final static int longFactor = 1;
    private final static int shortFactor = -1;

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
        final OrderCallExecutorResult orderExecutorResult = createResult(submitCall, OrderCallRequest.SUBMIT);
        return createObs(orderExecutorResult);
    }

    public Observable<OrderEvent> merge(final String mergeOrderLabel,
                                        final Collection<IOrder> toMergeOrders) {
        final OrderSupplierCall mergeCall = () -> engine.mergeOrders(mergeOrderLabel, toMergeOrders);
        final OrderCallExecutorResult orderExecutorResult = createResult(mergeCall, OrderCallRequest.MERGE);
        return createObs(orderExecutorResult);
    }

    private OrderCallExecutorResult createResult(final OrderSupplierCall orderSupplierCall,
                                                 final OrderCallRequest orderCallRequest) {
        final OrderCallExecutorResult orderExecutorResult = orderCallExecutor.run(orderSupplierCall);
        registerOrderCallRequest(orderExecutorResult, orderCallRequest);
        return orderExecutorResult;
    }

    private Observable<OrderEvent> createObs(final OrderCallExecutorResult orderExecutorResult) {
        return orderExecutorResult.exceptionOpt().isPresent()
                ? Observable.error(orderExecutorResult.exceptionOpt().get())
                : orderEventGateway
                        .observable()
                        .filter(orderEvent -> orderEvent.order() == orderExecutorResult.orderOpt().get());
    }

    public Observable<OrderEvent> close(final IOrder orderToClose) {
        return runChangeCall(() -> orderToClose.close(),
                             orderToClose,
                             OrderCallRequest.CLOSE);
    }

    public Observable<OrderEvent> setLabel(final IOrder orderToChangeLabel,
                                           final String newLabel) {
        return runChangeCall(() -> orderToChangeLabel.setLabel(newLabel),
                             orderToChangeLabel,
                             OrderCallRequest.CHANGE_LABEL);
    }

    public Observable<OrderEvent> setGTT(final IOrder orderToChangeGTT,
                                         final long newGTT) {
        return runChangeCall(() -> orderToChangeGTT.setGoodTillTime(newGTT),
                             orderToChangeGTT,
                             OrderCallRequest.CHANGE_GTT);
    }

    public Observable<OrderEvent> setOpenPrice(final IOrder orderToChangeOpenPrice,
                                               final double newOpenPrice) {
        return runChangeCall(() -> orderToChangeOpenPrice.setOpenPrice(newOpenPrice),
                             orderToChangeOpenPrice,
                             OrderCallRequest.CHANGE_OPENPRICE);
    }

    public Observable<OrderEvent> setAmount(final IOrder orderToChangeAmount,
                                            final double newAmount) {
        return runChangeCall(() -> orderToChangeAmount.setRequestedAmount(newAmount),
                             orderToChangeAmount,
                             OrderCallRequest.CHANGE_AMOUNT);
    }

    public Observable<OrderEvent> setSL(final IOrder orderToChangeSL,
                                        final double newSL) {
        return runChangeCall(() -> orderToChangeSL.setStopLossPrice(newSL),
                             orderToChangeSL,
                             OrderCallRequest.CHANGE_SL);
    }

    public Observable<OrderEvent> setTP(final IOrder orderToChangeTP,
                                        final double newTP) {
        return runChangeCall(() -> orderToChangeTP.setTakeProfitPrice(newTP),
                             orderToChangeTP,
                             OrderCallRequest.CHANGE_TP);
    }

    public Observable<OrderEvent> setSLWithPips(final IOrder orderToChangeSL,
                                                final double referencePrice,
                                                final double pips) {
        final int pipFactor = orderToChangeSL.isLong() ? shortFactor : longFactor;
        final double newSL = CalculationUtil.addPips(orderToChangeSL.getInstrument(),
                                                     referencePrice,
                                                     pipFactor * pips);
        return setSL(orderToChangeSL, newSL);
    }

    public Observable<OrderEvent> setTPWithPips(final IOrder orderToChangeTP,
                                                final double referencePrice,
                                                final double pips) {
        final int pipFactor = orderToChangeTP.isLong() ? longFactor : shortFactor;
        final double newTP = CalculationUtil.addPips(orderToChangeTP.getInstrument(),
                                                     referencePrice,
                                                     pipFactor * pips);
        return setTP(orderToChangeTP, newTP);
    }

    private Observable<OrderEvent> runChangeCall(final OrderChangeCall orderChangeCall,
                                                 final IOrder orderToChange,
                                                 final OrderCallRequest orderCallRequest) {
        final OrderCallExecutorResult executorResult = orderCallExecutor.run(() -> {
            orderChangeCall.change();
            return orderToChange;
        });
        registerOrderCallRequest(executorResult, orderCallRequest);
        return executorResult.exceptionOpt().isPresent()
                ? Observable.error(executorResult.exceptionOpt().get())
                : orderEventGateway
                        .observable()
                        .filter(orderEvent -> orderEvent.order() == orderToChange);
    }

    private void registerOrderCallRequest(final OrderCallExecutorResult orderExecutorResult,
                                          final OrderCallRequest orderCallRequest) {
        if (orderExecutorResult.orderOpt().isPresent())
            orderEventGateway.registerOrderRequest(orderExecutorResult.orderOpt().get(),
                                                   orderCallRequest);
    }
}
