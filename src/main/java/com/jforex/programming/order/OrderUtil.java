package com.jforex.programming.order;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Collection;

import com.dukascopy.api.IOrder;
import com.dukascopy.api.Instrument;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.position.PositionOrders;
import com.jforex.programming.position.PositionUtil;

import io.reactivex.Observable;

public class OrderUtil {

    private final OrderTask orderTask;
    private final PositionUtil positionUtil;

    public OrderUtil(final OrderTask orderTaskExecutor,
                     final PositionUtil positionUtil) {
        this.orderTask = orderTaskExecutor;
        this.positionUtil = positionUtil;
    }

    public Observable<OrderEvent> submitOrder(final OrderParams orderParams) {
        return orderTask.submitOrder(checkNotNull(orderParams));
    }

    public Observable<OrderEvent> mergeOrders(final String mergeOrderLabel,
                                              final Collection<IOrder> toMergeOrders) {
        return orderTask.mergeOrders(checkNotNull(mergeOrderLabel), checkNotNull(toMergeOrders));
    }

    public Observable<OrderEvent> close(final IOrder order) {
        return orderTask.close(checkNotNull(order));
    }

    public Observable<OrderEvent> setLabel(final IOrder order,
                                           final String label) {
        return orderTask.setLabel(checkNotNull(order), checkNotNull(label));
    }

    public Observable<OrderEvent> setGoodTillTime(final IOrder order,
                                                  final long newGTT) {
        return orderTask.setGoodTillTime(checkNotNull(order), newGTT);
    }

    public Observable<OrderEvent> setRequestedAmount(final IOrder order,
                                                     final double newRequestedAmount) {
        return orderTask.setRequestedAmount(checkNotNull(order), newRequestedAmount);
    }

    public Observable<OrderEvent> setOpenPrice(final IOrder order,
                                               final double newOpenPrice) {
        return orderTask.setOpenPrice(checkNotNull(order), newOpenPrice);
    }

    public Observable<OrderEvent> setStopLossPrice(final IOrder order,
                                                   final double newSL) {
        return orderTask.setStopLossPrice(checkNotNull(order), newSL);
    }

    public Observable<OrderEvent> setTakeProfitPrice(final IOrder order,
                                                     final double newTP) {
        return orderTask.setTakeProfitPrice(checkNotNull(order), newTP);
    }

    public Observable<OrderEvent> cancelStopLossPrice(final IOrder order) {
        return orderTask.cancelStopLossPrice(checkNotNull(order));
    }

    public Observable<OrderEvent> cancelTakeProfitPrice(final IOrder order) {
        return orderTask.cancelTakeProfitPrice(checkNotNull(order));
    }

    public final Observable<OrderEvent> mergePosition(final Instrument instrument,
                                                      final String mergeOrderLabel) {
        checkNotNull(instrument);
        checkNotNull(mergeOrderLabel);

        return positionUtil.merge(instrument, mergeOrderLabel);
    }

    public final Observable<OrderEvent> closePosition(final Instrument instrument,
                                                      final String mergeOrderLabel) {
        checkNotNull(instrument);
        checkNotNull(mergeOrderLabel);

        return positionUtil.close(instrument, mergeOrderLabel);
    }

    public final Observable<OrderEvent> cancelStopLossPrice(final Instrument instrument) {
        return positionUtil.cancelStopLossPrice(checkNotNull(instrument));
    }

    public final Observable<OrderEvent> cancelTakeProfitPrice(final Instrument instrument) {
        return positionUtil.cancelTakeProfitPrice(checkNotNull(instrument));
    }

    public final PositionOrders positionOrders(final Instrument instrument) {
        return positionUtil.positionOrders(checkNotNull(instrument));
    }
}
