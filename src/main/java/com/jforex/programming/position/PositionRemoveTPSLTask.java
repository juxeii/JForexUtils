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

public class PositionRemoveTPSLTask {

    private final PositionSingleTask positionSingleTask;

    private static final PlatformSettings platformSettings =
            ConfigFactory.create(PlatformSettings.class);
    private static final Logger logger = LogManager.getLogger(PositionRemoveTPSLTask.class);

    public PositionRemoveTPSLTask(final PositionSingleTask positionSetSLTPTask) {
        this.positionSingleTask = positionSetSLTPTask;
    }

    public Observable<OrderEvent> observable(final Set<IOrder> filledOrders) {
        final Instrument instrument = filledOrders.iterator().next().getInstrument();

        return Observable
                .from(filledOrders)
                .doOnSubscribe(() -> logger.debug("Starting remove TPSL task for position "
                        + instrument))
                .flatMap(this::removeSingleTPSLObservable)
                .doOnCompleted(() -> logger.debug("Removing TPSL task from "
                        + instrument + " was successful."))
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
