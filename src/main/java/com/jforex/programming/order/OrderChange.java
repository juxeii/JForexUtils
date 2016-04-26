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

    public OrderChangeResult close(final IOrder orderToClose) {
        return isClosed.test(orderToClose)
                ? new OrderChangeResult(orderToClose, Optional.empty(), OrderCallRequest.CLOSE)
                : orderChangeResult(() -> orderToClose.close(),
                                    orderToClose,
                                    OrderCallRequest.CLOSE);
    }

    public OrderChangeResult setLabel(final IOrder orderToChangeLabel,
                                      final String newLabel) {
        return orderToChangeLabel.getLabel().equals(newLabel)
                ? new OrderChangeResult(orderToChangeLabel, Optional.empty(), OrderCallRequest.CHANGE_LABEL)
                : orderChangeResult(() -> orderToChangeLabel.setLabel(newLabel),
                                    orderToChangeLabel,
                                    OrderCallRequest.CHANGE_LABEL);
    }

    public OrderChangeResult setGTT(final IOrder orderToChangeGTT,
                                    final long newGTT) {
        return orderToChangeGTT.getGoodTillTime() == newGTT
                ? new OrderChangeResult(orderToChangeGTT, Optional.empty(), OrderCallRequest.CHANGE_GTT)
                : orderChangeResult(() -> orderToChangeGTT.setGoodTillTime(newGTT),
                                    orderToChangeGTT,
                                    OrderCallRequest.CHANGE_GTT);
    }

    public OrderChangeResult setOpenPrice(final IOrder orderToChangeOpenPrice,
                                          final double newOpenPrice) {
        return orderToChangeOpenPrice.getOpenPrice() == newOpenPrice
                ? new OrderChangeResult(orderToChangeOpenPrice, Optional.empty(), OrderCallRequest.CHANGE_OPENPRICE)
                : orderChangeResult(() -> orderToChangeOpenPrice.setOpenPrice(newOpenPrice),
                                    orderToChangeOpenPrice,
                                    OrderCallRequest.CHANGE_OPENPRICE);
    }

    public OrderChangeResult setAmount(final IOrder orderToChangeAmount,
                                       final double newAmount) {
        return orderToChangeAmount.getAmount() == newAmount
                ? new OrderChangeResult(orderToChangeAmount, Optional.empty(), OrderCallRequest.CHANGE_AMOUNT)
                : orderChangeResult(() -> orderToChangeAmount.setRequestedAmount(newAmount),
                                    orderToChangeAmount,
                                    OrderCallRequest.CHANGE_AMOUNT);
    }

    public OrderChangeResult setSL(final IOrder orderToChangeSL,
                                   final double newSL) {
        return isSLSetTo(newSL).test(orderToChangeSL)
                ? new OrderChangeResult(orderToChangeSL, Optional.empty(), OrderCallRequest.CHANGE_SL)
                : orderChangeResult(() -> orderToChangeSL.setStopLossPrice(newSL),
                                    orderToChangeSL,
                                    OrderCallRequest.CHANGE_SL);
    }

    public OrderChangeResult setTP(final IOrder orderToChangeTP,
                                   final double newTP) {
        return isTPSetTo(newTP).test(orderToChangeTP)
                ? new OrderChangeResult(orderToChangeTP, Optional.empty(), OrderCallRequest.CHANGE_TP)
                : orderChangeResult(() -> orderToChangeTP.setTakeProfitPrice(newTP),
                                    orderToChangeTP,
                                    OrderCallRequest.CHANGE_TP);
    }

    public OrderChangeResult setSLInPips(final IOrder orderToChangeSL,
                                         final double referencePrice,
                                         final double pips) {
        final int pipFactor = orderToChangeSL.isLong() ? shortFactor : longFactor;
        final double newSL = CalculationUtil.addPips(orderToChangeSL.getInstrument(),
                                                     referencePrice,
                                                     pipFactor * pips);
        return setSL(orderToChangeSL, newSL);
    }

    public OrderChangeResult setTPInPips(final IOrder orderToChangeTP,
                                         final double referencePrice,
                                         final double pips) {
        final int pipFactor = orderToChangeTP.isLong() ? longFactor : shortFactor;
        final double newTP = CalculationUtil.addPips(orderToChangeTP.getInstrument(),
                                                     referencePrice,
                                                     pipFactor * pips);
        return setTP(orderToChangeTP, newTP);
    }

    private OrderChangeResult orderChangeResult(final OrderChangeCall orderChangeCall,
                                                final IOrder orderToChange,
                                                final OrderCallRequest orderCallRequest) {
        final OrderChangeResult orderChangeResult = callResultFromExecutorResult(orderChangeCall,
                                                                                 orderToChange,
                                                                                 orderCallRequest);
        registerChangeResult(orderChangeResult);
        return orderChangeResult;
    }

    private OrderChangeResult callResultFromExecutorResult(final OrderChangeCall orderChangeCall,
                                                           final IOrder orderToChange,
                                                           final OrderCallRequest orderCallRequest) {
        final OrderCallExecutorResult orderExecutorResult =
                orderCallExecutor.run(() -> {
                    orderChangeCall.change();
                    return orderToChange;
                });
        return new OrderChangeResult(orderToChange,
                                     orderExecutorResult.exceptionOpt(),
                                     orderCallRequest);
    }

    private void registerChangeResult(final OrderChangeResult orderChangeResult) {
        if (!orderChangeResult.exceptionOpt().isPresent())
            orderEventGateway.registerOrderRequest(orderChangeResult.order(),
                                                   orderChangeResult.callRequest());
    }
}
