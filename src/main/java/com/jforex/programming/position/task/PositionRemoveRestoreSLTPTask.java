package com.jforex.programming.position.task;

import org.aeonbits.owner.ConfigFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dukascopy.api.IOrder;
import com.jforex.programming.position.RestoreSLTPData;
import com.jforex.programming.settings.PlatformSettings;

import rx.Completable;
import rx.Observable;

public class PositionRemoveRestoreSLTPTask {

    private final PositionSetSLTPTask positionSetSLTPTask;

    public final static PlatformSettings platformSettings = ConfigFactory.create(PlatformSettings.class);
    private static final Logger logger = LogManager.getLogger(PositionRemoveRestoreSLTPTask.class);

    public PositionRemoveRestoreSLTPTask(final PositionSetSLTPTask positionSetSLTPTask) {
        this.positionSetSLTPTask = positionSetSLTPTask;
    }

    public Completable removeTPSLObs(final IOrder orderToRemoveSLTP) {
        final Completable removeTPObs = Observable.just(orderToRemoveSLTP)
                .doOnNext(order -> logger.debug("Remove TP from " + order.getLabel()))
                .flatMap(order -> positionSetSLTPTask.setTPCompletable(order, platformSettings.noTPPrice())
                        .toObservable())
                .toCompletable();
        final Completable removeSLObs = Observable.just(orderToRemoveSLTP)
                .doOnNext(order -> logger.debug("Remove SL from " + order.getLabel()))
                .flatMap(order -> positionSetSLTPTask.setSLCompletable(order, platformSettings.noSLPrice())
                        .toObservable())
                .toCompletable();
        return removeTPObs.concatWith(removeSLObs);
    }

    public Completable restoreSLTPObs(final IOrder mergeOrder,
                                      final RestoreSLTPData restoreSLTPData) {
        final Completable restoreSLObs = Observable.just(mergeOrder)
                .doOnNext(order -> logger.debug("Restore SL from " + order.getLabel()))
                .flatMap(order -> positionSetSLTPTask.setSLCompletable(order, restoreSLTPData.sl()).toObservable())
                .toCompletable();
        final Completable restoreTPObs = Observable.just(mergeOrder)
                .doOnNext(order -> logger.debug("Restore TP from " + order.getLabel()))
                .flatMap(order -> positionSetSLTPTask.setTPCompletable(order, restoreSLTPData.tp()).toObservable())
                .toCompletable();
        return restoreSLObs.concatWith(restoreTPObs);
    }
}
