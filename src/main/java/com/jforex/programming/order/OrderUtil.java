package com.jforex.programming.order;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.jforex.programming.order.OrderStaticUtil.instrumentFromOrders;

import java.util.Collection;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dukascopy.api.IOrder;
import com.dukascopy.api.Instrument;
import com.jforex.programming.order.builder.MergeBuilder;
import com.jforex.programming.order.builder.MergeBuilder.MergeOption;
import com.jforex.programming.order.builder.SubmitBuilder;
import com.jforex.programming.order.builder.SubmitBuilder.SubmitOption;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.event.OrderEventType;
import com.jforex.programming.position.PositionOrders;

import rx.Observable;

public class OrderUtil {

    private final OrderUtilImpl orderUtilImpl;

    private static final Logger logger = LogManager.getLogger(OrderUtil.class);

    public OrderUtil(final OrderUtilImpl orderUtilImpl) {
        this.orderUtilImpl = orderUtilImpl;
    }

    public PositionOrders positionOrders(final Instrument instrument) {
        return orderUtilImpl.positionOrders(checkNotNull(instrument));
    }

    public Observable<OrderEvent> submitOrder(final OrderParams orderParams) {
        checkNotNull(orderParams);

        final Instrument instrument = orderParams.instrument();
        final String orderLabel = orderParams.label();

        return orderUtilImpl.submitOrder(orderParams)
                .doOnSubscribe(() -> logger.info("Start submit task with label " + orderLabel + " for " + instrument))
                .doOnError(e -> logger.error("Submit task with label " + orderLabel
                        + " for " + instrument + " failed!Exception: " + e.getMessage()))
                .doOnCompleted(() -> logger.info("Submit task with label " + orderLabel
                        + " for " + instrument + " was successful."));
    }

    public Observable<OrderEvent> submitAndMergePosition(final String mergeOrderLabel,
                                                         final OrderParams orderParams) {
        checkNotNull(mergeOrderLabel);
        checkNotNull(orderParams);

        return orderUtilImpl.submitAndMergePosition(mergeOrderLabel, orderParams);
    }

    public Observable<OrderEvent> submitAndMergePositionToParams(final String mergeOrderLabel,
                                                                 final OrderParams orderParams) {
        checkNotNull(mergeOrderLabel);
        checkNotNull(orderParams);

        return orderUtilImpl.submitAndMergePositionToParams(mergeOrderLabel, orderParams);
    }

    public Observable<OrderEvent> mergeOrders(final String mergeOrderLabel,
                                              final Collection<IOrder> toMergeOrders) {
        checkNotNull(mergeOrderLabel);
        checkNotNull(toMergeOrders);

        return orderUtilImpl.mergeOrders(mergeOrderLabel, toMergeOrders)
                .doOnSubscribe(() -> logger.info("Starting to merge with label " + mergeOrderLabel
                        + " for position " + instrumentFromOrders(toMergeOrders) + "."))
                .doOnCompleted(() -> logger.info("Merging with label " + mergeOrderLabel
                        + " for position " + instrumentFromOrders(toMergeOrders) + " was successful."))
                .doOnError(e -> logger.error("Merging with label " + mergeOrderLabel + " for position "
                        + instrumentFromOrders(toMergeOrders) + " failed! Exception: " + e.getMessage()));
    }

    public Observable<OrderEvent> mergePositionOrders(final String mergeOrderLabel,
                                                      final Instrument instrument) {
        checkNotNull(mergeOrderLabel);
        checkNotNull(instrument);

        return orderUtilImpl.mergePositionOrders(mergeOrderLabel, instrument)
                .doOnSubscribe(() -> logger.info("Starting position merge for " +
                        instrument + " with label " + mergeOrderLabel))
                .doOnError(e -> logger.error("Position merge for " + instrument
                        + "  with label " + mergeOrderLabel + " failed!" + "Exception: " + e.getMessage()))
                .doOnCompleted(() -> logger.info("Position merge for " + instrument
                        + "  with label " + mergeOrderLabel + " was successful."));
    }

    public Observable<OrderEvent> closePosition(final Instrument instrument) {
        return orderUtilImpl.closePosition(checkNotNull(instrument))
                .doOnSubscribe(() -> logger.info("Starting position close for " + instrument))
                .doOnCompleted(() -> logger.info("Closing position " + instrument + " was successful."))
                .doOnError(e -> logger.error("Closing position " + instrument
                        + " failed! Exception: " + e.getMessage()));
    }

    public Observable<OrderEvent> close(final IOrder orderToClose) {
        final String commonLog = "state from " + orderToClose.getState() + " to " + IOrder.State.CLOSED;
        return changeObservable(orderUtilImpl.close(orderToClose),
                                orderToClose,
                                commonLog);
    }

    public Observable<OrderEvent> setLabel(final IOrder orderToChangeLabel,
                                           final String newLabel) {
        final String commonLog = "label from " + orderToChangeLabel.getLabel() + " to " + newLabel;
        return changeObservable(orderUtilImpl.setLabel(orderToChangeLabel, newLabel),
                                orderToChangeLabel,
                                commonLog);
    }

    public Observable<OrderEvent> setGoodTillTime(final IOrder orderToChangeGTT,
                                                  final long newGTT) {
        final String commonLog = "GTT from " + orderToChangeGTT.getGoodTillTime() + " to " + newGTT;
        return changeObservable(orderUtilImpl.setGoodTillTime(orderToChangeGTT, newGTT),
                                orderToChangeGTT,
                                commonLog);
    }

    public Observable<OrderEvent> setOpenPrice(final IOrder orderToChangeOpenPrice,
                                               final double newOpenPrice) {
        final String commonLog = "open price from " + orderToChangeOpenPrice.getOpenPrice() + " to " + newOpenPrice;
        return changeObservable(orderUtilImpl.setOpenPrice(orderToChangeOpenPrice, newOpenPrice),
                                orderToChangeOpenPrice,
                                commonLog);
    }

    public Observable<OrderEvent> setRequestedAmount(final IOrder orderToChangeAmount,
                                                     final double newRequestedAmount) {
        final String commonLog = "amount from " + orderToChangeAmount.getRequestedAmount()
                + " to " + newRequestedAmount;
        return changeObservable(orderUtilImpl.setRequestedAmount(orderToChangeAmount, newRequestedAmount),
                                orderToChangeAmount,
                                commonLog);
    }

    public Observable<OrderEvent> setStopLossPrice(final IOrder orderToChangeSL,
                                                   final double newSL) {
        final String commonLog = "SL from " + orderToChangeSL.getStopLossPrice() + " to " + newSL;
        return changeObservable(orderUtilImpl.setStopLossPrice(orderToChangeSL, newSL),
                                orderToChangeSL,
                                commonLog);
    }

    public Observable<OrderEvent> setTakeProfitPrice(final IOrder orderToChangeTP,
                                                     final double newTP) {
        final String commonLog = "TP from " + orderToChangeTP.getTakeProfitPrice() + " to " + newTP;
        return changeObservable(orderUtilImpl.setTakeProfitPrice(orderToChangeTP, newTP),
                                orderToChangeTP,
                                commonLog);
    }

    private Observable<OrderEvent> changeObservable(final Observable<OrderEvent> observable,
                                                    final IOrder order,
                                                    final String commonLog) {
        final String logMsg = commonLog + " for order " + order.getLabel()
                + " and instrument " + order.getInstrument();
        return observable
                .doOnSubscribe(() -> logger.info("Start to change " + logMsg))
                .doOnError(e -> logger.error("Failed to change " + logMsg + "!Excpetion: " + e.getMessage()))
                .doOnCompleted(() -> logger.info("Changed " + logMsg));
    }

    public final SubmitOption submitBuilder(final OrderParams orderParams) {
        return SubmitBuilder.forOrderParams(orderParams);
    }

    public final void startSubmit(final SubmitBuilder submitBuilder) {
        submitOrder(submitBuilder.orderParams())
                .subscribe(orderEvent -> {
                    final IOrder order = orderEvent.order();
                    if (orderEvent.type() == OrderEventType.FULLY_FILLED)
                        submitBuilder.fillAction().accept(order);
                    else if (orderEvent.type() == OrderEventType.PARTIAL_FILL_OK)
                        submitBuilder.partialFillAction().accept(order);
                    else if (orderEvent.type() == OrderEventType.SUBMIT_CONDITIONAL_OK
                            || orderEvent.type() == OrderEventType.SUBMIT_OK)
                        submitBuilder.submitOKAction().accept(order);
                    else if (orderEvent.type() == OrderEventType.SUBMIT_REJECTED)
                        submitBuilder.submitRejectAction().accept(order);
                    else if (orderEvent.type() == OrderEventType.FILL_REJECTED)
                        submitBuilder.fillRejectAction().accept(order);
                }, submitBuilder.errorAction()::accept);
    }

    public final MergeOption mergeBuilder(final String mergeOrderLabel,
                                          final Collection<IOrder> toMergeOrders) {
        return MergeBuilder.forParams(mergeOrderLabel, toMergeOrders);
    }

    public final void startMerge(final MergeBuilder mergeBuilder) {
        mergeOrders(mergeBuilder.mergeOrderLabel(), mergeBuilder.toMergeOrders())
                .subscribe(orderEvent -> {
                    final IOrder order = orderEvent.order();
                    if (orderEvent.type() == OrderEventType.MERGE_OK)
                        mergeBuilder.mergeOKAction().accept(order);
                    else if (orderEvent.type() == OrderEventType.MERGE_CLOSE_OK)
                        mergeBuilder.mergeCloseOKAction().accept(order);
                    else if (orderEvent.type() == OrderEventType.MERGE_REJECTED)
                        mergeBuilder.mergeRejectAction().accept(order);
                }, mergeBuilder.errorAction()::accept);
    }
}
