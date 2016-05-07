package com.jforex.programming.order;

import static com.jforex.programming.order.OrderStaticUtil.isClosed;
import static com.jforex.programming.order.OrderStaticUtil.isSLSetTo;
import static com.jforex.programming.order.OrderStaticUtil.isTPSetTo;

import java.util.Collection;

import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.position.OrderEventTypesInfo;
import com.jforex.programming.position.PositionTaskRejectException;

import com.dukascopy.api.IOrder;

import rx.Observable;

public class OrderUtilObservable {

    private final OrderUtil orderUtil;

    public OrderUtilObservable(final OrderUtil orderUtil) {
        this.orderUtil = orderUtil;
    }

    public Observable<IOrder> submit(final OrderParams orderParams) {
        return createObs(orderUtil.submit(orderParams),
                         OrderEventTypesInfo.submitEvents);
    }

    public Observable<IOrder> merge(final String mergeOrderLabel,
                                    final Collection<IOrder> toMergeOrders) {
        return createObs(orderUtil.merge(mergeOrderLabel, toMergeOrders),
                         OrderEventTypesInfo.mergeEvents);
    }

    public Observable<IOrder> close(final IOrder orderToClose) {
        return isClosed.test(orderToClose)
                ? Observable.just(orderToClose)
                : createObs(orderUtil.close(orderToClose),
                            OrderEventTypesInfo.closeEvents);
    }

    public Observable<IOrder> setLabel(final IOrder orderToChangeLabel,
                                       final String newLabel) {
        return orderToChangeLabel.getLabel().equals(newLabel)
                ? Observable.just(orderToChangeLabel)
                : createObs(orderUtil.setLabel(orderToChangeLabel, newLabel),
                            OrderEventTypesInfo.changeLabelEvents);
    }

    public Observable<IOrder> setGTT(final IOrder orderToChangeGTT,
                                     final long newGTT) {
        return orderToChangeGTT.getGoodTillTime() == newGTT
                ? Observable.just(orderToChangeGTT)
                : createObs(orderUtil.setGTT(orderToChangeGTT, newGTT),
                            OrderEventTypesInfo.changeGTTEvents);
    }

    public Observable<IOrder> setOpenPrice(final IOrder orderToChangeOpenPrice,
                                           final double newOpenPrice) {
        return orderToChangeOpenPrice.getOpenPrice() == newOpenPrice
                ? Observable.just(orderToChangeOpenPrice)
                : createObs(orderUtil.setOpenPrice(orderToChangeOpenPrice, newOpenPrice),
                            OrderEventTypesInfo.changeOpenPriceEvents);
    }

    public Observable<IOrder> setAmount(final IOrder orderToChangeAmount,
                                        final double newAmount) {
        return orderToChangeAmount.getRequestedAmount() == newAmount
                ? Observable.just(orderToChangeAmount)
                : createObs(orderUtil.setAmount(orderToChangeAmount, newAmount),
                            OrderEventTypesInfo.changeAmountEvents);
    }

    public Observable<IOrder> setSL(final IOrder orderToChangeSL,
                                    final double newSL) {
        return isSLSetTo(newSL).test(orderToChangeSL)
                ? Observable.just(orderToChangeSL)
                : createObs(orderUtil.setSL(orderToChangeSL, newSL),
                            OrderEventTypesInfo.changeSLEvents);
    }

    public Observable<IOrder> setTP(final IOrder orderToChangeTP,
                                    final double newTP) {
        return isTPSetTo(newTP).test(orderToChangeTP)
                ? Observable.just(orderToChangeTP)
                : createObs(orderUtil.setTP(orderToChangeTP, newTP),
                            OrderEventTypesInfo.changeTPEvents);
    }

    private Observable<IOrder> createObs(final Observable<OrderEvent> eventObs,
                                         final OrderEventTypesInfo orderEventData) {
        return Observable.create(subscriber -> {
            eventObs.filter(orderEvent -> orderEventData.all().contains(orderEvent.type()))
                    .flatMap(orderEvent -> orderEventEvaluationObs(orderEvent, orderEventData))
                    .subscribe(orderEvent -> {
                        subscriber.onNext(orderEvent.order());
                        subscriber.onCompleted();
                    }, throwable -> subscriber.onError(throwable));
        });
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
