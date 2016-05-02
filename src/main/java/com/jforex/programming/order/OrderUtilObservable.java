package com.jforex.programming.order;

import static com.jforex.programming.order.OrderStaticUtil.isClosed;
import static com.jforex.programming.order.OrderStaticUtil.isSLSetTo;
import static com.jforex.programming.order.OrderStaticUtil.isTPSetTo;

import java.util.Collection;
import java.util.Optional;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.position.OrderEventTypesInfo;
import com.jforex.programming.position.PositionTaskRejectException;

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
                : changeObs(orderUtil.close(orderToClose),
                            orderToClose,
                            OrderEventTypesInfo.closeEvents);
    }

    public Observable<IOrder> setLabel(final IOrder orderToChangeLabel,
                                       final String newLabel) {
        return orderToChangeLabel.getLabel().equals(newLabel)
                ? Observable.just(orderToChangeLabel)
                : changeObs(orderUtil.setLabel(orderToChangeLabel, newLabel),
                            orderToChangeLabel,
                            OrderEventTypesInfo.changeLabelEvents);
    }

    public Observable<IOrder> setGTT(final IOrder orderToChangeGTT,
                                     final long newGTT) {
        return orderToChangeGTT.getGoodTillTime() == newGTT
                ? Observable.just(orderToChangeGTT)
                : changeObs(orderUtil.setGTT(orderToChangeGTT, newGTT),
                            orderToChangeGTT,
                            OrderEventTypesInfo.changeGTTEvents);
    }

    public Observable<IOrder> setOpenPrice(final IOrder orderToChangeOpenPrice,
                                           final double newOpenPrice) {
        return orderToChangeOpenPrice.getOpenPrice() == newOpenPrice
                ? Observable.just(orderToChangeOpenPrice)
                : changeObs(orderUtil.setOpenPrice(orderToChangeOpenPrice, newOpenPrice),
                            orderToChangeOpenPrice,
                            OrderEventTypesInfo.changeOpenPriceEvents);
    }

    public Observable<IOrder> setAmount(final IOrder orderToChangeAmount,
                                        final double newAmount) {
        return orderToChangeAmount.getRequestedAmount() == newAmount
                ? Observable.just(orderToChangeAmount)
                : changeObs(orderUtil.setAmount(orderToChangeAmount, newAmount),
                            orderToChangeAmount,
                            OrderEventTypesInfo.changeAmountEvents);
    }

    public Observable<IOrder> setSL(final IOrder orderToChangeSL,
                                    final double newSL) {
        return isSLSetTo(newSL).test(orderToChangeSL)
                ? Observable.just(orderToChangeSL)
                : changeObs(orderUtil.setSL(orderToChangeSL, newSL),
                            orderToChangeSL,
                            OrderEventTypesInfo.changeSLEvents);
    }

    public Observable<IOrder> setTP(final IOrder orderToChangeTP,
                                    final double newTP) {
        return isTPSetTo(newTP).test(orderToChangeTP)
                ? Observable.just(orderToChangeTP)
                : changeObs(orderUtil.setTP(orderToChangeTP, newTP),
                            orderToChangeTP,
                            OrderEventTypesInfo.changeTPEvents);
    }

    private Observable<IOrder> changeObs(final Optional<Exception> exceptionOpt,
                                         final IOrder orderToChange,
                                         final OrderEventTypesInfo orderEventData) {
        return exceptionOpt.isPresent()
                ? Observable.error(exceptionOpt.get())
                : Observable
                        .just(orderToChange)
                        .flatMap(order -> orderChangeCallObservable(order, orderEventData));
    }

    private Observable<IOrder> createObs(final OrderCreateResult createResult,
                                         final OrderEventTypesInfo orderEventData) {
        final Optional<Exception> exceptionOpt = createResult.exceptionOpt();
        return exceptionOpt.isPresent()
                ? Observable.error(exceptionOpt.get())
                : Observable
                        .just(createResult.orderOpt().get())
                        .flatMap(order -> orderChangeCallObservable(order, orderEventData));
    }

    private final Observable<IOrder> orderChangeCallObservable(final IOrder order,
                                                               final OrderEventTypesInfo orderEventData) {
        return Observable.create(subscriber -> {
            orderEventObservable
                    .filter(orderEvent -> orderEvent.order() == order)
                    .filter(orderEvent -> orderEventData.all().contains(orderEvent.type()))
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
