package com.jforex.programming.position;

import static com.jforex.programming.order.OrderStaticUtil.orderCallObservable;

import java.util.Set;

import com.jforex.programming.order.OrderParams;
import com.jforex.programming.order.OrderUtil;
import com.jforex.programming.order.event.OrderEvent;

import com.dukascopy.api.IOrder;

import rx.Observable;

public class PositionObservable {

    private final OrderUtil orderUtil;
    private final Observable<OrderEvent> orderEventObservable;

    public PositionObservable(final OrderUtil orderUtil,
                              final Observable<OrderEvent> orderEventObservable) {
        this.orderUtil = orderUtil;
        this.orderEventObservable = orderEventObservable;
    }

    public Observable<IOrder> forSubmitAndServerReply(final OrderParams orderParams) {
        return withEventFilter(forSubmit(orderParams),
                               TaskEventData.submitEvents);
    }

    public Observable<IOrder> forMergeAndServerReply(final String mergeLabel,
                                                     final Set<IOrder> toMergeOrders) {
        return withEventFilter(forMerge(mergeLabel, toMergeOrders),
                               TaskEventData.mergeEvents);
    }

    public Observable<IOrder> forChangeSLAndServerReply(final IOrder orderToChangeSL,
                                                        final double newSL) {
        return withEventFilter(forChangeSL(orderToChangeSL, newSL),
                               TaskEventData.changeSLEvents);
    }

    public Observable<IOrder> forChangeTPAndServerReply(final IOrder orderToChangeTP,
                                                        final double newTP) {
        return withEventFilter(forChangeTP(orderToChangeTP, newTP),
                               TaskEventData.changeTPEvents);
    }

    public Observable<IOrder> forCloseAndServerReply(final IOrder orderToClose) {
        return withEventFilter(forClose(orderToClose),
                               TaskEventData.closeEvents);
    }

    public Observable<IOrder> batchChangeSL(final Set<IOrder> ordersToChangeSL,
                                            final double newSL) {
        return Observable.from(ordersToChangeSL)
                .flatMap(order -> forChangeSLAndServerReply(order, newSL));
    }

    public Observable<IOrder> batchChangeTP(final Set<IOrder> ordersToChangeTP,
                                            final double newTP) {
        return Observable.from(ordersToChangeTP)
                .flatMap(order -> forChangeTPAndServerReply(order, newTP));
    }

    public Observable<IOrder> forSubmit(final OrderParams orderParams) {
        return orderCallObservable(() -> orderUtil.submit(orderParams));
    }

    public Observable<IOrder> forMerge(final String mergeLabel,
                                       final Set<IOrder> toMergeOrders) {
        return orderCallObservable(() -> orderUtil.merge(mergeLabel, toMergeOrders));
    }

    public Observable<IOrder> forChangeSL(final IOrder orderToChangeSL,
                                          final double newSL) {
        return orderCallObservable(() -> orderUtil.setSL(orderToChangeSL, newSL));
    }

    public Observable<IOrder> forChangeTP(final IOrder orderToChangeTP,
                                          final double newTP) {
        return orderCallObservable(() -> orderUtil.setTP(orderToChangeTP, newTP));
    }

    public Observable<IOrder> forClose(final IOrder orderToClose) {
        return orderCallObservable(() -> orderUtil.close(orderToClose));
    }

    private Observable<OrderEvent> filterOrderEventByOrder(final IOrder order) {
        return orderEventObservable.filter(orderEvent -> orderEvent.order() == order);
    }

    private Observable<IOrder> filterOrderEvent(final IOrder order,
                                                final TaskEventData taskEventData) {
        return filterOrderEventByOrder(order)
                .filter(orderEvent -> taskEventData.all().contains(orderEvent.type()))
                .flatMap(orderEvent -> Observable.just(orderEvent.order()));

    }

    private Observable<IOrder> withEventFilter(final Observable<IOrder> orderObs,
                                               final TaskEventData taskEventData) {
        return orderObs.flatMap(order -> filterOrderEvent(order, taskEventData));
    }
}
