package com.jforex.programming.order;

import java.util.Collection;
import java.util.function.Function;
import java.util.function.Predicate;

import org.aeonbits.owner.ConfigFactory;

import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.jforex.programming.misc.CalculationUtil;
import com.jforex.programming.settings.PlatformSettings;

import com.dukascopy.api.IEngine.OrderCommand;
import com.dukascopy.api.IOrder;
import com.dukascopy.api.Instrument;
import com.dukascopy.api.OfferSide;

public final class OrderStaticUtil {

    private final static PlatformSettings platformSettings = ConfigFactory.create(PlatformSettings.class);

    private OrderStaticUtil() {
    }

    public final static ImmutableBiMap<OrderCommand, OrderCommand> orderCommands =
            new ImmutableBiMap.Builder<OrderCommand, OrderCommand>()
                    .put(OrderCommand.BUY,
                         OrderCommand.SELL)
                    .put(OrderCommand.BUYLIMIT,
                         OrderCommand.SELLLIMIT)
                    .put(OrderCommand.BUYLIMIT_BYBID,
                         OrderCommand.SELLLIMIT_BYASK)
                    .put(OrderCommand.BUYSTOP,
                         OrderCommand.SELLSTOP)
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
    public final static Predicate<IOrder> isCanceled = statePredicate.apply(IOrder.State.CANCELED);
    public final static Predicate<IOrder> isConditional = order -> order.getOrderCommand().isConditional();

    public final static Function<Instrument, Predicate<IOrder>> instrumentPredicate =
            instrument -> order -> order.getInstrument() == instrument;

    public final static Predicate<IOrder> ofInstrument(final Instrument instrument) {
        return instrumentPredicate.apply(instrument);
    }

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

    public final static Predicate<IOrder> isNoSLSet = isSLSetTo(platformSettings.noSLPrice());
    public final static Predicate<IOrder> isNoTPSet = isTPSetTo(platformSettings.noTPPrice());

    public final static OrderDirection direction(final IOrder order) {
        if (order != null && isFilled.test(order))
            return order.isLong()
                    ? OrderDirection.LONG
                    : OrderDirection.SHORT;
        return OrderDirection.FLAT;
    }

    public final static OrderDirection combinedDirection(final Collection<IOrder> orders) {
        final double signedAmount = combinedSignedAmount(orders);
        if (signedAmount > 0)
            return OrderDirection.LONG;
        return signedAmount < 0
                ? OrderDirection.SHORT
                : OrderDirection.FLAT;
    }

    public final static double signedAmount(final double amount,
                                            final OrderCommand orderCommand) {
        return buyOrderCommands.contains(orderCommand)
                ? amount
                : -amount;
    }

    public final static double signedAmount(final IOrder order) {
        return signedAmount(order.getAmount(), order.getOrderCommand());
    }

    public final static double combinedSignedAmount(final Collection<IOrder> orders) {
        return orders
                .stream()
                .mapToDouble(OrderStaticUtil::signedAmount)
                .sum();
    }

    public final static OfferSide offerSideForOrderCommand(final OrderCommand orderCommand) {
        return buyOrderCommands.contains(orderCommand)
                ? OfferSide.ASK
                : OfferSide.BID;
    }

    public final static OrderCommand directionToCommand(final OrderDirection orderDirection) {
        return orderDirection == OrderDirection.LONG
                ? OrderCommand.BUY
                : OrderCommand.SELL;
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

    public final static double slPriceWithPips(final IOrder order,
                                               final double price,
                                               final double pips) {
        return CalculationUtil.addPips(order.getInstrument(),
                                       price,
                                       order.isLong() ? -pips : pips);
    }

    public final static double tpPriceWithPips(final IOrder order,
                                               final double price,
                                               final double pips) {
        return CalculationUtil.addPips(order.getInstrument(),
                                       price,
                                       order.isLong() ? pips : -pips);
    }
}
