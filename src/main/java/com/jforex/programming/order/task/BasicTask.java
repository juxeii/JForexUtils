package com.jforex.programming.order.task;

import static com.jforex.programming.order.OrderStaticUtil.isAmountSetTo;
import static com.jforex.programming.order.OrderStaticUtil.isGTTSetTo;
import static com.jforex.programming.order.OrderStaticUtil.isLabelSetTo;
import static com.jforex.programming.order.OrderStaticUtil.isOpenPriceSetTo;
import static com.jforex.programming.order.OrderStaticUtil.isSLSetTo;
import static com.jforex.programming.order.OrderStaticUtil.isTPSetTo;

import java.util.Collection;

import com.dukascopy.api.IOrder;
import com.dukascopy.api.OfferSide;
import com.jforex.programming.order.OrderParams;
import com.jforex.programming.order.OrderStaticUtil;
import com.jforex.programming.order.OrderUtilHandler;
import com.jforex.programming.order.call.OrderCallReason;
import com.jforex.programming.order.command.CloseCommand;
import com.jforex.programming.order.command.SetSLCommand;
import com.jforex.programming.order.event.OrderEvent;

import io.reactivex.Completable;
import io.reactivex.Observable;

public class BasicTask {

    private final TaskExecutor taskExecutor;
    private final OrderUtilHandler orderUtilHandler;

    public BasicTask(final TaskExecutor orderTaskExecutor,
                     final OrderUtilHandler orderUtilHandler) {
        this.taskExecutor = orderTaskExecutor;
        this.orderUtilHandler = orderUtilHandler;
    }

    public Observable<OrderEvent> submitOrder(final OrderParams orderParams) {
        final OrderCallReason callReason = orderParams.orderCommand().isConditional()
                ? OrderCallReason.SUBMIT_CONDITIONAL
                : OrderCallReason.SUBMIT;

        return Observable.defer(() -> taskExecutor
            .submitOrder(orderParams)
            .toObservable()
            .flatMap(order -> orderUtilObservable(order, callReason)));
    }

    public Observable<OrderEvent> mergeOrders(final String mergeOrderLabel,
                                              final Collection<IOrder> toMergeOrders) {
        return Observable
            .just(toMergeOrders)
            .filter(orders -> orders.size() >= 2)
            .flatMap(orders -> taskExecutor
                .mergeOrders(mergeOrderLabel, orders)
                .toObservable()
                .flatMap(order -> orderUtilObservable(order, OrderCallReason.MERGE)));
    }

    public Observable<OrderEvent> close(final CloseCommand closeCommand) {

        return Observable
            .just(closeCommand.order())
            .filter(order -> !OrderStaticUtil.isClosed.test(order))
            .flatMap(order -> evalCloseParmas(order, closeCommand)
                .andThen(orderUtilObservable(order, OrderCallReason.CLOSE)));
    }

    private Completable evalCloseParmas(final IOrder orderToClose,
                                        final CloseCommand closeCommand) {
        if (closeCommand.price() == 0.0)
            return taskExecutor
                .close(orderToClose,
                       closeCommand.amount());
        else
            return taskExecutor
                .close(orderToClose,
                       closeCommand.amount(),
                       closeCommand.price(),
                       closeCommand.slippage());
    }

    public Observable<OrderEvent> setLabel(final IOrder orderToSetLabel,
                                           final String label) {
        return Observable
            .just(orderToSetLabel)
            .filter(order -> !isLabelSetTo(label).test(order))
            .flatMap(order -> taskExecutor
                .setLabel(order, label)
                .andThen(orderUtilObservable(order, OrderCallReason.CHANGE_LABEL)));
    }

    public Observable<OrderEvent> setGoodTillTime(final IOrder orderToSetGTT,
                                                  final long newGTT) {
        return Observable
            .just(orderToSetGTT)
            .filter(order -> !isGTTSetTo(newGTT).test(order))
            .flatMap(order -> taskExecutor
                .setGoodTillTime(order, newGTT)
                .andThen(orderUtilObservable(order, OrderCallReason.CHANGE_GTT)));
    }

    public Observable<OrderEvent> setRequestedAmount(final IOrder orderToSetAmount,
                                                     final double newRequestedAmount) {
        return Observable
            .just(orderToSetAmount)
            .filter(order -> !isAmountSetTo(newRequestedAmount).test(order))
            .flatMap(order -> taskExecutor
                .setRequestedAmount(order, newRequestedAmount)
                .andThen(orderUtilObservable(order, OrderCallReason.CHANGE_AMOUNT)));
    }

    public Observable<OrderEvent> setOpenPrice(final IOrder orderToSetOpenPrice,
                                               final double newOpenPrice) {
        return Observable
            .just(orderToSetOpenPrice)
            .filter(order -> !isOpenPriceSetTo(newOpenPrice).test(order))
            .flatMap(order -> taskExecutor
                .setOpenPrice(order, newOpenPrice)
                .andThen(orderUtilObservable(order, OrderCallReason.CHANGE_PRICE)));
    }

    public Observable<OrderEvent> setStopLossPrice(final SetSLCommand setSLCommand) {
        return Observable
            .just(setSLCommand.order())
            .filter(order -> !isSLSetTo(setSLCommand.newSL()).test(order))
            .flatMap(order -> taskExecutor
                .setStopLossPrice(order,
                                  setSLCommand.newSL(),
                                  offerSideForSLCommand(order, setSLCommand),
                                  setSLCommand.trailingStep())
                .andThen(orderUtilObservable(order, OrderCallReason.CHANGE_SL)));
    }

    private OfferSide offerSideForSLCommand(final IOrder orderToSetSL,
                                            final SetSLCommand setSLCommand) {
        return setSLCommand
            .maybeOfferSide()
            .orElse(orderToSetSL.isLong()
                    ? OfferSide.BID
                    : OfferSide.ASK);
    }

    public Observable<OrderEvent> setTakeProfitPrice(final IOrder orderToSetTP,
                                                     final double newTP) {
        return Observable
            .just(orderToSetTP)
            .filter(order -> !isTPSetTo(newTP).test(order))
            .flatMap(order -> taskExecutor
                .setTakeProfitPrice(order, newTP)
                .andThen(orderUtilObservable(order, OrderCallReason.CHANGE_TP)));
    }

    private final Observable<OrderEvent> orderUtilObservable(final IOrder order,
                                                             final OrderCallReason orderCallReason) {
        return orderUtilHandler.callObservable(order, orderCallReason);
    }
}
