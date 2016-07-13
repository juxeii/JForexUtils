package com.jforex.programming.order;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.event.OrderEventTypeData;

import rx.Observable;

public class OrderChangeUtil {

    private final OrderUtilHandler orderUtilHandler;

    private static final Logger logger = LogManager.getLogger(OrderUtilHandler.class);

    public OrderChangeUtil(final OrderUtilHandler orderUtilHandler) {
        this.orderUtilHandler = orderUtilHandler;
    }

    public Observable<OrderEvent> close(final IOrder orderToClose) {
        final String commonLog = "order " + orderToClose.getLabel() + " for instrument "
                + orderToClose.getInstrument();

        return orderUtilHandler
                .changeObservable(() -> orderToClose.close(),
                                  orderToClose,
                                  OrderEventTypeData.closeData)
                .doOnSubscribe(() -> logger.info("Starting to close " + commonLog))
                .doOnError(e -> logger
                        .error("Failed to close " + commonLog + "!Exception: " + e.getMessage()))
                .doOnCompleted(() -> logger.info("Closed " + commonLog));
    }

    public Observable<OrderEvent> setLabel(final IOrder orderToChangeLabel,
                                           final String newLabel) {
        final Observable<OrderEvent> observable =
                orderUtilHandler.changeObservable(() -> orderToChangeLabel.setLabel(newLabel),
                                                  orderToChangeLabel,
                                                  OrderEventTypeData.changeLabelData);

        return appendLogsToObsverable(orderToChangeLabel,
                                      observable,
                                      "label",
                                      orderToChangeLabel.getLabel(),
                                      newLabel);
    }

    public Observable<OrderEvent> setGoodTillTime(final IOrder orderToChangeGTT,
                                                  final long newGTT) {
        final Observable<OrderEvent> observable =
                orderUtilHandler.changeObservable(() -> orderToChangeGTT.setGoodTillTime(newGTT),
                                                  orderToChangeGTT,
                                                  OrderEventTypeData.changeGTTData);

        return appendLogsToObsverable(orderToChangeGTT,
                                      observable,
                                      "GTT",
                                      orderToChangeGTT.getGoodTillTime(),
                                      newGTT);
    }

    public Observable<OrderEvent> setOpenPrice(final IOrder orderToChangeOpenPrice,
                                               final double newOpenPrice) {
        final Observable<OrderEvent> observable =
                orderUtilHandler
                        .changeObservable(() -> orderToChangeOpenPrice.setOpenPrice(newOpenPrice),
                                          orderToChangeOpenPrice,
                                          OrderEventTypeData.changeOpenPriceData);

        return appendLogsToObsverable(orderToChangeOpenPrice,
                                      observable,
                                      "open price",
                                      orderToChangeOpenPrice.getOpenPrice(),
                                      newOpenPrice);
    }

    public Observable<OrderEvent> setRequestedAmount(final IOrder orderToChangeAmount,
                                                     final double newAmount) {
        final Observable<OrderEvent> observable =
                orderUtilHandler
                        .changeObservable(() -> orderToChangeAmount.setRequestedAmount(newAmount),
                                          orderToChangeAmount,
                                          OrderEventTypeData.changeAmountData);

        return appendLogsToObsverable(orderToChangeAmount,
                                      observable,
                                      "amount",
                                      orderToChangeAmount.getAmount(),
                                      newAmount);
    }

    public Observable<OrderEvent> setStopLossPrice(final IOrder orderToChangeSL,
                                                   final double newSL) {
        final Observable<OrderEvent> observable =
                orderUtilHandler.changeObservable(() -> orderToChangeSL.setStopLossPrice(newSL),
                                                  orderToChangeSL,
                                                  OrderEventTypeData.changeSLData);

        return appendLogsToObsverable(orderToChangeSL,
                                      observable,
                                      "SL",
                                      orderToChangeSL.getStopLossPrice(),
                                      newSL);
    }

    public Observable<OrderEvent> setTakeProfitPrice(final IOrder orderToChangeTP,
                                                     final double newTP) {
        final Observable<OrderEvent> observable =
                orderUtilHandler.changeObservable(() -> orderToChangeTP.setTakeProfitPrice(newTP),
                                                  orderToChangeTP,
                                                  OrderEventTypeData.changeTPData);

        return appendLogsToObsverable(orderToChangeTP,
                                      observable,
                                      "TP",
                                      orderToChangeTP.getTakeProfitPrice(),
                                      newTP);
    }

    private <T> Observable<OrderEvent> appendLogsToObsverable(final IOrder orderToChange,
                                                              final Observable<OrderEvent> observable,
                                                              final String valueName,
                                                              final T currentValue,
                                                              final T newValue) {
        final String commonLog =
                valueName + " from " + currentValue + " to " + newValue + " for order "
                        + orderToChange.getLabel() + " and instrument "
                        + orderToChange.getInstrument();

        return observable
                .doOnSubscribe(() -> logger.info("Start to change " + commonLog))
                .doOnError(e -> logger.debug("Failed to change " + commonLog
                        + "!Excpetion: " + e.getMessage()))
                .doOnCompleted(() -> logger.info("Changed " + commonLog));
    }
}
