package com.jforex.programming.position;

import java.util.Set;

import org.aeonbits.owner.ConfigFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dukascopy.api.IOrder;
import com.dukascopy.api.Instrument;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.settings.PlatformSettings;

import rx.Observable;

public class PositionMultiTask {

    private final PositionSingleTask positionSingleTask;

    private final static PlatformSettings platformSettings = ConfigFactory.create(PlatformSettings.class);
    private final static Logger logger = LogManager.getLogger(PositionMultiTask.class);

    public PositionMultiTask(final PositionSingleTask positionSetSLTPTask) {
        this.positionSingleTask = positionSetSLTPTask;
    }

    public Observable<OrderEvent> restoreSLTPObservable(final IOrder mergeOrder,
                                                        final RestoreSLTPData restoreSLTPData) {
        final Instrument instrument = mergeOrder.getInstrument();

        return restoreSingleSLTPObservable(mergeOrder, restoreSLTPData)
                .doOnSubscribe(() -> logger.debug("Starting restore SLTP task for position " + instrument))
                .doOnCompleted(() -> logger.debug("Restoring SLTP for position " + instrument + " was successful."))
                .doOnError(e -> logger.error("Restoring SLTP for position " + instrument
                        + " failed! Exception: " + e.getMessage()));
    }

    private final Observable<OrderEvent> restoreSingleSLTPObservable(final IOrder mergeOrder,
                                                                     final RestoreSLTPData restoreSLTPData) {
        final Observable<OrderEvent> restoreSLObs =
                positionSingleTask.setSLObservable(mergeOrder, restoreSLTPData.sl());
        final Observable<OrderEvent> restoreTPObs =
                positionSingleTask.setTPObservable(mergeOrder, restoreSLTPData.tp());

        return restoreSLObs.concatWith(restoreTPObs);
    }

    public Observable<OrderEvent> removeTPSLObservable(final Set<IOrder> filledOrders) {
        final Instrument instrument = filledOrders.iterator().next().getInstrument();

        return Observable
                .from(filledOrders)
                .doOnSubscribe(() -> logger.debug("Starting remove TPSL task for position " + instrument))
                .flatMap(this::removeSingleTPSLObservable)
                .doOnCompleted(() -> logger.debug("Removing TPSL from " + instrument + " was successful."))
                .doOnError(e -> logger.error("Removing TPSL from " + instrument
                        + " failed! Exception: " + e.getMessage()));
    }

    private final Observable<OrderEvent> removeSingleTPSLObservable(final IOrder orderToRemoveSLTP) {
        final Observable<OrderEvent> removeTPObs =
                positionSingleTask.setTPObservable(orderToRemoveSLTP, platformSettings.noTPPrice());
        final Observable<OrderEvent> removeSLObs =
                positionSingleTask.setSLObservable(orderToRemoveSLTP, platformSettings.noSLPrice());

        return removeTPObs.concatWith(removeSLObs);
    }
}
