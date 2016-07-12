package com.jforex.programming.position;

import static com.jforex.programming.order.OrderStaticUtil.isClosed;
import static com.jforex.programming.order.OrderStaticUtil.isSLSetTo;
import static com.jforex.programming.order.OrderStaticUtil.isTPSetTo;

import java.util.Collection;

import com.jforex.programming.misc.StreamUtil;
import com.jforex.programming.order.OrderChangeUtil;
import com.jforex.programming.order.OrderCreateUtil;
import com.jforex.programming.order.event.OrderEvent;

import com.dukascopy.api.IOrder;

import rx.Observable;

public class PositionSingleTask {

    private final OrderCreateUtil orderCreateUtil;
    private final OrderChangeUtil orderChangeUtil;

    public PositionSingleTask(final OrderCreateUtil orderCreateUtil,
                              final OrderChangeUtil orderChangeUtil) {
        this.orderCreateUtil = orderCreateUtil;
        this.orderChangeUtil = orderChangeUtil;
    }

    public Observable<OrderEvent> setSLObservable(final IOrder orderToChangeSL,
                                                  final double newSL) {
        return Observable
                .just(orderToChangeSL)
                .filter(order -> !isSLSetTo(newSL).test(order))
                .flatMap(order -> orderChangeUtil.setStopLossPrice(order, newSL))
                .retryWhen(StreamUtil::positionTaskRetry);
    }

    public Observable<OrderEvent> setTPObservable(final IOrder orderToChangeTP,
                                                  final double newTP) {
        return Observable
                .just(orderToChangeTP)
                .filter(order -> !isTPSetTo(newTP).test(order))
                .flatMap(order -> orderChangeUtil.setTakeProfitPrice(order, newTP))
                .retryWhen(StreamUtil::positionTaskRetry);
    }

    public Observable<OrderEvent> mergeObservable(final String mergeOrderLabel,
                                                  final Collection<IOrder> toMergeOrders) {
        return Observable
                .just(mergeOrderLabel)
                .flatMap(order -> orderCreateUtil.mergeOrders(mergeOrderLabel, toMergeOrders))
                .retryWhen(StreamUtil::positionTaskRetry);
    }

    public Observable<OrderEvent> closeObservable(final IOrder orderToClose) {
        return Observable
                .just(orderToClose)
                .filter(order -> !isClosed.test(order))
                .flatMap(orderChangeUtil::close)
                .retryWhen(StreamUtil::positionTaskRetry);
    }
}
