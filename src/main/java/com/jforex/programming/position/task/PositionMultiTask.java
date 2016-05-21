package com.jforex.programming.position.task;

import org.aeonbits.owner.ConfigFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.position.RestoreSLTPData;
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

    public Observable<OrderEvent> removeTPSLObservable(final IOrder orderToRemoveSLTP) {
        return removeTPObservable(orderToRemoveSLTP)
                .concatWith(removeSLObservable(orderToRemoveSLTP));
    }

    private Observable<OrderEvent> removeTPObservable(final IOrder orderToRemoveTP) {
        final Instrument instrument = orderToRemoveTP.getInstrument();
        return Observable.just(orderToRemoveTP)
                .doOnSubscribe(() -> logger.debug("Remove TP from " + orderToRemoveTP.getLabel()
                        + " for " + instrument))
                .flatMap(order -> positionSingleTask.setTPObservable(order, platformSettings.noTPPrice()))
                .doOnError(e -> logger.debug("Failed to remove TP from " + orderToRemoveTP.getLabel()
                        + " for " + instrument + ". Exception: " + e.getMessage()))
                .doOnCompleted(() -> logger.debug("Removed TP from " + orderToRemoveTP.getLabel()
                        + " for " + instrument + " successfully."));
    }

    private Observable<OrderEvent> removeSLObservable(final IOrder orderToRemoveSL) {
        final Instrument instrument = orderToRemoveSL.getInstrument();
        return Observable.just(orderToRemoveSL)
                .doOnSubscribe(() -> logger.debug("Remove SL from " + orderToRemoveSL.getLabel()
                        + " for " + instrument))
                .flatMap(order -> positionSingleTask.setSLObservable(order, platformSettings.noSLPrice()))
                .doOnError(e -> logger.debug("Failed to remove SL from " + orderToRemoveSL.getLabel()
                        + " for " + instrument + ". Exception: " + e.getMessage()))
                .doOnCompleted(() -> logger.debug("Removed SL from " + orderToRemoveSL.getLabel()
                        + " for " + instrument + " successfully."));
    }

    public Observable<OrderEvent> restoreSLTPObservable(final IOrder mergeOrder,
                                                        final RestoreSLTPData restoreSLTPData) {
        return restoreSLObservable(mergeOrder, restoreSLTPData)
                .concatWith(restoreTPObservable(mergeOrder, restoreSLTPData));
    }

    private Observable<OrderEvent> restoreSLObservable(final IOrder mergeOrder,
                                                       final RestoreSLTPData restoreSLTPData) {
        final Instrument instrument = mergeOrder.getInstrument();
        return Observable.just(mergeOrder)
                .doOnSubscribe(() -> logger.debug("Restore SL for order " + mergeOrder.getLabel()
                        + " for " + instrument))
                .flatMap(order -> positionSingleTask.setSLObservable(order, restoreSLTPData.sl()))
                .doOnError(e -> logger.debug("Failed to restore SL for order " + mergeOrder.getLabel()
                        + " for " + instrument + ". Exception: " + e.getMessage()))
                .doOnCompleted(() -> logger.debug("Restore SL for order " + mergeOrder.getLabel()
                        + " for " + instrument + " successfully."));
    }

    private Observable<OrderEvent> restoreTPObservable(final IOrder mergeOrder,
                                                       final RestoreSLTPData restoreSLTPData) {
        final Instrument instrument = mergeOrder.getInstrument();
        return Observable.just(mergeOrder)
                .doOnSubscribe(() -> logger.debug("Restore TP for order " + mergeOrder.getLabel()
                        + " for " + instrument))
                .flatMap(order -> positionSingleTask.setTPObservable(order, restoreSLTPData.tp()))
                .doOnError(e -> logger.debug("Failed to restore TP for order " + mergeOrder.getLabel()
                        + " for " + instrument + ". Exception: " + e.getMessage()))
                .doOnCompleted(() -> logger.debug("Restore TP for order " + mergeOrder.getLabel()
                        + " for " + instrument + " successfully."));
    }
}
