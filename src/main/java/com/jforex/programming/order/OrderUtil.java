package com.jforex.programming.order;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.jforex.programming.order.OrderStaticUtil.instrumentFromOrders;

import java.util.Collection;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dukascopy.api.IOrder;
import com.dukascopy.api.Instrument;
import com.jforex.programming.order.builder.CloseProcess;
import com.jforex.programming.order.builder.ClosePositionProcess;
import com.jforex.programming.order.builder.MergeProcess;
import com.jforex.programming.order.builder.SetAmountProcess;
import com.jforex.programming.order.builder.SetGTTProcess;
import com.jforex.programming.order.builder.SetLabelProcess;
import com.jforex.programming.order.builder.SetPriceProcess;
import com.jforex.programming.order.builder.SetSLProcess;
import com.jforex.programming.order.builder.SetTPProcess;
import com.jforex.programming.order.builder.SubmitProcess;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.position.PositionOrders;

import rx.Observable;

public class OrderUtil {

    private final OrderUtilImpl orderUtilImpl;

    private static final Logger logger = LogManager.getLogger(OrderUtil.class);

    public OrderUtil(final OrderUtilImpl orderUtilImpl) {
        this.orderUtilImpl = orderUtilImpl;
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

    public final void startSubmit(final SubmitProcess submitBuilder) {
        final OrderParams orderParams = submitBuilder.orderParams();
        final Instrument instrument = orderParams.instrument();
        final String orderLabel = orderParams.label();
        final Observable<OrderEvent> observable = orderUtilImpl.submitOrder(orderParams)
                .doOnSubscribe(() -> logger.info("Start submit task with label " + orderLabel + " for " + instrument))
                .doOnError(e -> logger.error("Submit task with label " + orderLabel
                        + " for " + instrument + " failed!Exception: " + e.getMessage()))
                .doOnCompleted(() -> logger.info("Submit task with label " + orderLabel
                        + " for " + instrument + " was successful."));

        submitBuilder.startObservable(observable);
    }

    public final void startMerge(final MergeProcess mergeBuilder) {
        final String mergeOrderLabel = mergeBuilder.mergeOrderLabel();
        final Collection<IOrder> toMergeOrders = mergeBuilder.toMergeOrders();
        final Observable<OrderEvent> observable = orderUtilImpl.mergeOrders(mergeOrderLabel, toMergeOrders)
                .doOnSubscribe(() -> logger.info("Starting to merge with label " + mergeOrderLabel
                        + " for position " + instrumentFromOrders(toMergeOrders) + "."))
                .doOnCompleted(() -> logger.info("Merging with label " + mergeOrderLabel
                        + " for position " + instrumentFromOrders(toMergeOrders) + " was successful."))
                .doOnError(e -> logger.error("Merging with label " + mergeOrderLabel + " for position "
                        + instrumentFromOrders(toMergeOrders) + " failed! Exception: " + e.getMessage()));

        mergeBuilder.startObservable(observable);
    }

    public final void startClose(final CloseProcess closeBuilder) {
        final IOrder orderToClose = closeBuilder.orderToClose();
        final String commonLog = "state from " + orderToClose.getState() + " to " + IOrder.State.CLOSED;
        final Observable<OrderEvent> observable =
                changeObservable(orderUtilImpl.close(orderToClose),
                                 orderToClose,
                                 commonLog);

        closeBuilder.startObservable(observable);
    }

    public final void startPositionClose(final ClosePositionProcess closePositionBuilder) {
        final Instrument instrument = closePositionBuilder.instrument();
        final Observable<OrderEvent> observable = orderUtilImpl.closePosition(instrument)
                .doOnSubscribe(() -> logger.info("Starting position close for " + instrument))
                .doOnCompleted(() -> logger.info("Closing position " + instrument + " was successful."))
                .doOnError(e -> logger.error("Closing position " + instrument
                        + " failed! Exception: " + e.getMessage()));

        closePositionBuilder.startObservable(observable);
    }

    public final void startLabelChange(final SetLabelProcess setLabelBuilder) {
        final IOrder orderToChangeLabel = setLabelBuilder.order();
        final String newLabel = setLabelBuilder.newLabel();
        final String commonLog = "label from " + orderToChangeLabel.getLabel() + " to " + newLabel;
        final Observable<OrderEvent> observable =
                changeObservable(orderUtilImpl.setLabel(orderToChangeLabel, newLabel),
                                 orderToChangeLabel,
                                 commonLog);

        setLabelBuilder.startObservable(observable);
    }

    public final void startGTTChange(final SetGTTProcess setGTTBuilder) {
        final IOrder orderToChangeGTT = setGTTBuilder.order();
        final long newGTT = setGTTBuilder.newGTT();
        final String commonLog = "GTT from " + orderToChangeGTT.getGoodTillTime() + " to " + newGTT;
        final Observable<OrderEvent> observable =
                changeObservable(orderUtilImpl.setGoodTillTime(orderToChangeGTT, newGTT),
                                 orderToChangeGTT,
                                 commonLog);

        setGTTBuilder.startObservable(observable);
    }

    public final void startAmountChange(final SetAmountProcess setAmountBuilder) {
        final IOrder orderToChangeAmount = setAmountBuilder.order();
        final double newRequestedAmount = setAmountBuilder.newAmount();
        final String commonLog = "amount from " + orderToChangeAmount.getRequestedAmount()
                + " to " + newRequestedAmount;
        final Observable<OrderEvent> observable =
                changeObservable(orderUtilImpl.setRequestedAmount(orderToChangeAmount, newRequestedAmount),
                                 orderToChangeAmount,
                                 commonLog);

        setAmountBuilder.startObservable(observable);
    }

    public final void startOpenPriceChange(final SetPriceProcess setPriceBuilder) {
        final IOrder orderToChangeOpenPrice = setPriceBuilder.order();
        final double newOpenPrice = setPriceBuilder.newOpenPrice();
        final String commonLog = "open price from " + orderToChangeOpenPrice.getOpenPrice() + " to " + newOpenPrice;
        final Observable<OrderEvent> observable =
                changeObservable(orderUtilImpl.setOpenPrice(orderToChangeOpenPrice, newOpenPrice),
                                 orderToChangeOpenPrice,
                                 commonLog);

        setPriceBuilder.startObservable(observable);
    }

    public final void startSLChange(final SetSLProcess setSLBuilder) {
        final IOrder orderToChangeSL = setSLBuilder.order();
        final double newSL = setSLBuilder.newSL();
        final String commonLog = "SL from " + orderToChangeSL.getStopLossPrice() + " to " + newSL;
        final Observable<OrderEvent> observable =
                changeObservable(orderUtilImpl.setStopLossPrice(orderToChangeSL, newSL),
                                 orderToChangeSL,
                                 commonLog);

        setSLBuilder.startObservable(observable);
    }

    public final void startTPChange(final SetTPProcess setTPBuilder) {
        final IOrder orderToChangeTP = setTPBuilder.order();
        final double newTP = setTPBuilder.newTP();
        final String commonLog = "TP from " + orderToChangeTP.getTakeProfitPrice() + " to " + newTP;
        final Observable<OrderEvent> observable =
                changeObservable(orderUtilImpl.setTakeProfitPrice(orderToChangeTP, newTP),
                                 orderToChangeTP,
                                 commonLog);

        setTPBuilder.startObservable(observable);
    }

    public PositionOrders positionOrders(final Instrument instrument) {
        return orderUtilImpl.positionOrders(checkNotNull(instrument));
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
}
