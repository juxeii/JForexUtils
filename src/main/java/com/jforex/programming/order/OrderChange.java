package com.jforex.programming.order;

import java.util.Optional;

import com.jforex.programming.misc.CalculationUtil;
import com.jforex.programming.order.call.OrderCallExecutor;
import com.jforex.programming.order.call.OrderCallExecutorResult;
import com.jforex.programming.order.call.OrderCallRequest;
import com.jforex.programming.order.call.OrderChangeCall;
import com.jforex.programming.order.event.OrderEventGateway;

import com.dukascopy.api.IOrder;

public class OrderChange {

    private final OrderCallExecutor orderCallExecutor;
    private final OrderEventGateway orderEventGateway;

    private final static int longFactor = 1;
    private final static int shortFactor = -1;

    public OrderChange(final OrderCallExecutor orderCallExecutor,
                       final OrderEventGateway orderEventGateway) {
        this.orderCallExecutor = orderCallExecutor;
        this.orderEventGateway = orderEventGateway;
    }

    public Optional<Exception> close(final IOrder orderToClose) {
        return runChangeCall(() -> orderToClose.close(),
                             orderToClose,
                             OrderCallRequest.CLOSE);
    }

    public Optional<Exception> setLabel(final IOrder orderToChangeLabel,
                                        final String newLabel) {
        return runChangeCall(() -> orderToChangeLabel.setLabel(newLabel),
                             orderToChangeLabel,
                             OrderCallRequest.CHANGE_LABEL);
    }

    public Optional<Exception> setGTT(final IOrder orderToChangeGTT,
                                      final long newGTT) {
        return runChangeCall(() -> orderToChangeGTT.setGoodTillTime(newGTT),
                             orderToChangeGTT,
                             OrderCallRequest.CHANGE_GTT);
    }

    public Optional<Exception> setOpenPrice(final IOrder orderToChangeOpenPrice,
                                            final double newOpenPrice) {
        return runChangeCall(() -> orderToChangeOpenPrice.setOpenPrice(newOpenPrice),
                             orderToChangeOpenPrice,
                             OrderCallRequest.CHANGE_OPENPRICE);
    }

    public Optional<Exception> setAmount(final IOrder orderToChangeAmount,
                                         final double newAmount) {
        return runChangeCall(() -> orderToChangeAmount.setRequestedAmount(newAmount),
                             orderToChangeAmount,
                             OrderCallRequest.CHANGE_AMOUNT);
    }

    public Optional<Exception> setSL(final IOrder orderToChangeSL,
                                     final double newSL) {
        return runChangeCall(() -> orderToChangeSL.setStopLossPrice(newSL),
                             orderToChangeSL,
                             OrderCallRequest.CHANGE_SL);
    }

    public Optional<Exception> setTP(final IOrder orderToChangeTP,
                                     final double newTP) {
        return runChangeCall(() -> orderToChangeTP.setTakeProfitPrice(newTP),
                             orderToChangeTP,
                             OrderCallRequest.CHANGE_TP);
    }

    public Optional<Exception> setSLWithPips(final IOrder orderToChangeSL,
                                             final double referencePrice,
                                             final double pips) {
        final int pipFactor = orderToChangeSL.isLong() ? shortFactor : longFactor;
        final double newSL = CalculationUtil.addPips(orderToChangeSL.getInstrument(),
                                                     referencePrice,
                                                     pipFactor * pips);
        return setSL(orderToChangeSL, newSL);
    }

    public Optional<Exception> setTPWithPips(final IOrder orderToChangeTP,
                                             final double referencePrice,
                                             final double pips) {
        final int pipFactor = orderToChangeTP.isLong() ? longFactor : shortFactor;
        final double newTP = CalculationUtil.addPips(orderToChangeTP.getInstrument(),
                                                     referencePrice,
                                                     pipFactor * pips);
        return setTP(orderToChangeTP, newTP);
    }

    private Optional<Exception> runChangeCall(final OrderChangeCall orderChangeCall,
                                              final IOrder orderToChange,
                                              final OrderCallRequest orderCallRequest) {
        final OrderCallExecutorResult orderCallExecutorResult = orderCallExecutor.run(() -> {
            orderChangeCall.change();
            return orderToChange;
        });
        registerCallRequest(orderCallExecutorResult, orderCallRequest);
        return orderCallExecutorResult.exceptionOpt();
    }

    private void registerCallRequest(final OrderCallExecutorResult orderCallExecutorResult,
                                     final OrderCallRequest orderCallRequest) {
        if (!orderCallExecutorResult.exceptionOpt().isPresent())
            orderEventGateway.registerOrderRequest(orderCallExecutorResult.orderOpt().get(),
                                                   orderCallRequest);
    }
}
