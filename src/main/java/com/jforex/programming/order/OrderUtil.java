package com.jforex.programming.order;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Collection;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dukascopy.api.IEngine;
import com.dukascopy.api.IOrder;
import com.dukascopy.api.Instrument;
import com.jforex.programming.misc.StreamUtil;
import com.jforex.programming.order.command.CloseCommand;
import com.jforex.programming.order.command.MergeCommand;
import com.jforex.programming.order.command.SetAmountCommand;
import com.jforex.programming.order.command.SetGTTCommand;
import com.jforex.programming.order.command.SetLabelCommand;
import com.jforex.programming.order.command.SetOpenPriceCommand;
import com.jforex.programming.order.command.SetSLCommand;
import com.jforex.programming.order.command.SetTPCommand;
import com.jforex.programming.order.command.SubmitCommand;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.event.OrderEventTypeData;
import com.jforex.programming.position.OrderProcessState;
import com.jforex.programming.position.Position;
import com.jforex.programming.position.PositionFactory;
import com.jforex.programming.position.PositionOrders;
import com.jforex.programming.position.PositionRemoveTPSLTask;
import com.jforex.programming.position.PositionSingleTask;

import rx.Completable;
import rx.Observable;

public class OrderUtil {

    private final IEngine engine;
    private final PositionFactory positionFactory;
    private final OrderUtilHandler orderUtilHandler;
    private final PositionSingleTask positionSingleTask;
    private final PositionRemoveTPSLTask positionRemoveTPSLTask;

    private static final Logger logger = LogManager.getLogger(OrderUtil.class);

    public OrderUtil(final IEngine engine,
                     final PositionFactory positionFactory,
                     final OrderUtilHandler orderUtilHandler,
                     final PositionSingleTask positionSingleTask,
                     final PositionRemoveTPSLTask positionRemoveTPSLTask) {
        this.engine = engine;
        this.positionFactory = positionFactory;
        this.orderUtilHandler = orderUtilHandler;
        this.positionSingleTask = positionSingleTask;
        this.positionRemoveTPSLTask = positionRemoveTPSLTask;
    }

    public PositionOrders positionOrders(final Instrument instrument) {
        return positionFactory.forInstrument(checkNotNull(instrument));
    }

    private Position position(final Instrument instrument) {
        return (Position) positionOrders(instrument);
    }

    public Observable<OrderEvent> retryObservable(final OrderEvent orderEvent) {
        checkNotNull(orderEvent);

        return orderUtilHandler
                .rejectAsErrorObservable(orderEvent)
                .retryWhen(StreamUtil::retryObservable);
    }

    public Observable<OrderEvent> submitOrder(final OrderParams orderParams) {
        checkNotNull(orderParams);

        return orderUtilHandler
                .callObservable(new SubmitCommand(orderParams, engine))
                .doOnNext(submitEvent -> addOrderToPositionIfDone(submitEvent,
                                                                  OrderEventTypeData.submitData));
    }

    public Observable<OrderEvent> mergeOrders(final String mergeOrderLabel,
                                              final Collection<IOrder> toMergeOrders) {
        checkNotNull(mergeOrderLabel);
        checkNotNull(toMergeOrders);

        return Observable
                .just(toMergeOrders)
                .filter(orders -> orders.size() > 1)
                .flatMap(orders -> orderUtilHandler
                        .callObservable(new MergeCommand(mergeOrderLabel, toMergeOrders, engine))
                        .doOnNext(mergeEvent -> addOrderToPositionIfDone(mergeEvent,
                                                                         OrderEventTypeData.mergeData)));
    }

    private void addOrderToPositionIfDone(final OrderEvent orderEvent,
                                          final OrderEventTypeData orderEventTypeData) {
        if (orderEventTypeData.isDoneType(orderEvent.type())) {
            final IOrder order = orderEvent.order();
            position(order.getInstrument()).addOrder(order);
        }
    }

    public Observable<OrderEvent> mergePositionOrders(final String mergeOrderLabel,
                                                      final Instrument instrument) {
        checkNotNull(mergeOrderLabel);
        checkNotNull(instrument);

        final Set<IOrder> toMergeOrders = position(instrument).filled();

        logger.debug("Starting position merge for " + instrument + " with label " + mergeOrderLabel);
        return Observable
                .just(toMergeOrders)
                .filter(orders -> orders.size() > 1)
                .doOnNext(orders -> position(instrument).markAllOrders(OrderProcessState.ACTIVE))
                .flatMap(positionRemoveTPSLTask::observable)
                .toCompletable()
                .andThen(mergeOrders(mergeOrderLabel, toMergeOrders)
                        .flatMap(this::retryObservable))
                .doOnCompleted(() -> logger.debug("Position merge for " + instrument
                        + "  with label " + mergeOrderLabel + " was successful."));
    }

    public Completable closePosition(final Instrument instrument) {
        logger.debug("Starting position close for " + checkNotNull(instrument));
        final Position position = position(instrument);

        return Observable
                .from(position.filledOrOpened())
                .doOnSubscribe(() -> position.markAllOrders(OrderProcessState.ACTIVE))
                .flatMap(positionSingleTask::closeObservable)
                .doOnTerminate(() -> position(instrument)
                        .markAllOrders(OrderProcessState.IDLE))
                .doOnCompleted(() -> logger.debug("Closing position "
                        + instrument + " was successful."))
                .doOnError(e -> logger.error("Closing position " + instrument
                        + " failed! Exception: " + e.getMessage()))
                .toCompletable();
    }

    public Observable<OrderEvent> close(final IOrder orderToClose) {
        checkNotNull(orderToClose);

        return orderUtilHandler.callObservable(new CloseCommand(orderToClose));
    }

    public Observable<OrderEvent> setLabel(final IOrder orderToChangeLabel,
                                           final String newLabel) {
        checkNotNull(orderToChangeLabel);

        return orderUtilHandler
                .callObservable(new SetLabelCommand(orderToChangeLabel, newLabel));
    }

    public Observable<OrderEvent> setGoodTillTime(final IOrder orderToChangeGTT,
                                                  final long newGTT) {
        checkNotNull(orderToChangeGTT);

        return orderUtilHandler.callObservable(new SetGTTCommand(orderToChangeGTT, newGTT));
    }

    public Observable<OrderEvent> setOpenPrice(final IOrder orderToChangeOpenPrice,
                                               final double newOpenPrice) {
        checkNotNull(orderToChangeOpenPrice);

        return orderUtilHandler
                .callObservable(new SetOpenPriceCommand(orderToChangeOpenPrice, newOpenPrice));
    }

    public Observable<OrderEvent> setRequestedAmount(final IOrder orderToChangeAmount,
                                                     final double newRequestedAmount) {
        checkNotNull(orderToChangeAmount);

        return orderUtilHandler
                .callObservable(new SetAmountCommand(orderToChangeAmount, newRequestedAmount));
    }

    public Observable<OrderEvent> setStopLossPrice(final IOrder orderToChangeSL,
                                                   final double newSL) {
        checkNotNull(orderToChangeSL);

        return orderUtilHandler.callObservable(new SetSLCommand(orderToChangeSL, newSL));
    }

    public Observable<OrderEvent> setTakeProfitPrice(final IOrder orderToChangeTP,
                                                     final double newTP) {
        checkNotNull(orderToChangeTP);

        return orderUtilHandler.callObservable(new SetTPCommand(orderToChangeTP, newTP));
    }
}
