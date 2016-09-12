package com.jforex.programming.order;

import static com.jforex.programming.order.OrderStaticUtil.instrumentFromOrders;
import static com.jforex.programming.order.OrderStaticUtil.isAmountSetTo;
import static com.jforex.programming.order.OrderStaticUtil.isClosed;
import static com.jforex.programming.order.OrderStaticUtil.isGTTSetTo;
import static com.jforex.programming.order.OrderStaticUtil.isLabelSetTo;
import static com.jforex.programming.order.OrderStaticUtil.isOpenPriceSetTo;
import static com.jforex.programming.order.OrderStaticUtil.isSLSetTo;
import static com.jforex.programming.order.OrderStaticUtil.isTPSetTo;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import com.dukascopy.api.IOrder;
import com.dukascopy.api.Instrument;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.jforex.programming.order.call.OrderCallReason;
import com.jforex.programming.order.command.CloseCommand;
import com.jforex.programming.order.command.CommonCommand;
import com.jforex.programming.order.command.MergeCommand;
import com.jforex.programming.order.command.SetAmountCommand;
import com.jforex.programming.order.command.SetGTTCommand;
import com.jforex.programming.order.command.SetLabelCommand;
import com.jforex.programming.order.command.SetOpenPriceCommand;
import com.jforex.programming.order.command.SetSLCommand;
import com.jforex.programming.order.command.SetTPCommand;
import com.jforex.programming.order.command.SubmitCommand;
import com.jforex.programming.position.Position;
import com.jforex.programming.position.PositionFactory;

import rx.Completable;
import rx.Observable;

public class OrderUtilCompletable {

    private final OrderUtilHandler orderUtilHandler;
    private final PositionFactory positionFactory;

    private final Map<OrderCallReason, Function<CommonCommand, Completable>> completableByCallReason =
            Maps.immutableEnumMap(ImmutableMap.<OrderCallReason, Function<CommonCommand, Completable>> builder()
                .put(OrderCallReason.SUBMIT, cmd -> submitOrder((SubmitCommand) cmd))
                .put(OrderCallReason.MERGE, cmd -> mergeOrders((MergeCommand) cmd))
                .put(OrderCallReason.CLOSE, cmd -> close((CloseCommand) cmd))
                .put(OrderCallReason.CHANGE_LABEL, cmd -> setLabel((SetLabelCommand) cmd))
                .put(OrderCallReason.CHANGE_GTT, cmd -> setGTT((SetGTTCommand) cmd))
                .put(OrderCallReason.CHANGE_AMOUNT, cmd -> setAmount((SetAmountCommand) cmd))
                .put(OrderCallReason.CHANGE_PRICE, cmd -> setOpenPrice((SetOpenPriceCommand) cmd))
                .put(OrderCallReason.CHANGE_SL, cmd -> setSL((SetSLCommand) cmd))
                .put(OrderCallReason.CHANGE_TP, cmd -> setTP((SetTPCommand) cmd))
                .build());

    public OrderUtilCompletable(final OrderUtilHandler orderUtilHandler,
                                final PositionFactory positionFactory) {
        this.orderUtilHandler = orderUtilHandler;
        this.positionFactory = positionFactory;
    }

    public Completable submitOrder(final SubmitCommand command) {
        return Completable.defer(() -> {
            return orderUtilHandler
                .callObservable(command)
                .toCompletable();
        });
    }

    public Completable mergeOrders(final MergeCommand command) {
        return Completable.defer(() -> {
            final Set<IOrder> toMergeOrders = command.toMergeOrders();
            return toMergeOrders.size() < 2
                    ? Completable.complete()
                    : Observable
                        .just(toMergeOrders)
                        .doOnSubscribe(() -> positionOfOrders(toMergeOrders).markOrdersActive(toMergeOrders))
                        .flatMap(orders -> orderUtilHandler.callObservable(command))
                        .doOnTerminate(() -> positionOfOrders(toMergeOrders).markOrdersIdle(toMergeOrders))
                        .toCompletable();
        });
    }

    public Completable close(final CloseCommand command) {
        return Completable.defer(() -> {
            final IOrder orderToClose = command.order();
            final Position position = position(orderToClose.getInstrument());
            return Observable
                .just(orderToClose)
                .filter(order -> !isClosed.test(order))
                .doOnNext(order -> position.markOrderActive(orderToClose))
                .flatMap(order -> orderUtilHandler.callObservable(command))
                .doOnNext(orderEvent -> position.markOrderIdle(orderToClose))
                .toCompletable();
        });
    }

    public Completable setLabel(final SetLabelCommand command) {
        return Completable.defer(() -> {
            return Observable
                .just(command.order())
                .filter(order -> !isLabelSetTo(command.newLabel()).test(order))
                .flatMap(order -> orderUtilHandler.callObservable(command))
                .toCompletable();
        });
    }

    public Completable setGTT(final SetGTTCommand command) {
        return Completable.defer(() -> {
            return Observable
                .just(command.order())
                .filter(order -> !isGTTSetTo(command.newGTT()).test(order))
                .flatMap(order -> orderUtilHandler.callObservable(command))
                .toCompletable();
        });
    }

    public Completable setAmount(final SetAmountCommand command) {
        return Completable.defer(() -> {
            return Observable
                .just(command.order())
                .filter(order -> !isAmountSetTo(command.newAmount()).test(order))
                .flatMap(order -> orderUtilHandler.callObservable(command))
                .toCompletable();
        });
    }

    public Completable setOpenPrice(final SetOpenPriceCommand command) {
        return Completable.defer(() -> {
            return Observable
                .just(command.order())
                .filter(order -> !isOpenPriceSetTo(command.newOpenPrice()).test(order))
                .flatMap(order -> orderUtilHandler.callObservable(command))
                .toCompletable();
        });
    }

    public Completable setSL(final SetSLCommand command) {
        return Completable.defer(() -> {
            return Observable
                .just(command.order())
                .filter(order -> !isSLSetTo(command.newSL()).test(order))
                .flatMap(order -> orderUtilHandler.callObservable(command))
                .toCompletable();
        });
    }

    public Completable setTP(final SetTPCommand command) {
        return Completable.defer(() -> {
            return Observable
                .just(command.order())
                .filter(order -> !isTPSetTo(command.newTP()).test(order))
                .flatMap(order -> orderUtilHandler.callObservable(command))
                .toCompletable();
        });
    }

    public Completable commandCompletable(final CommonCommand command) {
        return completableByCallReason.get(command.callReason()).apply(command);
    }

    private Position position(final Instrument instrument) {
        return positionFactory.forInstrument(instrument);
    }

    private Position positionOfOrders(final Collection<IOrder> orders) {
        return position(instrumentFromOrders(orders));
    }
}
