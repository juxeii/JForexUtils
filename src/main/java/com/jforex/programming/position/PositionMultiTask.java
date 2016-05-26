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
        final Instrument instrument = mergeOrder.getInstrument();
        logger.debug("Starting restore SLTP task for position " + instrument);
        return restoreSingleSLTPObservable(mergeOrder, restoreSLTPData)
                .doOnCompleted(() -> logger.debug("Restoring SLTP for position " + instrument + " was successful."))
                .doOnError(e -> logger.error("Restoring SLTP for position " + instrument
                        + " failed! Exception: " + e.getMessage()));
    }

    private Observable<OrderEvent> restoreSingleSLTPObservable(final IOrder mergeOrder,
                                                               final RestoreSLTPData restoreSLTPData) {
        final Observable<OrderEvent> restoreSLObs =
                positionSingleTask.setSLObservable(mergeOrder, restoreSLTPData.sl());
        final Observable<OrderEvent> restoreTPObs =
                Observable.defer(() -> positionSingleTask.setTPObservable(mergeOrder,
                                                                          restoreSLTPData.tp()));
        return restoreSLObs.concatWith(restoreTPObs);
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
        final Observable<OrderEvent> removeTPObs =
                positionSingleTask.setTPObservable(orderToRemoveSLTP, platformSettings.noTPPrice());
        final Observable<OrderEvent> removeSLObs =
                Observable.defer(() -> positionSingleTask.setSLObservable(orderToRemoveSLTP,
                                                                          platformSettings.noSLPrice()));
        return removeTPObs.concatWith(removeSLObs);
    }
}
