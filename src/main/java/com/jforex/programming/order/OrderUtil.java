package com.jforex.programming.order;

import java.util.Collection;
import java.util.Set;

import com.dukascopy.api.IEngine;
import com.dukascopy.api.IOrder;
import com.dukascopy.api.Instrument;
import com.jforex.programming.order.command.CloseCommand;
import com.jforex.programming.order.command.MergeCommand;
import com.jforex.programming.order.command.MergePositionCommand;
import com.jforex.programming.order.command.SetAmountCommand;
import com.jforex.programming.order.command.SetGTTCommand;
import com.jforex.programming.order.command.SetLabelCommand;
import com.jforex.programming.order.command.SetOpenPriceCommand;
import com.jforex.programming.order.command.SetSLCommand;
import com.jforex.programming.order.command.SetTPCommand;
import com.jforex.programming.order.command.SubmitCommand;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.event.OrderEventTypeData;
import com.jforex.programming.position.Position;
import com.jforex.programming.position.PositionFactory;
import com.jforex.programming.position.PositionOrders;
import com.jforex.programming.position.RestoreSLTPData;
import com.jforex.programming.position.RestoreSLTPPolicy;

import rx.Completable;
import rx.Observable;

public class OrderUtil {

    private final IEngine engine;
    private final PositionFactory positionFactory;
    private final OrderPositionHandler orderPositionHandler;
    private final OrderUtilHandler orderUtilHandler;

    public OrderUtil(final IEngine engine,
                     final PositionFactory positionFactory,
                     final OrderPositionHandler orderPositionHandler,
                     final OrderUtilHandler orderUtilHandler) {
        this.engine = engine;
        this.positionFactory = positionFactory;
        this.orderPositionHandler = orderPositionHandler;
        this.orderUtilHandler = orderUtilHandler;
    }

    public PositionOrders positionOrders(final Instrument instrument) {
        return positionFactory.forInstrument(instrument);
    }

    private Position position(final Instrument instrument) {
        return (Position) positionOrders(instrument);
    }

    public Observable<OrderEvent> submitOrder(final OrderParams orderParams) {
        return orderUtilHandler
                .callObservable(new SubmitCommand(orderParams, engine))
                .doOnNext(submitEvent -> addOrderToPositionIfDone(submitEvent,
                                                                  OrderEventTypeData.submitData));
    }

    public Observable<OrderEvent> mergeOrders(final String mergeOrderLabel,
                                              final Collection<IOrder> toMergeOrders) {
        return orderPositionHandler
                .mergeOrders(new MergeCommand(mergeOrderLabel, toMergeOrders, engine));
    }

    private void addOrderToPositionIfDone(final OrderEvent orderEvent,
                                          final OrderEventTypeData orderEventTypeData) {
        if (orderEventTypeData.isDoneType(orderEvent.type())) {
            final IOrder order = orderEvent.order();
            position(order.getInstrument()).addOrder(order);
        }
    }

    public Observable<OrderEvent> mergePositionOrders(final String mergeOrderLabel,
                                                      final Instrument instrument,
                                                      final RestoreSLTPPolicy restoreSLTPPolicy) {
        final Set<IOrder> toMergeOrders = positionOrders(instrument).filled();
        if (toMergeOrders.size() < 2)
            return Observable.empty();

        final RestoreSLTPData restoreSLTPData =
                new RestoreSLTPData(restoreSLTPPolicy.restoreSL(toMergeOrders),
                                    restoreSLTPPolicy.restoreTP(toMergeOrders));
        final MergePositionCommand command = new MergePositionCommand(mergeOrderLabel,
                                                                      toMergeOrders,
                                                                      instrument,
                                                                      restoreSLTPData,
                                                                      engine);
        return orderPositionHandler.mergePositionOrders(command);
    }

    public Completable closePosition(final Instrument instrument) {
        return orderPositionHandler.closePosition(instrument);
    }

    public Observable<OrderEvent> close(final IOrder orderToClose) {
        return orderUtilHandler.callObservable(new CloseCommand(orderToClose));
    }

    public Observable<OrderEvent> setLabel(final IOrder orderToChangeLabel,
                                           final String newLabel) {
        return orderUtilHandler
                .callObservable(new SetLabelCommand(orderToChangeLabel, newLabel));
    }

    public Observable<OrderEvent> setGoodTillTime(final IOrder orderToChangeGTT,
                                                  final long newGTT) {
        return orderUtilHandler.callObservable(new SetGTTCommand(orderToChangeGTT, newGTT));
    }

    public Observable<OrderEvent> setOpenPrice(final IOrder orderToChangeOpenPrice,
                                               final double newOpenPrice) {
        return orderUtilHandler
                .callObservable(new SetOpenPriceCommand(orderToChangeOpenPrice, newOpenPrice));
    }

    public Observable<OrderEvent> setRequestedAmount(final IOrder orderToChangeAmount,
                                                     final double newRequestedAmount) {
        return orderUtilHandler
                .callObservable(new SetAmountCommand(orderToChangeAmount, newRequestedAmount));
    }

    public Observable<OrderEvent> setStopLossPrice(final IOrder orderToChangeSL,
                                                   final double newSL) {
        return orderUtilHandler.callObservable(new SetSLCommand(orderToChangeSL, newSL));
    }

    public Observable<OrderEvent> setTakeProfitPrice(final IOrder orderToChangeTP,
                                                     final double newTP) {
        return orderUtilHandler.callObservable(new SetTPCommand(orderToChangeTP, newTP));
    }
}
