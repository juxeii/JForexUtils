package com.jforex.programming.order;

import static com.jforex.programming.order.OrderStaticUtil.isClosed;
import static com.jforex.programming.order.OrderStaticUtil.isSLSetTo;
import static com.jforex.programming.order.OrderStaticUtil.isTPSetTo;

import java.util.Collection;
import java.util.Optional;

import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.position.OrderEventTypesInfo;
import com.jforex.programming.position.PositionTaskRejectException;

import com.dukascopy.api.IOrder;

import rx.Observable;

public class OrderUtilObservable {

    private final OrderUtil orderUtil;
    private final Observable<OrderEvent> orderEventObservable;

    public OrderUtilObservable(final OrderUtil orderUtil,
                               final Observable<OrderEvent> orderEventObservable) {
        this.orderUtil = orderUtil;
        this.orderEventObservable = orderEventObservable;
    }

    public Observable<OrderEvent> orderEventObservable() {
        return orderEventObservable;
    }

    public Observable<IOrder> submit(final OrderParams orderParams) {
        return orderCallObservable(orderUtil.submit(orderParams),
                                   OrderEventTypesInfo.submitEvents);
    }

    public Observable<IOrder> merge(final String mergeOrderLabel,
                                    final Collection<IOrder> toMergeOrders) {
        return orderCallObservable(orderUtil.merge(mergeOrderLabel, toMergeOrders),
                                   OrderEventTypesInfo.mergeEvents);
    }

    public Observable<IOrder> close(final IOrder orderToClose) {
        return isClosed.test(orderToClose)
                ? Observable.just(orderToClose)
                : orderCallObservable(new OrderCreateResult(Optional.of(orderToClose),
                                                            orderUtil.close(orderToClose)),
                                      OrderEventTypesInfo.closeEvents);
    }

    public Observable<IOrder> setLabel(final IOrder orderToChangeLabel,
                                       final String newLabel) {
        return orderToChangeLabel.getLabel().equals(newLabel)
                ? Observable.just(orderToChangeLabel)
                : orderCallObservable(new OrderCreateResult(Optional.of(orderToChangeLabel),
                                                            orderUtil.setLabel(orderToChangeLabel, newLabel)),
                                      OrderEventTypesInfo.changeLabelEvents);
    }

    public Observable<IOrder> setGTT(final IOrder orderToChangeGTT,
                                     final long newGTT) {
        return orderToChangeGTT.getGoodTillTime() == newGTT
                ? Observable.just(orderToChangeGTT)
                : orderCallObservable(new OrderCreateResult(Optional.of(orderToChangeGTT),
                                                            orderUtil.setGTT(orderToChangeGTT, newGTT)),
                                      OrderEventTypesInfo.changeGTTEvents);
    }

    public Observable<IOrder> setOpenPrice(final IOrder orderToChangeOpenPrice,
                                           final double newOpenPrice) {
        return orderToChangeOpenPrice.getOpenPrice() == newOpenPrice
                ? Observable.just(orderToChangeOpenPrice)
                : orderCallObservable(new OrderCreateResult(Optional.of(orderToChangeOpenPrice),
                                                            orderUtil.setOpenPrice(orderToChangeOpenPrice,
                                                                                   newOpenPrice)),
                                      OrderEventTypesInfo.changeOpenPriceEvents);
    }

    public Observable<IOrder> setAmount(final IOrder orderToChangeAmount,
                                        final double newAmount) {
        return orderToChangeAmount.getRequestedAmount() == newAmount
                ? Observable.just(orderToChangeAmount)
                : orderCallObservable(new OrderCreateResult(Optional.of(orderToChangeAmount),
                                                            orderUtil.setAmount(orderToChangeAmount, newAmount)),
                                      OrderEventTypesInfo.changeAmountEvents);
    }

    public Observable<IOrder> setSL(final IOrder orderToChangeSL,
                                    final double newSL) {
        return isSLSetTo(newSL).test(orderToChangeSL)
                ? Observable.just(orderToChangeSL)
                : orderCallObservable(new OrderCreateResult(Optional.of(orderToChangeSL),
                                                            orderUtil.setSL(orderToChangeSL, newSL)),
                                      OrderEventTypesInfo.changeSLEvents);
    }

    public Observable<IOrder> setTP(final IOrder orderToChangeTP,
                                    final double newTP) {
        return isTPSetTo(newTP).test(orderToChangeTP)
                ? Observable.just(orderToChangeTP)
                : orderCallObservable(new OrderCreateResult(Optional.of(orderToChangeTP),
                                                            orderUtil.setTP(orderToChangeTP, newTP)),
                                      OrderEventTypesInfo.changeTPEvents);
    }

    private final Observable<IOrder> orderCallObservable(final OrderCreateResult orderCreateResult,
                                                         final OrderEventTypesInfo orderEventData) {
        return Observable.create(subscriber -> {
            if (orderCreateResult.exceptionOpt().isPresent())
                subscriber.onError(orderCreateResult.exceptionOpt().get());
            else {
                filterForEvents(orderCreateResult.orderOpt().get(), orderEventData)
                        .subscribe(orderEvent -> {
                            subscriber.onNext(orderEvent.order());
                            subscriber.onCompleted();
                        }, t -> subscriber.onError(t));
            }
        });
    }

    private final Observable<OrderEvent> filterForEvents(final IOrder order,
                                                         final OrderEventTypesInfo orderEventData) {
        return orderEventObservable.filter(orderEvent -> orderEvent.order() == order)
                .filter(orderEvent -> orderEventData.all().contains(orderEvent.type()))
                .flatMap(orderEvent -> orderEventEvaluationObs(orderEvent, orderEventData));
    }

    private final Observable<OrderEvent> orderEventEvaluationObs(final OrderEvent orderEvent,
                                                                 final OrderEventTypesInfo orderEventData) {
        return Observable.create(subscriber -> {
            if (orderEventData.isRejectType(orderEvent.type()))
                subscriber.onError(new PositionTaskRejectException("", orderEvent));
            else {
                subscriber.onNext(orderEvent);
                subscriber.onCompleted();
            }
        });
    }
}
