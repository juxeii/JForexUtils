package com.jforex.programming.position.task;

import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dukascopy.api.IOrder;
import com.dukascopy.api.Instrument;

import rx.Completable;
import rx.Observable;

public class PositionBatchTask {

    private final PositionSingleTask positionSingleTask;
    private final PositionMultiTask positionMultiTask;

    private static final Logger logger = LogManager.getLogger(PositionBatchTask.class);

    public PositionBatchTask(final PositionSingleTask positionSingleTask,
                             final PositionMultiTask positionMultiTask) {
        this.positionSingleTask = positionSingleTask;
        this.positionMultiTask = positionMultiTask;
    }

    public Completable closeCompletable(final Set<IOrder> ordersToClose) {
        final Instrument instrument = ordersToClose.iterator().next().getInstrument();
        logger.debug("Starting close task for position " + instrument);
        return Observable.from(ordersToClose)
                .flatMap(positionSingleTask::closeObservable)
                .doOnCompleted(() -> logger.debug("Closing position " + instrument + " was successful."))
                .doOnError(e -> logger.error("Closing position " + instrument
                        + " failed! Exception: " + e.getMessage()))
                .toCompletable();
    }

    public Completable removeTPSLObservable(final Set<IOrder> filledOrders) {
        final Instrument instrument = filledOrders.iterator().next().getInstrument();
        logger.debug("Starting remove TPSL task for position " + instrument);
        return Observable.from(filledOrders)
                .flatMap(order -> positionMultiTask.removeTPSLObservable(order))
                .doOnCompleted(() -> logger.debug("Removing TPSL from " + instrument + " was successful."))
                .doOnError(e -> logger.error("Removing TPSL from " + instrument
                        + " failed! Exception: " + e.getMessage()))
                .toCompletable();
    }
}
