package com.jforex.programming.order;

import java.util.Collection;

import com.google.common.base.Supplier;
import com.jforex.programming.order.call.OrderCallResult;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.position.OrderEventData;
import com.jforex.programming.position.PositionTaskRejectException;

import com.dukascopy.api.IOrder;

import rx.Observable;
import rx.Subscriber;

public class OrderUtilObservable {

    private final OrderUtil orderUtil;
    private final Observable<OrderEvent> orderEventObservable;

    public OrderUtilObservable(final OrderUtil orderUtil,
                               final Observable<OrderEvent> orderEventObservable) {
        this.orderUtil = orderUtil;
        this.orderEventObservable = orderEventObservable;
    }

    public Observable<IOrder> submit(final OrderParams orderParams) {
        return orderCallObservable(() -> orderUtil.submit(orderParams),
                                   OrderEventData.submitEvents);
    }

    public Observable<IOrder> merge(final String mergeOrderLabel,
                                    final Collection<IOrder> toMergeOrders) {
        return orderCallObservable(() -> orderUtil.merge(mergeOrderLabel, toMergeOrders),
                                   OrderEventData.mergeEvents);
    }

    public Observable<IOrder> close(final IOrder orderToClose) {
        return orderCallObservable(() -> orderUtil.close(orderToClose),
                                   OrderEventData.closeEvents);
    }

    public Observable<IOrder> setLabel(final IOrder orderToChangeLabel,
                                       final String newLabel) {
        return orderCallObservable(() -> orderUtil.setLabel(orderToChangeLabel, newLabel),
                                   OrderEventData.changeLabelEvents);
    }

    public Observable<IOrder> setGTT(final IOrder orderToChangeGTT,
                                     final long newGTT) {
        return orderCallObservable(() -> orderUtil.setGTT(orderToChangeGTT, newGTT),
                                   OrderEventData.changeGTTEvents);
    }

    public Observable<IOrder> setOpenPrice(final IOrder orderToChangeOpenPrice,
                                           final double newOpenPrice) {
        return orderCallObservable(() -> orderUtil.setOpenPrice(orderToChangeOpenPrice, newOpenPrice),
                                   OrderEventData.changeOpenPriceEvents);
    }

    public Observable<IOrder> setAmount(final IOrder orderToChangeAmount,
                                        final double newAmount) {
        return orderCallObservable(() -> orderUtil.setAmount(orderToChangeAmount, newAmount),
                                   OrderEventData.changeAmountEvents);
    }

    public Observable<IOrder> setSL(final IOrder orderToChangeSL,
                                    final double newSL) {
        return orderCallObservable(() -> orderUtil.setSL(orderToChangeSL, newSL),
                                   OrderEventData.changeSLEvents);
    }

    public Observable<IOrder> setTP(final IOrder orderToChangeTP,
                                    final double newTP) {
        return orderCallObservable(() -> orderUtil.setTP(orderToChangeTP, newTP),
                                   OrderEventData.changeTPEvents);
    }

    private final Observable<IOrder> orderCallObservable(final Supplier<OrderCallResult> orderCall,
                                                         final OrderEventData orderEventData) {
        return Observable.create(subscriber -> {
            final OrderCallResult orderCallResult = orderCall.get();
            if (orderCallResult.exceptionOpt().isPresent())
                subscriber.onError(orderCallResult.exceptionOpt().get());
            else {
                final IOrder order = orderFromCallResult(orderCallResult);
                orderEventObservable.filter(orderEvent -> orderEvent.order() == order)
                        .filter(orderEvent -> orderEventData.all().contains(orderEvent.type()))
                        .flatMap(orderEvent -> processOrderEvent(orderEvent, orderEventData, subscriber))
                        .subscribe();
            }
        });
    }

    private Observable<IOrder> processOrderEvent(final OrderEvent orderEvent,
                                                 final OrderEventData orderEventData,
                                                 final Subscriber<? super IOrder> externalSubscriber) {
        return Observable.create(subscriber -> {
            if (orderEventData.forReject().contains(orderEvent.type())) {
                externalSubscriber.onError(new PositionTaskRejectException("", orderEvent));
            } else if (orderEventData.forDone().contains(orderEvent.type())) {
                externalSubscriber.onNext(orderEvent.order());
                externalSubscriber.onCompleted();
            }
        });
    }

    private IOrder orderFromCallResult(final OrderCallResult orderCallResult) {
        return orderCallResult instanceof OrderCreateResult
                ? ((OrderCreateResult) orderCallResult).orderOpt().get()
                : ((OrderChangeResult) orderCallResult).order();
    }
}
