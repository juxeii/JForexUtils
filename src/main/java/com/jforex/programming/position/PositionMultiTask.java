package com.jforex.programming.position;

import java.util.Set;

import org.aeonbits.owner.ConfigFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.settings.PlatformSettings;

import com.dukascopy.api.IOrder;
import com.dukascopy.api.Instrument;

import rx.Observable;

public class PositionMultiTask {

    private final PositionSingleTask positionSingleTask;

    private final static PlatformSettings platformSettings = ConfigFactory.create(PlatformSettings.class);
    private static final Logger logger = LogManager.getLogger(PositionMultiTask.class);

    public PositionMultiTask(final PositionSingleTask positionSetSLTPTask) {
        this.positionSingleTask = positionSetSLTPTask;
    }

    public Observable<OrderEvent> restoreSLTPObservable(final IOrder mergeOrder,
                                                        final RestoreSLTPData restoreSLTPData) {
        return restoreSLObservable(mergeOrder, restoreSLTPData)
                .concatWith(restoreTPObservable(mergeOrder, restoreSLTPData));
    }

    public Observable<OrderEvent> removeTPSLObservable(final Set<IOrder> filledOrders) {
        final Instrument instrument = filledOrders.iterator().next().getInstrument();
        logger.debug("Starting remove TPSL task for position " + instrument);
        return Observable.from(filledOrders)
                .flatMap(order -> removeSingleTPSLObservable(order))
                .doOnCompleted(() -> logger.debug("Removing TPSL from " + instrument + " was successful."))
                .doOnError(e -> logger.error("Removing TPSL from " + instrument
                        + " failed! Exception: " + e.getMessage()));
    }

    private Observable<OrderEvent> removeSingleTPSLObservable(final IOrder orderToRemoveSLTP) {
        return removeTPObservable(orderToRemoveSLTP)
                .concatWith(removeSLObservable(orderToRemoveSLTP));
    }

    private Observable<OrderEvent> removeTPObservable(final IOrder orderToRemoveTP) {
        final Instrument instrument = orderToRemoveTP.getInstrument();
        final String orderLabel = orderToRemoveTP.getLabel();
        return Observable.just(orderToRemoveTP)
                .doOnSubscribe(() -> logger.debug("Remove TP from " + orderLabel
                        + " for " + instrument))
                .flatMap(order -> positionSingleTask.setTPObservable(order, platformSettings.noTPPrice()))
                .doOnError(e -> logger.debug("Failed to remove TP from " + orderLabel
                        + " for " + instrument + ". Exception: " + e.getMessage()))
                .doOnCompleted(() -> logger.debug("Removed TP from " + orderLabel
                        + " for " + instrument + " successfully."));
    }

    private Observable<OrderEvent> removeSLObservable(final IOrder orderToRemoveSL) {
        final Instrument instrument = orderToRemoveSL.getInstrument();
        final String orderLabel = orderToRemoveSL.getLabel();
        return Observable.just(orderToRemoveSL)
                .doOnSubscribe(() -> logger.debug("Remove SL from " + orderLabel
                        + " for " + instrument))
                .flatMap(order -> positionSingleTask.setSLObservable(order, platformSettings.noSLPrice()))
                .doOnError(e -> logger.debug("Failed to remove SL from " + orderLabel
                        + " for " + instrument + ". Exception: " + e.getMessage()))
                .doOnCompleted(() -> logger.debug("Removed SL from " + orderLabel
                        + " for " + instrument + " successfully."));
    }

    private Observable<OrderEvent> restoreSLObservable(final IOrder mergeOrder,
                                                       final RestoreSLTPData restoreSLTPData) {
        final Instrument instrument = mergeOrder.getInstrument();
        final String orderLabel = mergeOrder.getLabel();
        return Observable.just(mergeOrder)
                .doOnSubscribe(() -> logger.debug("Restore SL for order " + orderLabel
                        + " for " + instrument))
                .flatMap(order -> positionSingleTask.setSLObservable(order, restoreSLTPData.sl()))
                .doOnError(e -> logger.debug("Failed to restore SL for order " + orderLabel
                        + " for " + instrument + ". Exception: " + e.getMessage()))
                .doOnCompleted(() -> logger.debug("Restore SL for order " + orderLabel
                        + " for " + instrument + " successfully."));
    }

    private Observable<OrderEvent> restoreTPObservable(final IOrder mergeOrder,
                                                       final RestoreSLTPData restoreSLTPData) {
        final Instrument instrument = mergeOrder.getInstrument();
        final String orderLabel = mergeOrder.getLabel();
        return Observable.just(mergeOrder)
                .doOnSubscribe(() -> logger.debug("Restore TP for order " + orderLabel
                        + " for " + instrument))
                .flatMap(order -> positionSingleTask.setTPObservable(order, restoreSLTPData.tp()))
                .doOnError(e -> logger.debug("Failed to restore TP for order " + orderLabel
                        + " for " + instrument + ". Exception: " + e.getMessage()))
                .doOnCompleted(() -> logger.debug("Restore TP for order " + orderLabel
                        + " for " + instrument + " successfully."));
    }
}
