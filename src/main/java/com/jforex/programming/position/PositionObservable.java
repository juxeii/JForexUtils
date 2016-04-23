package com.jforex.programming.position;

import static com.jforex.programming.order.OrderStaticUtil.orderCallObservable;

import java.util.Set;

import com.jforex.programming.order.OrderParams;
import com.jforex.programming.order.OrderUtil;
import com.jforex.programming.order.event.OrderEvent;

import com.dukascopy.api.IOrder;

import rx.Observable;

//@formatter:off
public class PositionObservable {

    private final OrderUtil orderUtil;
    private final Observable<OrderEvent> orderEventObservable;

    public PositionObservable(final OrderUtil orderUtil,
                              final Observable<OrderEvent> orderEventObservable) {
        this.orderUtil = orderUtil;
        this.orderEventObservable = orderEventObservable;
    }

    public Observable<OrderEvent> forSubmitAndServerReply(final OrderParams orderParams) {
        return forSubmit(orderParams)
               .flatMap(order -> filterOrderEvent(order, TaskEventData.submitEvents));
    }

    public Observable<OrderEvent> forMergeAndServerReply(final String mergeLabel,
                                                         final Set<IOrder> toMergeOrders) {
        return forMerge(mergeLabel, toMergeOrders)
               .flatMap(order -> filterOrderEvent(order, TaskEventData.mergeEvents));
    }

    public Observable<OrderEvent> forChangeSLAndServerReply(final IOrder orderToChangeSL,
                                                            final double newSL) {
        return forChangeSL(orderToChangeSL,newSL)
               .flatMap(order -> filterOrderEvent(order, TaskEventData.changeSLEvents));
    }

    public Observable<OrderEvent> forChangeTPAndServerReply(final IOrder orderToChangeTP,
                                                            final double newTP) {
        return forChangeTP(orderToChangeTP,newTP)
               .flatMap(order -> filterOrderEvent(order, TaskEventData.changeTPEvents));
    }

    public Observable<OrderEvent> forCloseAndServerReply(final IOrder orderToClose) {
        return forClose(orderToClose)
               .flatMap(order -> filterOrderEvent(order, TaskEventData.closeEvents));
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

    private Observable<OrderEvent> filterOrderEvent(final IOrder order,
                                                    final TaskEventData taskEventData) {
        return filterOrderEventByOrder(order)
               .filter(orderEvent -> taskEventData.all().contains(orderEvent.type()));
    }
}
