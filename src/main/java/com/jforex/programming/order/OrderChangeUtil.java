package com.jforex.programming.order;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.event.OrderEventTypeData;

import rx.Observable;

public class OrderChangeUtil {

    private final OrderUtilHandler orderUtilHandler;

    public OrderChangeUtil(final OrderUtilHandler orderUtilHandler) {
        this.orderUtilHandler = orderUtilHandler;
    }

    public Observable<OrderEvent> close(final IOrder orderToClose) {
        return orderUtilHandler.changeObservable(() -> orderToClose.close(),
                                                 orderToClose,
                                                 OrderEventTypeData.closeData);
    }

    public Observable<OrderEvent> setLabel(final IOrder orderToChangeLabel,
                                           final String newLabel) {
        return orderUtilHandler.changeObservable(() -> orderToChangeLabel.setLabel(newLabel),
                                                 orderToChangeLabel,
                                                 OrderEventTypeData.changeLabelData);
    }

    public Observable<OrderEvent> setGoodTillTime(final IOrder orderToChangeGTT,
                                                  final long newGTT) {
        return orderUtilHandler.changeObservable(() -> orderToChangeGTT.setGoodTillTime(newGTT),
                                                 orderToChangeGTT,
                                                 OrderEventTypeData.changeGTTData);
    }

    public Observable<OrderEvent> setOpenPrice(final IOrder orderToChangeOpenPrice,
                                               final double newOpenPrice) {
        return orderUtilHandler.changeObservable(() -> orderToChangeOpenPrice.setOpenPrice(newOpenPrice),
                                                 orderToChangeOpenPrice,
                                                 OrderEventTypeData.changeOpenPriceData);
    }

    public Observable<OrderEvent> setRequestedAmount(final IOrder orderToChangeAmount,
                                                     final double newAmount) {
        return orderUtilHandler.changeObservable(() -> orderToChangeAmount.setRequestedAmount(newAmount),
                                                 orderToChangeAmount,
                                                 OrderEventTypeData.changeAmountData);
    }

    public Observable<OrderEvent> setStopLossPrice(final IOrder orderToChangeSL,
                                                   final double newSL) {
        return orderUtilHandler.changeObservable(() -> orderToChangeSL.setStopLossPrice(newSL),
                                                 orderToChangeSL,
                                                 OrderEventTypeData.changeSLData);
    }

    public Observable<OrderEvent> setTakeProfitPrice(final IOrder orderToChangeTP,
                                                     final double newTP) {
        return orderUtilHandler.changeObservable(() -> orderToChangeTP.setTakeProfitPrice(newTP),
                                                 orderToChangeTP,
                                                 OrderEventTypeData.changeTPData);
    }
}
