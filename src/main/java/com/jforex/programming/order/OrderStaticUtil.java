package com.jforex.programming.order;

import java.util.Collection;
import java.util.function.Function;
import java.util.function.Predicate;

import org.aeonbits.owner.ConfigFactory;

import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.jforex.programming.math.CalculationUtil;
import com.jforex.programming.settings.PlatformSettings;

import com.dukascopy.api.IEngine.OrderCommand;
import com.dukascopy.api.IOrder;
import com.dukascopy.api.Instrument;
import com.dukascopy.api.OfferSide;

public final class OrderStaticUtil {

    private static final PlatformSettings platformSettings = ConfigFactory.create(PlatformSettings.class);

    private OrderStaticUtil() {
    }

    public static final ImmutableBiMap<OrderCommand, OrderCommand> orderCommands =
            new ImmutableBiMap.Builder<OrderCommand, OrderCommand>()
                    .put(OrderCommand.BUY, OrderCommand.SELL)
                    .put(OrderCommand.BUYLIMIT, OrderCommand.SELLLIMIT)
                    .put(OrderCommand.BUYLIMIT_BYBID, OrderCommand.SELLLIMIT_BYASK)
                    .put(OrderCommand.BUYSTOP, OrderCommand.SELLSTOP)
                    .put(OrderCommand.BUYSTOP_BYBID, OrderCommand.SELLSTOP_BYASK)
                    .build();

    public static final ImmutableSet<OrderCommand> buyOrderCommands =
            Sets.immutableEnumSet(orderCommands.keySet());

    public static final ImmutableSet<OrderCommand> sellOrderCommands =
            Sets.immutableEnumSet(orderCommands.values());

    public static final Function<IOrder.State, Predicate<IOrder>> statePredicate =
            orderState -> order -> order.getState() == orderState;

    public static final Predicate<IOrder> isOpened = statePredicate.apply(IOrder.State.OPENED);
    public static final Predicate<IOrder> isFilled = statePredicate.apply(IOrder.State.FILLED);
    public static final Predicate<IOrder> isClosed = statePredicate.apply(IOrder.State.CLOSED);
    public static final Predicate<IOrder> isCanceled = statePredicate.apply(IOrder.State.CANCELED);
    public static final Predicate<IOrder> isConditional = order -> order.getOrderCommand().isConditional();

    public static final Function<Instrument, Predicate<IOrder>> instrumentPredicate =
            instrument -> order -> order.getInstrument() == instrument;

    public static final Predicate<IOrder> ofInstrument(final Instrument instrument) {
        return instrumentPredicate.apply(instrument);
    }

    public static final Function<Double, Predicate<IOrder>> orderSLPredicate =
            sl -> order -> order.getStopLossPrice() == sl;

    public static final Function<Double, Predicate<IOrder>> orderTPPredicate =
            tp -> order -> order.getTakeProfitPrice() == tp;

    public static final Predicate<IOrder> isSLSetTo(final Double sl) {
        return orderSLPredicate.apply(sl);
    }

    public static final Predicate<IOrder> isTPSetTo(final Double tp) {
        return orderTPPredicate.apply(tp);
    }

    public static final Predicate<IOrder> isNoSLSet = isSLSetTo(platformSettings.noSLPrice());
    public static final Predicate<IOrder> isNoTPSet = isTPSetTo(platformSettings.noTPPrice());

    public static final OrderDirection direction(final IOrder order) {
        if (order != null && isFilled.test(order))
            return order.isLong()
                    ? OrderDirection.LONG
                    : OrderDirection.SHORT;
        return OrderDirection.FLAT;
    }

    public static final OrderDirection combinedDirection(final Collection<IOrder> orders) {
        final double signedAmount = combinedSignedAmount(orders);
        if (signedAmount > 0)
            return OrderDirection.LONG;
        return signedAmount < 0
                ? OrderDirection.SHORT
                : OrderDirection.FLAT;
    }

    public static final double signedAmount(final double amount,
                                            final OrderCommand orderCommand) {
        return buyOrderCommands.contains(orderCommand)
                ? amount
                : -amount;
    }

    public static final double signedAmount(final IOrder order) {
        return signedAmount(order.getAmount(), order.getOrderCommand());
    }

    public static final double combinedSignedAmount(final Collection<IOrder> orders) {
        return orders
                .stream()
                .mapToDouble(OrderStaticUtil::signedAmount)
                .sum();
    }

    public static final OfferSide offerSideForOrderCommand(final OrderCommand orderCommand) {
        return buyOrderCommands.contains(orderCommand)
                ? OfferSide.ASK
                : OfferSide.BID;
    }

    public static final OrderCommand directionToCommand(final OrderDirection orderDirection) {
        return orderDirection == OrderDirection.LONG
                ? OrderCommand.BUY
                : OrderCommand.SELL;
    }

    public static final OrderCommand switchCommand(final OrderCommand orderCommand) {
        return orderCommands.containsKey(orderCommand)
                ? orderCommands.get(orderCommand)
                : orderCommands.inverse().get(orderCommand);
    }

    public static final OrderDirection switchDirection(final OrderDirection orderDirection) {
        if (orderDirection == OrderDirection.FLAT)
            return orderDirection;
        return orderDirection == OrderDirection.LONG
                ? OrderDirection.SHORT
                : OrderDirection.LONG;
    }

    public static final double slPriceWithPips(final IOrder order,
                                               final double price,
                                               final double pips) {
        return CalculationUtil.addPips(order.getInstrument(),
                                       price,
                                       order.isLong() ? -pips : pips);
    }

    public static final double tpPriceWithPips(final IOrder order,
                                               final double price,
                                               final double pips) {
        return slPriceWithPips(order, price, -pips);
    }
}
