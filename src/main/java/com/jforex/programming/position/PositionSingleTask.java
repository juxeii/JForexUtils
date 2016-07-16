package com.jforex.programming.position;

import static com.jforex.programming.order.OrderStaticUtil.isClosed;
import static com.jforex.programming.order.OrderStaticUtil.isSLSetTo;
import static com.jforex.programming.order.OrderStaticUtil.isTPSetTo;

import com.jforex.programming.misc.StreamUtil;
import com.jforex.programming.order.OrderUtilHandler;
import com.jforex.programming.order.command.OrderCallCommand;
import com.jforex.programming.order.event.OrderEvent;

import com.dukascopy.api.IOrder;

import rx.Observable;

public class PositionSingleTask {

    private final OrderUtilHandler orderUtilHandler;

    public PositionSingleTask(final OrderUtilHandler orderUtilHandler) {
        this.orderUtilHandler = orderUtilHandler;
    }

    public Observable<OrderEvent> setSLObservable(final IOrder orderToChangeSL,
                                                  final double newSL) {
        return Observable
                .just(orderToChangeSL)
                .filter(order -> !isSLSetTo(newSL).test(order))
                .flatMap(order -> orderUtilHandler
                        .observable(OrderCallCommand.setSLCommand(orderToChangeSL, newSL)))
                .retryWhen(StreamUtil::positionTaskRetry);
    }

    public Observable<OrderEvent> setTPObservable(final IOrder orderToChangeTP,
                                                  final double newTP) {
        return Observable
                .just(orderToChangeTP)
                .filter(order -> !isTPSetTo(newTP).test(order))
                .flatMap(order -> orderUtilHandler
                        .observable(OrderCallCommand.setTPCommand(orderToChangeTP, newTP)))
                .retryWhen(StreamUtil::positionTaskRetry);
    }

    public Observable<OrderEvent> closeObservable(final IOrder orderToClose) {
        return Observable
                .just(orderToClose)
                .filter(order -> !isClosed.test(order))
                .flatMap(order -> orderUtilHandler
                        .observable(OrderCallCommand.closeCommand(orderToClose)))
                .retryWhen(StreamUtil::positionTaskRetry);
    }
}
