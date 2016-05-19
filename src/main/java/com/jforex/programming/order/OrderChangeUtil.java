package com.jforex.programming.order;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.event.OrderEventTypeData;

import com.dukascopy.api.IOrder;

import rx.Observable;

public class OrderChangeUtil {

    private final OrderUtilHandler orderUtilHandler;

    private static final Logger logger = LogManager.getLogger(OrderChangeUtil.class);

    public OrderChangeUtil(final OrderUtilHandler orderUtilHandler) {
        this.orderUtilHandler = orderUtilHandler;
    }

    public Observable<OrderEvent> close(final IOrder orderToClose) {
        final Observable<OrderEvent> closeObs =
                orderUtilHandler.runOrderChangeCall(() -> orderToClose.close(),
                                                    orderToClose,
                                                    OrderEventTypeData.closeData);
        closeObs.doOnCompleted(() -> logger.debug("Closing " + orderToClose.getLabel() + " was successful."))
                .subscribe(orderEvent -> {},
                           e -> logger.error("Closing " + orderToClose.getLabel() + " failed!"));
        return closeObs;
    }

    public Observable<OrderEvent> setLabel(final IOrder orderToChangeLabel,
                                           final String newLabel) {
        return orderUtilHandler.runOrderChangeCall(() -> orderToChangeLabel.setLabel(newLabel),
                                                   orderToChangeLabel,
                                                   OrderEventTypeData.changeLabelData);
    }

    public Observable<OrderEvent> setGoodTillTime(final IOrder orderToChangeGTT,
                                                  final long newGTT) {
        return orderUtilHandler.runOrderChangeCall(() -> orderToChangeGTT.setGoodTillTime(newGTT),
                                                   orderToChangeGTT,
                                                   OrderEventTypeData.changeGTTData);
    }

    public Observable<OrderEvent> setOpenPrice(final IOrder orderToChangeOpenPrice,
                                               final double newOpenPrice) {
        return orderUtilHandler.runOrderChangeCall(() -> orderToChangeOpenPrice.setOpenPrice(newOpenPrice),
                                                   orderToChangeOpenPrice,
                                                   OrderEventTypeData.changeOpenPriceData);
    }

    public Observable<OrderEvent> setRequestedAmount(final IOrder orderToChangeAmount,
                                                     final double newAmount) {
        return orderUtilHandler.runOrderChangeCall(() -> orderToChangeAmount.setRequestedAmount(newAmount),
                                                   orderToChangeAmount,
                                                   OrderEventTypeData.changeAmountData);
    }

    public Observable<OrderEvent> setStopLossPrice(final IOrder orderToChangeSL,
                                                   final double newSL) {
        return orderUtilHandler.runOrderChangeCall(() -> orderToChangeSL.setStopLossPrice(newSL),
                                                   orderToChangeSL,
                                                   OrderEventTypeData.changeSLData);
    }

    public Observable<OrderEvent> setTakeProfitPrice(final IOrder orderToChangeTP,
                                                     final double newTP) {
        return orderUtilHandler.runOrderChangeCall(() -> orderToChangeTP.setTakeProfitPrice(newTP),
                                                   orderToChangeTP,
                                                   OrderEventTypeData.changeTPData);
    }
}
