package com.jforex.programming.order;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.event.OrderEventTypeData;

import com.dukascopy.api.IOrder;
import com.dukascopy.api.Instrument;

import rx.Observable;

public class OrderChangeUtil {

    private final OrderUtilHandler orderUtilHandler;

    private static final Logger logger = LogManager.getLogger(OrderUtilHandler.class);

    public OrderChangeUtil(final OrderUtilHandler orderUtilHandler) {
        this.orderUtilHandler = orderUtilHandler;
    }

    public Observable<OrderEvent> close(final IOrder orderToClose) {
        final Instrument instrument = orderToClose.getInstrument();
        final String orderLabel = orderToClose.getLabel();

        return orderUtilHandler
                .changeObservable(() -> orderToClose.close(),
                                  orderToClose,
                                  OrderEventTypeData.closeData)
                .doOnSubscribe(() -> logger.debug("Starting to close order " + orderLabel
                        + " for instrument " + instrument))
                .doOnError(e -> logger.error("Closing order " + orderLabel
                        + " for instrument " + instrument + " failed! Exception: " + e.getMessage()))
                .doOnCompleted(() -> logger.debug("Closing order " + orderLabel + " for instrument "
                        + instrument + " was successful."));
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
        final Instrument instrument = orderToChangeSL.getInstrument();
        final String orderLabel = orderToChangeSL.getLabel();
        final double currentSL = orderToChangeSL.getStopLossPrice();

        return orderUtilHandler
                .changeObservable(() -> orderToChangeSL.setStopLossPrice(newSL),
                                  orderToChangeSL,
                                  OrderEventTypeData.changeSLData)
                .doOnSubscribe(() -> logger.debug("Start to change SL from " + currentSL + " to "
                        + newSL + " for order " + orderLabel + " and instrument " + instrument))
                .doOnError(e -> logger.debug("Failed to change SL from " + currentSL + " to " + newSL +
                        " for order " + orderLabel + " and instrument "
                        + instrument + ".Excpetion: " + e.getMessage()))
                .doOnCompleted(() -> logger.debug("Changed SL from " + currentSL + " to " + newSL +
                        " for order " + orderLabel + " and instrument " + instrument));
    }

    public Observable<OrderEvent> setTakeProfitPrice(final IOrder orderToChangeTP,
                                                     final double newTP) {
        final Instrument instrument = orderToChangeTP.getInstrument();
        final String orderLabel = orderToChangeTP.getLabel();
        final double currentTP = orderToChangeTP.getTakeProfitPrice();

        return orderUtilHandler
                .changeObservable(() -> orderToChangeTP.setTakeProfitPrice(newTP),
                                  orderToChangeTP,
                                  OrderEventTypeData.changeTPData)
                .doOnSubscribe(() -> logger.debug("Start to change TP from " + currentTP + " to "
                        + newTP + " for order " + orderLabel + " and instrument " + instrument))
                .doOnError(e -> logger.debug("Failed to change TP from " + currentTP + " to " + newTP +
                        " for order " + orderLabel + " and instrument "
                        + instrument + ".Excpetion: " + e.getMessage()))
                .doOnCompleted(() -> logger.debug("Changed TP from " + currentTP + " to " + newTP +
                        " for order " + orderLabel + " and instrument " + instrument));
    }
}
