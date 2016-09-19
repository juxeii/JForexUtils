package com.jforex.programming.order;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Collection;

import com.dukascopy.api.IOrder;
import com.dukascopy.api.Instrument;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.position.CancelSLPositionCommand;
import com.jforex.programming.position.CancelTPPositionCommand;
import com.jforex.programming.position.ClosePositionCommand;
import com.jforex.programming.position.PositionOrders;
import com.jforex.programming.position.PositionTask;

import io.reactivex.Observable;

public class OrderUtil {

    private final OrderTask orderTask;
    private final PositionTask positionTask;

    public OrderUtil(final OrderTask orderTaskExecutor,
                     final PositionTask positionTask) {
        this.orderTask = orderTaskExecutor;
        this.positionTask = positionTask;
    }

    public Observable<OrderEvent> submitOrder(final OrderParams orderParams) {
        checkNotNull(orderParams);

        return orderTask.submitOrder(orderParams);
    }

    public Observable<OrderEvent> mergeOrders(final String mergeOrderLabel,
                                              final Collection<IOrder> toMergeOrders) {
        checkNotNull(mergeOrderLabel);
        checkNotNull(toMergeOrders);

        return orderTask.mergeOrders(mergeOrderLabel, toMergeOrders);
    }

    public Observable<OrderEvent> close(final IOrder order) {
        checkNotNull(order);

        return orderTask.close(order);
    }

    public Observable<OrderEvent> setLabel(final IOrder order,
                                           final String label) {
        checkNotNull(order);
        checkNotNull(label);

        return orderTask.setLabel(order, label);
    }

    public Observable<OrderEvent> setGoodTillTime(final IOrder order,
                                                  final long newGTT) {
        checkNotNull(order);

        return orderTask.setGoodTillTime(order, newGTT);
    }

    public Observable<OrderEvent> setRequestedAmount(final IOrder order,
                                                     final double newRequestedAmount) {
        checkNotNull(order);

        return orderTask.setRequestedAmount(order, newRequestedAmount);
    }

    public Observable<OrderEvent> setOpenPrice(final IOrder order,
                                               final double newOpenPrice) {
        checkNotNull(order);

        return orderTask.setOpenPrice(order, newOpenPrice);
    }

    public Observable<OrderEvent> setStopLossPrice(final IOrder order,
                                                   final double newSL) {
        checkNotNull(order);

        return orderTask.setStopLossPrice(order, newSL);
    }

    public Observable<OrderEvent> setTakeProfitPrice(final IOrder order,
                                                     final double newTP) {
        checkNotNull(order);

        return orderTask.setTakeProfitPrice(order, newTP);
    }

    public final Observable<OrderEvent> mergePosition(final Instrument instrument,
                                                      final String mergeOrderLabel) {
        checkNotNull(instrument);
        checkNotNull(mergeOrderLabel);

        return positionTask.merge(instrument, mergeOrderLabel);
    }

    public Observable<OrderEvent> closePosition(final ClosePositionCommand command) {
        checkNotNull(command);

        return positionTask.close(command);
    }

    public Observable<OrderEvent> cancelStopLossPriceOnPosition(final CancelSLPositionCommand command) {
        return positionTask.cancelStopLossPrice(command);
    }

    public Observable<OrderEvent> cancelTakeProfitPriceOnPosition(final CancelTPPositionCommand command) {
        return positionTask.cancelTakeProfitPrice(command);
    }

    public final PositionOrders positionOrders(final Instrument instrument) {
        checkNotNull(instrument);

        return positionTask.positionOrders(instrument);
    }
}
