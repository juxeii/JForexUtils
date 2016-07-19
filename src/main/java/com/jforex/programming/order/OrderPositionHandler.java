package com.jforex.programming.order;

import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dukascopy.api.IOrder;
import com.dukascopy.api.Instrument;
import com.jforex.programming.order.command.MergeCommand;
import com.jforex.programming.order.command.MergePositionCommand;
import com.jforex.programming.order.command.OrderCallCommand;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.event.OrderEventTypeData;
import com.jforex.programming.position.Position;
import com.jforex.programming.position.PositionFactory;
import com.jforex.programming.position.PositionMultiTask;
import com.jforex.programming.position.PositionOrders;
import com.jforex.programming.position.PositionSingleTask;

import rx.Completable;
import rx.Observable;

public class OrderPositionHandler {

    private final OrderUtilHandler orderUtilHandler;
    private final PositionSingleTask positionSingleTask;
    private final PositionMultiTask positionMultiTask;
    private final PositionFactory positionFactory;

    private static final Logger logger = LogManager.getLogger(OrderPositionHandler.class);

    public OrderPositionHandler(final OrderUtilHandler orderUtilHandler,
                                final PositionSingleTask positionSingleTask,
                                final PositionMultiTask positionMultiTask,
                                final PositionFactory positionFactory) {
        this.orderUtilHandler = orderUtilHandler;
        this.positionSingleTask = positionSingleTask;
        this.positionMultiTask = positionMultiTask;
        this.positionFactory = positionFactory;
    }

    public PositionOrders positionOrders(final Instrument instrument) {
        return positionFactory.forInstrument(instrument);
    }

    private Position position(final Instrument instrument) {
        return (Position) positionOrders(instrument);
    }

    public Observable<OrderEvent> submitOrder(final OrderCallCommand command) {
        return orderUtilHandler
                .callObservable(command)
                .doOnNext(submitEvent -> addOrderToPositionIfDone(submitEvent,
                                                                  OrderEventTypeData.submitData));
    }

    public Observable<OrderEvent> mergeOrders(final MergeCommand command) {
        return orderUtilHandler
                .callObservable(command)
                .doOnNext(mergeEvent -> addOrderToPositionIfDone(mergeEvent,
                                                                 OrderEventTypeData.mergeData));
    }

    private void addOrderToPositionIfDone(final OrderEvent orderEvent,
                                          final OrderEventTypeData orderEventTypeData) {
        if (orderEventTypeData.isDoneType(orderEvent.type())) {
            final IOrder order = orderEvent.order();
            position(order.getInstrument()).addOrder(order);
        }
    }

    public Observable<OrderEvent> mergePositionOrders(final MergePositionCommand command) {
        final Set<IOrder> filledOrders = command.toMergeOrders();
        final Instrument instrument = command.instrument();
        final String mergeOrderLabel = command.mergeOrderLabel();

        return Observable
                .just(filledOrders)
                .doOnSubscribe(() -> {
                    logger.debug("Starting position merge for " + instrument + " with label "
                            + mergeOrderLabel);
                    position(instrument).markAllOrdersActive();
                })
                .flatMap(positionMultiTask::removeTPSLObservable)
                .concatWith(orderUtilHandler
                        .callWithRetriesObservable(command)
                        .doOnNext(oe -> position(oe.order().getInstrument()).addOrder(oe.order()))
                        .map(OrderEvent::order)
                        .flatMap(order -> positionMultiTask
                                .restoreSLTPObservable(order, command.restoreSLTPData())))
                .doOnCompleted(() -> logger
                        .debug("Position merge for " + instrument + "  with label "
                                + mergeOrderLabel + " was successful."));
    }

    public Completable closePosition(final Instrument instrument) {
        final Position position = position(instrument);

        return Observable.from(position.filledOrOpened())
                .doOnSubscribe(() -> {
                    logger.debug("Starting position close for " + instrument);
                    position.markAllOrdersActive();
                })
                .flatMap(positionSingleTask::closeObservable)
                .doOnCompleted(() -> logger
                        .debug("Closing position " + instrument + " was successful."))
                .doOnError(e -> logger.error("Closing position " + instrument
                        + " failed! Exception: " + e.getMessage()))
                .toCompletable();
    }
}
