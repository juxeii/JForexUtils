package com.jforex.programming.order;

import java.util.Collection;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dukascopy.api.IOrder;
import com.dukascopy.api.Instrument;
import com.jforex.programming.builder.OrderParams;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.event.OrderEventTypeData;
import com.jforex.programming.position.Position;
import com.jforex.programming.position.PositionFactory;
import com.jforex.programming.position.PositionMultiTask;
import com.jforex.programming.position.PositionOrders;
import com.jforex.programming.position.PositionSingleTask;
import com.jforex.programming.position.RestoreSLTPData;
import com.jforex.programming.position.RestoreSLTPPolicy;

import rx.Completable;
import rx.Observable;

public class OrderPositionUtil {

    private final OrderCreateUtil orderCreateUtil;
    private final PositionSingleTask positionSingleTask;
    private final PositionMultiTask positionMultiTask;
    private final PositionFactory positionFactory;

    private static final Logger logger = LogManager.getLogger(OrderPositionUtil.class);

    public OrderPositionUtil(final OrderCreateUtil orderCreateUtil,
                             final PositionSingleTask positionSingleTask,
                             final PositionMultiTask positionMultiTask,
                             final PositionFactory positionFactory) {
        this.orderCreateUtil = orderCreateUtil;
        this.positionSingleTask = positionSingleTask;
        this.positionMultiTask = positionMultiTask;
        this.positionFactory = positionFactory;
    }

    public PositionOrders positionOrders(final Instrument instrument) {
        return positionFactory.forInstrument(instrument);
    }

    public Observable<OrderEvent> submitOrder(final OrderParams orderParams) {
        final Instrument instrument = orderParams.instrument();
        logger.debug("Start submit task with label " + orderParams.label() + " for " + instrument + " position.");

        final Position position = positionFactory.forInstrument(instrument);
        return orderCreateUtil
                .submitOrder(orderParams)
                .doOnNext(submitEvent -> {
                    if (OrderEventTypeData.submitData.isDoneType(submitEvent.type()))
                        position.addOrder(submitEvent.order());
                })
                .doOnError(e -> logger.error("Submit " + orderParams.label() + " for position "
                        + instrument + " failed! Exception: " + e.getMessage()));
    }

    public Observable<OrderEvent> mergeOrders(final String mergeOrderLabel,
                                              final Collection<IOrder> toMergeOrders) {
        final Instrument instrument = toMergeOrders.iterator().next().getInstrument();
        final Position position = positionFactory.forInstrument(instrument);
        return orderCreateUtil
                .mergeOrders(mergeOrderLabel, toMergeOrders)
                .doOnNext(mergeEvent -> position.addOrder(mergeEvent.order()))
                .doOnError(e -> logger.error("Merge with label " + mergeOrderLabel + " failed! Exception: "
                        + e.getMessage()));
    }

    public Observable<OrderEvent> mergePositionOrders(final String mergeOrderLabel,
                                                      final Instrument instrument,
                                                      final RestoreSLTPPolicy restoreSLTPPolicy) {
        final Position position = positionFactory.forInstrument(instrument);
        final Set<IOrder> filledOrders = position.filled();
        if (filledOrders.size() < 2)
            return Observable.empty();

        final RestoreSLTPData restoreSLTPData = new RestoreSLTPData(restoreSLTPPolicy.restoreSL(filledOrders),
                                                                    restoreSLTPPolicy.restoreTP(filledOrders));
        final Observable<OrderEvent> mergeAndRestoreObs =
                Observable.defer(() -> positionSingleTask.mergeObservable(mergeOrderLabel, filledOrders))
                        .map(OrderEvent::order)
                        .doOnNext(position::addOrder)
                        .flatMap(order -> positionMultiTask.restoreSLTPObservable(order, restoreSLTPData))
                        .doOnCompleted(() -> logger.debug("Merge task for " + instrument + " position with label "
                                + mergeOrderLabel + " was successful."));

        return Observable.just(filledOrders)
                .doOnSubscribe(position::markAllOrdersActive)
                .doOnNext(toMergeOrders -> logger.debug("Starting merge task for " + instrument
                        + " position with label " + mergeOrderLabel))
                .flatMap(positionMultiTask::removeTPSLObservable)
                .concatWith(mergeAndRestoreObs)
                .cast(OrderEvent.class);
    }

    public Completable closePosition(final Instrument instrument) {
        final Position position = positionFactory.forInstrument(instrument);

        return Observable.from(position.filledOrOpened())
                .doOnSubscribe(() -> logger.debug("Starting close task for position " + instrument))
                .doOnSubscribe(position::markAllOrdersActive)
                .flatMap(positionSingleTask::closeObservable)
                .doOnCompleted(() -> logger.debug("Closing position " + instrument + " was successful."))
                .doOnError(e -> logger.error("Closing position " + instrument
                        + " failed! Exception: " + e.getMessage()))
                .toCompletable();
    }
}
