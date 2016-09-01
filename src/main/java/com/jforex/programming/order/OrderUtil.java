package com.jforex.programming.order;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.jforex.programming.order.OrderStaticUtil.instrumentFromOrders;

import java.util.Collection;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Consumer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dukascopy.api.IOrder;
import com.dukascopy.api.Instrument;
import com.jforex.programming.order.call.OrderCallRejectException;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.event.OrderEventType;
import com.jforex.programming.order.event.OrderEventTypeSets;
import com.jforex.programming.order.process.ClosePositionProcess;
import com.jforex.programming.order.process.CloseProcess;
import com.jforex.programming.order.process.CommonProcess;
import com.jforex.programming.order.process.MergePositionProcess;
import com.jforex.programming.order.process.MergeProcess;
import com.jforex.programming.order.process.ProcessRetry;
import com.jforex.programming.order.process.SetAmountProcess;
import com.jforex.programming.order.process.SetGTTProcess;
import com.jforex.programming.order.process.SetLabelProcess;
import com.jforex.programming.order.process.SetOpenPriceProcess;
import com.jforex.programming.order.process.SetSLProcess;
import com.jforex.programming.order.process.SetTPProcess;
import com.jforex.programming.order.process.SubmitAndMergePositionProcess;
import com.jforex.programming.order.process.SubmitProcess;
import com.jforex.programming.position.PositionOrders;

import rx.Observable;

public class OrderUtil {

    private final OrderUtilObservable orderUtilImpl;

    private static final Logger logger = LogManager.getLogger(OrderUtil.class);

    public OrderUtil(final OrderUtilObservable orderUtilImpl) {
        this.orderUtilImpl = orderUtilImpl;
    }

    public PositionOrders positionOrders(final Instrument instrument) {
        return orderUtilImpl.positionOrders(checkNotNull(instrument));
    }

    public final void startSubmit(final SubmitProcess process) {
        final OrderParams orderParams = process.orderParams();
        final Instrument instrument = orderParams.instrument();
        final String orderLabel = orderParams.label();
        final Observable<OrderEvent> observable = orderUtilImpl.submitOrder(orderParams)
            .doOnSubscribe(() -> logger.info("Start submit task with label " + orderLabel + " for " + instrument))
            .doOnError(e -> logger.error("Submit task with label " + orderLabel
                    + " for " + instrument + " failed!Exception: " + e.getMessage()))
            .doOnCompleted(() -> logger.info("Submit task with label " + orderLabel
                    + " for " + instrument + " was successful."));

        startProcess(process, observable);
    }

    public final void startSubmitAndMergePosition(final SubmitAndMergePositionProcess process) {
        startSubmitAndMergeGeneric(process, orderUtilImpl::submitAndMergePosition);
    }

    public final void startSubmitAndMergePositionToParams(final SubmitAndMergePositionProcess process) {
        startSubmitAndMergeGeneric(process, orderUtilImpl::submitAndMergePositionToParams);
    }

    private final void startSubmitAndMergeGeneric(final SubmitAndMergePositionProcess process,
                                                  final BiFunction<String, OrderParams, Observable<OrderEvent>> call) {
        final OrderParams orderParams = process.orderParams();
        final String mergeOrderLabel = process.mergeOrderLabel();
        final Observable<OrderEvent> observable = call.apply(mergeOrderLabel, orderParams);

        startProcess(process, observable);
    }

    public final void startMerge(final MergeProcess process) {
        final String mergeOrderLabel = process.mergeOrderLabel();
        final Collection<IOrder> toMergeOrders = process.toMergeOrders();
        final Observable<OrderEvent> observable = orderUtilImpl.mergeOrders(mergeOrderLabel, toMergeOrders)
            .doOnSubscribe(() -> logger.info("Starting to merge with label " + mergeOrderLabel
                    + " for position " + instrumentFromOrders(toMergeOrders) + "."))
            .doOnCompleted(() -> logger.info("Merging with label " + mergeOrderLabel
                    + " for position " + instrumentFromOrders(toMergeOrders) + " was successful."))
            .doOnError(e -> logger.error("Merging with label " + mergeOrderLabel + " for position "
                    + instrumentFromOrders(toMergeOrders) + " failed! Exception: " + e.getMessage()));

        startProcess(process, observable);
    }

    public final void startPositionMerge(final MergePositionProcess process) {
        final String mergeOrderLabel = process.mergeOrderLabel();
        final Instrument instrument = process.instrument();
        final Observable<OrderEvent> observable = orderUtilImpl.mergePositionOrders(mergeOrderLabel, instrument)
            .doOnSubscribe(() -> logger.info("Starting position merge for " +
                    instrument + " with label " + mergeOrderLabel))
            .doOnError(e -> logger.error("Position merge for " + instrument
                    + "  with label " + mergeOrderLabel + " failed!" + "Exception: " + e.getMessage()))
            .doOnCompleted(() -> logger.info("Position merge for " + instrument
                    + "  with label " + mergeOrderLabel + " was successful."));

        startProcess(process, observable);
    }

    public final void startClose(final CloseProcess process) {
        final IOrder orderToClose = process.orderToClose();
        final String commonLog = "state from " + orderToClose.getState() + " to " + IOrder.State.CLOSED;
        final Observable<OrderEvent> observable =
                changeObservable(orderUtilImpl.close(orderToClose),
                                 orderToClose,
                                 commonLog);

        startProcess(process, observable);
    }

    public final void startPositionClose(final ClosePositionProcess process) {
        final Instrument instrument = process.instrument();
        final Observable<OrderEvent> observable = orderUtilImpl.closePosition(instrument)
            .doOnSubscribe(() -> logger.info("Starting position close for " + instrument))
            .doOnCompleted(() -> logger.info("Closing position " + instrument + " was successful."))
            .doOnError(e -> logger.error("Closing position " + instrument
                    + " failed! Exception: " + e.getMessage()));

        startProcess(process, observable);
    }

    public final void startLabelChange(final SetLabelProcess process) {
        final IOrder orderToChangeLabel = process.order();
        final String newLabel = process.newLabel();
        final String commonLog = "label from " + orderToChangeLabel.getLabel() + " to " + newLabel;
        final Observable<OrderEvent> observable =
                changeObservable(orderUtilImpl.setLabel(orderToChangeLabel, newLabel),
                                 orderToChangeLabel,
                                 commonLog);

        startProcess(process, observable);
    }

    public final void startGTTChange(final SetGTTProcess process) {
        final IOrder orderToChangeGTT = process.order();
        final long newGTT = process.newGTT();
        final String commonLog = "GTT from " + orderToChangeGTT.getGoodTillTime() + " to " + newGTT;
        final Observable<OrderEvent> observable =
                changeObservable(orderUtilImpl.setGoodTillTime(orderToChangeGTT, newGTT),
                                 orderToChangeGTT,
                                 commonLog);

        startProcess(process, observable);
    }

    public final void startAmountChange(final SetAmountProcess process) {
        final IOrder orderToChangeAmount = process.order();
        final double newRequestedAmount = process.newAmount();
        final String commonLog = "amount from " + orderToChangeAmount.getRequestedAmount()
                + " to " + newRequestedAmount;
        final Observable<OrderEvent> observable =
                changeObservable(orderUtilImpl.setRequestedAmount(orderToChangeAmount, newRequestedAmount),
                                 orderToChangeAmount,
                                 commonLog);

        startProcess(process, observable);
    }

    public final void startOpenPriceChange(final SetOpenPriceProcess process) {
        final IOrder orderToChangeOpenPrice = process.order();
        final double newOpenPrice = process.newOpenPrice();
        final String commonLog = "open price from " + orderToChangeOpenPrice.getOpenPrice() + " to " + newOpenPrice;
        final Observable<OrderEvent> observable =
                changeObservable(orderUtilImpl.setOpenPrice(orderToChangeOpenPrice, newOpenPrice),
                                 orderToChangeOpenPrice,
                                 commonLog);

        startProcess(process, observable);
    }

    public final void startSLChange(final SetSLProcess process) {
        final IOrder orderToChangeSL = process.order();
        final double newSL = process.newSL();
        final String commonLog = "SL from " + orderToChangeSL.getStopLossPrice() + " to " + newSL;
        final Observable<OrderEvent> observable =
                changeObservable(orderUtilImpl.setStopLossPrice(orderToChangeSL, newSL),
                                 orderToChangeSL,
                                 commonLog);

        startProcess(process, observable);
    }

    public final void startTPChange(final SetTPProcess process) {
        final IOrder orderToChangeTP = process.order();
        final double newTP = process.newTP();
        final String commonLog = "TP from " + orderToChangeTP.getTakeProfitPrice() + " to " + newTP;
        final Observable<OrderEvent> observable =
                changeObservable(orderUtilImpl.setTakeProfitPrice(orderToChangeTP, newTP),
                                 orderToChangeTP,
                                 commonLog);

        startProcess(process, observable);
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

    private final void startProcess(final CommonProcess process,
                                    final Observable<OrderEvent> observable) {
        final Observable<OrderEvent> addActionHandlersObservable =
                observable.doOnNext(orderEvent -> callEventHandler(orderEvent,
                                                                   process.eventHandlerForType()));
        evaluateRetry(process, addActionHandlersObservable)
            .subscribe(process.eventAction()::accept,
                       process.errorAction()::accept);
    }

    private final void callEventHandler(final OrderEvent orderEvent,
                                        final Map<OrderEventType, Consumer<IOrder>> eventHandlerForType) {
        final OrderEventType type = orderEvent.type();
        if (eventHandlerForType.containsKey(type))
            eventHandlerForType
                .get(type)
                .accept(orderEvent.order());
    }

    private final Observable<OrderEvent> evaluateRetry(final CommonProcess process,
                                                       final Observable<OrderEvent> observable) {
        if (process.noOfRetries() > 0) {
            final ProcessRetry orderProcessRetry = new ProcessRetry(process.noOfRetries(), process.delayInMillis());
            return observable
                .flatMap(orderEvent -> rejectAsErrorObservable(orderEvent))
                .retryWhen(orderProcessRetry::retryOnRejectObservable);
        }
        return observable;
    }

    private Observable<OrderEvent> rejectAsErrorObservable(final OrderEvent orderEvent) {
        logger.info("EVEEEEEEEEEEEEEEEEEEE " + orderEvent.type() + " contains: "
                + OrderEventTypeSets.rejectEvents.contains(orderEvent.type()));
        return OrderEventTypeSets.rejectEvents.contains(orderEvent.type())
                ? Observable.error(new OrderCallRejectException("Reject event",
                                                                orderEvent))
                : Observable.just(orderEvent);
    }
}
