package com.jforex.programming.order;

import static com.jforex.programming.order.OrderStaticUtil.isClosed;
import static com.jforex.programming.order.OrderStaticUtil.isSLSetTo;
import static com.jforex.programming.order.OrderStaticUtil.isTPSetTo;

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
        return isClosed.test(orderToClose)
                ? Optional.empty()
                : orderChangeResult(() -> orderToClose.close(),
                                    orderToClose,
                                    OrderCallRequest.CLOSE);
    }

    public Optional<Exception> setLabel(final IOrder orderToChangeLabel,
                                        final String newLabel) {
        return orderToChangeLabel.getLabel().equals(newLabel)
                ? Optional.empty()
                : orderChangeResult(() -> orderToChangeLabel.setLabel(newLabel),
                                    orderToChangeLabel,
                                    OrderCallRequest.CHANGE_LABEL);
    }

    public Optional<Exception> setGTT(final IOrder orderToChangeGTT,
                                      final long newGTT) {
        return orderToChangeGTT.getGoodTillTime() == newGTT
                ? Optional.empty()
                : orderChangeResult(() -> orderToChangeGTT.setGoodTillTime(newGTT),
                                    orderToChangeGTT,
                                    OrderCallRequest.CHANGE_GTT);
    }

    public Optional<Exception> setOpenPrice(final IOrder orderToChangeOpenPrice,
                                            final double newOpenPrice) {
        return orderToChangeOpenPrice.getOpenPrice() == newOpenPrice
                ? Optional.empty()
                : orderChangeResult(() -> orderToChangeOpenPrice.setOpenPrice(newOpenPrice),
                                    orderToChangeOpenPrice,
                                    OrderCallRequest.CHANGE_OPENPRICE);
    }

    public Optional<Exception> setAmount(final IOrder orderToChangeAmount,
                                         final double newAmount) {
        return orderToChangeAmount.getAmount() == newAmount
                ? Optional.empty()
                : orderChangeResult(() -> orderToChangeAmount.setRequestedAmount(newAmount),
                                    orderToChangeAmount,
                                    OrderCallRequest.CHANGE_AMOUNT);
    }

    public Optional<Exception> setSL(final IOrder orderToChangeSL,
                                     final double newSL) {
        return isSLSetTo(newSL).test(orderToChangeSL)
                ? Optional.empty()
                : orderChangeResult(() -> orderToChangeSL.setStopLossPrice(newSL),
                                    orderToChangeSL,
                                    OrderCallRequest.CHANGE_SL);
    }

    public Optional<Exception> setTP(final IOrder orderToChangeTP,
                                     final double newTP) {
        return isTPSetTo(newTP).test(orderToChangeTP)
                ? Optional.empty()
                : orderChangeResult(() -> orderToChangeTP.setTakeProfitPrice(newTP),
                                    orderToChangeTP,
                                    OrderCallRequest.CHANGE_TP);
    }

    public Optional<Exception> setSLInPips(final IOrder orderToChangeSL,
                                           final double referencePrice,
                                           final double pips) {
        final int pipFactor = orderToChangeSL.isLong() ? shortFactor : longFactor;
        final double newSL = CalculationUtil.addPips(orderToChangeSL.getInstrument(),
                                                     referencePrice,
                                                     pipFactor * pips);
        return setSL(orderToChangeSL, newSL);
    }

    public Optional<Exception> setTPInPips(final IOrder orderToChangeTP,
                                           final double referencePrice,
                                           final double pips) {
        final int pipFactor = orderToChangeTP.isLong() ? longFactor : shortFactor;
        final double newTP = CalculationUtil.addPips(orderToChangeTP.getInstrument(),
                                                     referencePrice,
                                                     pipFactor * pips);
        return setTP(orderToChangeTP, newTP);
    }

    private Optional<Exception> orderChangeResult(final OrderChangeCall orderChangeCall,
                                                  final IOrder orderToChange,
                                                  final OrderCallRequest orderCallRequest) {
        final OrderCallExecutorResult orderCallExecutorResult = callResultFromExecutorResult(orderChangeCall,
                                                                                             orderToChange,
                                                                                             orderCallRequest);
        registerChangeResult(orderCallExecutorResult, orderCallRequest);
        return orderCallExecutorResult.exceptionOpt();
    }

    private OrderCallExecutorResult callResultFromExecutorResult(final OrderChangeCall orderChangeCall,
                                                                 final IOrder orderToChange,
                                                                 final OrderCallRequest orderCallRequest) {
        return orderCallExecutor.run(() -> {
            orderChangeCall.change();
            return orderToChange;
        });
    }

    private void registerChangeResult(final OrderCallExecutorResult orderCallExecutorResult,
                                      final OrderCallRequest orderCallRequest) {
        if (!orderCallExecutorResult.exceptionOpt().isPresent())
            orderEventGateway.registerOrderRequest(orderCallExecutorResult.orderOpt().get(),
                                                   orderCallRequest);
    }
}
