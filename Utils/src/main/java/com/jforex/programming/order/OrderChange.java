package com.jforex.programming.order;

import com.dukascopy.api.IOrder;
import com.jforex.programming.misc.CalculationUtil;
import com.jforex.programming.order.call.OrderCallExecutor;
import com.jforex.programming.order.call.OrderCallExecutorResult;
import com.jforex.programming.order.call.OrderCallRequest;
import com.jforex.programming.order.call.OrderChangeCall;
import com.jforex.programming.order.event.OrderEventGateway;

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
        return orderChangeResult(() -> orderToClose.close(),
                                 orderToClose,
                                 OrderCallRequest.CLOSE);
    }

    public OrderChangeResult setLabel(final IOrder orderToChangeLabel,
                                      final String newLabel) {
        return orderChangeResult(() -> orderToChangeLabel.setLabel(newLabel),
                                 orderToChangeLabel,
                                 OrderCallRequest.CHANGE_LABEL);
    }

    public OrderChangeResult setGTT(final IOrder orderToChangeGTT,
                                    final long newGTT) {
        return orderChangeResult(() -> orderToChangeGTT.setGoodTillTime(newGTT),
                                 orderToChangeGTT,
                                 OrderCallRequest.CHANGE_GTT);
    }

    public OrderChangeResult setOpenPrice(final IOrder orderToChangeOpenPrice,
                                          final double newOpenPrice) {
        return orderChangeResult(() -> orderToChangeOpenPrice.setOpenPrice(newOpenPrice),
                                 orderToChangeOpenPrice,
                                 OrderCallRequest.CHANGE_OPENPRICE);
    }

    public OrderChangeResult setAmount(final IOrder orderToChangeAmount,
                                       final double newAmount) {
        return orderChangeResult(() -> orderToChangeAmount.setRequestedAmount(newAmount),
                                 orderToChangeAmount,
                                 OrderCallRequest.CHANGE_AMOUNT);
    }

    public OrderChangeResult setSL(final IOrder orderToChangeSL,
                                   final double newSL) {
        return orderChangeResult(() -> orderToChangeSL.setStopLossPrice(newSL),
                                 orderToChangeSL,
                                 OrderCallRequest.CHANGE_SL);
    }

    public OrderChangeResult setTP(final IOrder orderToChangeTP,
                                   final double newTP) {
        return orderChangeResult(() -> orderToChangeTP.setTakeProfitPrice(newTP),
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
