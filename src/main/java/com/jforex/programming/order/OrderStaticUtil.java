package com.jforex.programming.order;

import static com.jforex.programming.misc.JForexUtil.pfs;

import java.util.Collection;
import java.util.function.Function;
import java.util.function.Predicate;

import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.jforex.programming.order.call.OrderCallResult;

import com.dukascopy.api.IEngine.OrderCommand;
import com.dukascopy.api.IOrder;
import com.dukascopy.api.OfferSide;

import rx.Observable;

public final class OrderStaticUtil {

    private OrderStaticUtil() {
    }

    // @formatter:off
    public final static ImmutableBiMap<OrderCommand, OrderCommand> orderCommands =
            new ImmutableBiMap.Builder<OrderCommand, OrderCommand>()
                                                                    .put(OrderCommand.BUY, OrderCommand.SELL)
                                                                    .put(OrderCommand.BUYLIMIT, OrderCommand.SELLLIMIT)
                                                                    .put(OrderCommand.BUYLIMIT_BYBID,
                                                                         OrderCommand.SELLLIMIT_BYASK)
                                                                    .put(OrderCommand.BUYSTOP, OrderCommand.SELLSTOP)
                                                                    .put(OrderCommand.BUYSTOP_BYBID,
                                                                         OrderCommand.SELLSTOP_BYASK)
                                                                    .build();

    public final static ImmutableSet<OrderCommand> buyOrderCommands =
            Sets.immutableEnumSet(orderCommands.keySet());

    public final static ImmutableSet<OrderCommand> sellOrderCommands =
            Sets.immutableEnumSet(orderCommands.values());

    public final static Function<IOrder.State, Predicate<IOrder>> statePredicate =
            orderState -> order -> order.getState() == orderState;

    public final static Predicate<IOrder> isOpened = statePredicate.apply(IOrder.State.OPENED);
    public final static Predicate<IOrder> isFilled = statePredicate.apply(IOrder.State.FILLED);
    public final static Predicate<IOrder> isClosed = statePredicate.apply(IOrder.State.CLOSED);
    public final static Predicate<IOrder> isConditional = order -> order.getOrderCommand().isConditional();

    public final static Function<Double, Predicate<IOrder>> orderSLPredicate =
            sl -> order -> order.getStopLossPrice() == sl;

    public final static Function<Double, Predicate<IOrder>> orderTPPredicate =
            tp -> order -> order.getTakeProfitPrice() == tp;

    public final static Predicate<IOrder> isSLSetTo(final Double sl) {
        return orderSLPredicate.apply(sl);
    }

    public final static Predicate<IOrder> isTPSetTo(final Double tp) {
        return orderTPPredicate.apply(tp);
    }

    public final static Predicate<IOrder> isNoSLSet = isSLSetTo(pfs.NO_STOP_LOSS_PRICE());
    public final static Predicate<IOrder> isNoTPSet = isTPSetTo(pfs.NO_TAKE_PROFIT_PRICE());

    public final static OrderDirection direction(final IOrder order) {
        if (order != null && isFilled.test(order))
            return order.isLong() ? OrderDirection.LONG : OrderDirection.SHORT;
        return OrderDirection.FLAT;
    }

    public final static OrderDirection combinedDirection(final Collection<IOrder> orders) {
        final double signedAmount = combinedSignedAmount(orders);
        if (signedAmount > 0)
            return OrderDirection.LONG;
        return signedAmount < 0 ? OrderDirection.SHORT : OrderDirection.FLAT;
    }

    public final static double signedAmount(final double amount,
                                            final OrderCommand orderCommand) {
        return buyOrderCommands.contains(orderCommand) ? amount : -amount;
    }

    public final static double signedAmount(final IOrder order) {
        return signedAmount(order.getAmount(), order.getOrderCommand());
    }

    public final static double combinedSignedAmount(final Collection<IOrder> orders) {
        return orders.stream()
                     .mapToDouble(OrderStaticUtil::signedAmount)
                     .sum();
    }

    public final static OfferSide offerSideForOrderCommand(final OrderCommand orderCommand) {
        return buyOrderCommands.contains(orderCommand) ? OfferSide.ASK : OfferSide.BID;
    }

    public final static OrderCommand directionToCommand(final OrderDirection orderDirection) {
        return orderDirection == OrderDirection.LONG ? OrderCommand.BUY : OrderCommand.SELL;
    }

    public final static OrderCommand switchCommand(final OrderCommand orderCommand) {
        return orderCommands.containsKey(orderCommand)
                ? orderCommands.get(orderCommand)
                : orderCommands.inverse().get(orderCommand);
    }

    public final static OrderDirection switchDirection(final OrderDirection orderDirection) {
        if (orderDirection == OrderDirection.FLAT)
            return orderDirection;
        return orderDirection == OrderDirection.LONG
                ? OrderDirection.SHORT
                : OrderDirection.LONG;
    }

    public final static Observable<IOrder>
           orderCallObservable(final Supplier<OrderCallResult> orderCallResultSupplier) {
        return Observable.create(subscriber -> {
            final OrderCallResult orderCallResult = orderCallResultSupplier.get();
            if (orderCallResult.exceptionOpt().isPresent())
                subscriber.onError(orderCallResult.exceptionOpt().get());
            else {
                if (orderCallResult instanceof OrderCreateResult)
                    subscriber.onNext(((OrderCreateResult) orderCallResult).orderOpt().get());
                else
                    subscriber.onNext(((OrderChangeResult) orderCallResult).order());
                subscriber.onCompleted();
            }
        });
    }
}
