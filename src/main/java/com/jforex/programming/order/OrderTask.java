package com.jforex.programming.order;

import static com.jforex.programming.order.OrderStaticUtil.isAmountSetTo;
import static com.jforex.programming.order.OrderStaticUtil.isClosed;
import static com.jforex.programming.order.OrderStaticUtil.isGTTSetTo;
import static com.jforex.programming.order.OrderStaticUtil.isLabelSetTo;
import static com.jforex.programming.order.OrderStaticUtil.isOpenPriceSetTo;
import static com.jforex.programming.order.OrderStaticUtil.isSLSetTo;
import static com.jforex.programming.order.OrderStaticUtil.isTPSetTo;

import java.util.Collection;

import com.dukascopy.api.IOrder;
import com.jforex.programming.misc.JForexUtil;
import com.jforex.programming.order.MergeCommandWithParent.MergeExecutionMode;
import com.jforex.programming.order.call.OrderCallReason;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.settings.PlatformSettings;

import io.reactivex.Observable;
import io.reactivex.functions.Function;

public class OrderTask {

    private final OrderTaskExecutor orderTaskExecutor;
    private final OrderUtilHandler orderUtilHandler;

    private static final PlatformSettings platformSettings = JForexUtil.platformSettings;

    public OrderTask(final OrderTaskExecutor orderTaskExecutor,
                     final OrderUtilHandler orderUtilHandler) {
        this.orderTaskExecutor = orderTaskExecutor;
        this.orderUtilHandler = orderUtilHandler;
    }

    public Observable<OrderEvent> submitOrder(final OrderParams orderParams) {
        return orderTaskExecutor
            .submitOrder(orderParams)
            .toObservable()
            .flatMap(order -> orderUtilObservable(order, OrderCallReason.SUBMIT));
    }

    private Observable<OrderEvent> mergeOrdersWithExecutor(final String mergeOrderLabel,
                                                           final Collection<IOrder> toMergeOrders) {
        return toMergeOrders.size() < 2
                ? Observable.empty()
                : orderTaskExecutor
                    .mergeOrders(mergeOrderLabel, toMergeOrders)
                    .toObservable()
                    .flatMap(order -> orderUtilObservable(order, OrderCallReason.MERGE));
    }

    public Observable<OrderEvent> mergeOrders(final MergeCommand command) {
        final Collection<IOrder> toMergeOrders = command.toMergeOrders();
        final MergeCommandWithParent innerMerge = command.mergeCommandWithParent();

        final Observable<OrderEvent> cancelSLTP = createCancelSLTP(toMergeOrders, innerMerge);
        final Observable<OrderEvent> merge = mergeOrdersWithExecutor(innerMerge.mergeOrderLabel(), toMergeOrders);

        return cancelSLTP.concatWith(merge);
    }

    public Observable<OrderEvent> createCancelSLTP(final Collection<IOrder> toMergeOrders,
                                                   final MergeCommandWithParent command) {
        if (toMergeOrders.size() < 2)
            return Observable.empty();

        final MergeExecutionMode executionMode = command.executionMode();
        Observable<OrderEvent> obs;
        if (executionMode == MergeExecutionMode.ConcatSLAndTP)
            obs = cancelSL(toMergeOrders, command)
                .concatWith(cancelTP(toMergeOrders, command));
        else if (executionMode == MergeExecutionMode.ConcatTPAndSL)
            obs = cancelTP(toMergeOrders, command)
                .concatWith(cancelSL(toMergeOrders, command));
        else
            obs = cancelSL(toMergeOrders, command)
                .mergeWith(cancelTP(toMergeOrders, command));

        return obs.compose(command.cancelSLTPCompose());
    }

    public Observable<OrderEvent> createMerge(final Collection<IOrder> toMergeOrders,
                                              final MergeCommandWithParent command) {
        return mergeOrdersWithExecutor(command.mergeOrderLabel(), toMergeOrders)
            .compose(command.mergeCompose());
    }

    private Observable<OrderEvent> cancelSL(final Collection<IOrder> toMergeOrders,
                                            final MergeCommandWithParent command) {
        return batch(toMergeOrders, order -> setStopLossPrice(order, platformSettings.noSLPrice())
            .compose(command.cancelSLCompose(order)));
    }

    private Observable<OrderEvent> cancelTP(final Collection<IOrder> toMergeOrders,
                                            final MergeCommandWithParent command) {
        return batch(toMergeOrders, order -> setTakeProfitPrice(order, platformSettings.noTPPrice())
            .compose(command.cancelTPCompose(order)));
    }

    public final Observable<OrderEvent> batch(final Collection<IOrder> orders,
                                              final Function<IOrder, Observable<OrderEvent>> batchTask) {
        return Observable.defer(() -> Observable
            .fromIterable(orders)
            .flatMap(batchTask::apply));
    }

    public Observable<OrderEvent> close(final IOrder orderToClose) {
        return Observable
            .just(orderToClose)
            .filter(order -> !isClosed.test(order))
            .flatMap(order -> orderTaskExecutor
                .close(order)
                .andThen(orderUtilObservable(order, OrderCallReason.CLOSE)));
    }

    public Observable<OrderEvent> setLabel(final IOrder orderToSetLabel,
                                           final String label) {
        return Observable
            .just(orderToSetLabel)
            .filter(order -> !isLabelSetTo(label).test(order))
            .flatMap(order -> orderTaskExecutor
                .setLabel(order, label)
                .andThen(orderUtilObservable(order, OrderCallReason.CHANGE_LABEL)));
    }

    public Observable<OrderEvent> setGoodTillTime(final IOrder orderToSetGTT,
                                                  final long newGTT) {
        return Observable
            .just(orderToSetGTT)
            .filter(order -> !isGTTSetTo(newGTT).test(order))
            .flatMap(order -> orderTaskExecutor
                .setGoodTillTime(order, newGTT)
                .andThen(orderUtilObservable(order, OrderCallReason.CHANGE_GTT)));
    }

    public Observable<OrderEvent> setRequestedAmount(final IOrder orderToSetAmount,
                                                     final double newRequestedAmount) {
        return Observable
            .just(orderToSetAmount)
            .filter(order -> !isAmountSetTo(newRequestedAmount).test(order))
            .flatMap(order -> orderTaskExecutor
                .setRequestedAmount(order, newRequestedAmount)
                .andThen(orderUtilObservable(order, OrderCallReason.CHANGE_AMOUNT)));
    }

    public Observable<OrderEvent> setOpenPrice(final IOrder orderToSetOpenPrice,
                                               final double newOpenPrice) {
        return Observable
            .just(orderToSetOpenPrice)
            .filter(order -> !isOpenPriceSetTo(newOpenPrice).test(order))
            .flatMap(order -> orderTaskExecutor
                .setOpenPrice(order, newOpenPrice)
                .andThen(orderUtilObservable(order, OrderCallReason.CHANGE_PRICE)));
    }

    public Observable<OrderEvent> setStopLossPrice(final IOrder orderToSetSL,
                                                   final double newSL) {
        return Observable
            .just(orderToSetSL)
            .filter(order -> !isSLSetTo(newSL).test(order))
            .flatMap(order -> orderTaskExecutor
                .setStopLossPrice(order, newSL)
                .andThen(orderUtilObservable(order, OrderCallReason.CHANGE_SL)));
    }

    public Observable<OrderEvent> setTakeProfitPrice(final IOrder orderToSetTP,
                                                     final double newTP) {
        return Observable
            .just(orderToSetTP)
            .filter(order -> !isTPSetTo(newTP).test(order))
            .flatMap(order -> orderTaskExecutor
                .setTakeProfitPrice(order, newTP)
                .andThen(orderUtilObservable(order, OrderCallReason.CHANGE_TP)));
    }

    private Observable<OrderEvent> orderUtilObservable(final IOrder order,
                                                       final OrderCallReason orderCallReason) {
        return Observable.defer(() -> orderUtilHandler.callObservable(order, orderCallReason));
    }
}
