package com.jforex.programming.order.command;

import java.util.concurrent.Callable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.jforex.programming.order.event.OrderEventTypeData;

import com.dukascopy.api.IOrder;

public abstract class OrderCallCommand {

    private final Callable<IOrder> callable;
    private final OrderEventTypeData orderEventTypeData;

    protected static final Logger logger = LogManager.getLogger(OrderCallCommand.class);

    public OrderCallCommand(final Callable<IOrder> callable,
                            final OrderEventTypeData orderEventTypeData) {
        this.callable = callable;
        this.orderEventTypeData = orderEventTypeData;
    }

    public Callable<IOrder> callable() {
        return callable;
    }

    public OrderEventTypeData orderEventTypeData() {
        return orderEventTypeData;
    }

    public void logOnSubscribe() {
        logger.info(subscribeLog());
    }

    public void logOnError(final Throwable e) {
        logger.error(errorLog(e));
    }

    public void logOnCompleted() {
        logger.info(completedLog());
    }

    protected abstract String subscribeLog();

    protected abstract String errorLog(final Throwable e);

    protected abstract String completedLog();

    public static final OrderChangeCommand<IOrder.State> closeCommand(final IOrder orderToClose) {
        return new OrderChangeCommand<IOrder.State>(orderToClose,
                                                    () -> orderToClose.close(),
                                                    OrderEventTypeData.closeData,
                                                    orderToClose.getState(),
                                                    IOrder.State.CLOSED,
                                                    "order state");
    }

    public static final OrderChangeCommand<String> setLabelCommand(final IOrder orderToChangeLabel,
                                                                   final String newLabel) {
        return new OrderChangeCommand<String>(orderToChangeLabel,
                                              () -> orderToChangeLabel.setLabel(newLabel),
                                              OrderEventTypeData.changeLabelData,
                                              orderToChangeLabel.getLabel(),
                                              newLabel,
                                              "label");
    }

    public static final OrderChangeCommand<Long> setGTTCommand(final IOrder orderToChangeGTT,
                                                               final long newGTT) {
        return new OrderChangeCommand<Long>(orderToChangeGTT,
                                            () -> orderToChangeGTT.setGoodTillTime(newGTT),
                                            OrderEventTypeData.changeGTTData,
                                            orderToChangeGTT.getGoodTillTime(),
                                            newGTT,
                                            "GTT");
    }

    public static final OrderChangeCommand<Double> setOpenPriceCommand(final IOrder orderToChangeOpenPrice,
                                                                       final double newOpenPrice) {
        return new OrderChangeCommand<Double>(orderToChangeOpenPrice,
                                              () -> orderToChangeOpenPrice.setOpenPrice(newOpenPrice),
                                              OrderEventTypeData.changeOpenPriceData,
                                              orderToChangeOpenPrice.getOpenPrice(),
                                              newOpenPrice,
                                              "open price");
    }

    public static final OrderChangeCommand<Double> setAmountCommand(final IOrder orderToChangeAmount,
                                                                    final double newAmount) {
        return new OrderChangeCommand<Double>(orderToChangeAmount,
                                              () -> orderToChangeAmount.setRequestedAmount(newAmount),
                                              OrderEventTypeData.changeAmountData,
                                              orderToChangeAmount.getRequestedAmount(),
                                              newAmount,
                                              "amount");
    }

    public static final OrderChangeCommand<Double> setSLCommand(final IOrder orderToChangeSL,
                                                                final double newSL) {
        return new OrderChangeCommand<Double>(orderToChangeSL,
                                              () -> orderToChangeSL.setStopLossPrice(newSL),
                                              OrderEventTypeData.changeSLData,
                                              orderToChangeSL.getStopLossPrice(),
                                              newSL,
                                              "SL");
    }

    public static final OrderChangeCommand<Double> setTPCommand(final IOrder orderToChangeTP,
                                                                final double newTP) {
        return new OrderChangeCommand<Double>(orderToChangeTP,
                                              () -> orderToChangeTP.setTakeProfitPrice(newTP),
                                              OrderEventTypeData.changeTPData,
                                              orderToChangeTP.getTakeProfitPrice(),
                                              newTP,
                                              "TP");
    }
}
