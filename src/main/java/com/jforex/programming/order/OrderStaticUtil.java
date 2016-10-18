package com.jforex.programming.order;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Collection;
import java.util.function.Function;
import java.util.function.Predicate;

import com.dukascopy.api.IEngine.OrderCommand;
import com.dukascopy.api.IOrder;
import com.dukascopy.api.Instrument;
import com.dukascopy.api.OfferSide;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.jforex.programming.math.CalculationUtil;
import com.jforex.programming.misc.JForexUtil;
import com.jforex.programming.settings.PlatformSettings;

public final class OrderStaticUtil {

    private OrderStaticUtil() {
    }

    private static final PlatformSettings platformSettings = JForexUtil.platformSettings;

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
        checkNotNull(instrument);

        return instrumentPredicate.apply(instrument);
    }

    public static final Function<String, Predicate<IOrder>> labelPredicate =
            label -> order -> order.getLabel().equals(label);

    public static final Function<Long, Predicate<IOrder>> gttPredicate =
            gtt -> order -> order.getGoodTillTime() == gtt;

    public static final Function<Double, Predicate<IOrder>> amountPredicate =
            amount -> order -> Double.compare(order.getRequestedAmount(), amount) == 0;

    public static final Function<Double, Predicate<IOrder>> openPricePredicate =
            openPrice -> order -> Double.compare(order.getOpenPrice(), openPrice) == 0;

    public static final Function<Double, Predicate<IOrder>> slPredicate =
            sl -> order -> Double.compare(order.getStopLossPrice(), sl) == 0;

    public static final Function<Double, Predicate<IOrder>> tpPredicate =
            tp -> order -> Double.compare(order.getTakeProfitPrice(), tp) == 0;

    public static final Predicate<IOrder> isLabelSetTo(final String label) {
        return labelPredicate.apply(checkNotNull(label));
    }

    public static final Predicate<IOrder> isGTTSetTo(final long gtt) {
        return gttPredicate.apply(gtt);
    }

    public static final Predicate<IOrder> isAmountSetTo(final double amount) {
        return amountPredicate.apply(amount);
    }

    public static final Predicate<IOrder> isOpenPriceSetTo(final double openPrice) {
        return openPricePredicate.apply(openPrice);
    }

    public static final Predicate<IOrder> isSLSetTo(final double sl) {
        return slPredicate.apply(sl);
    }

    public static final Predicate<IOrder> isTPSetTo(final double tp) {
        return tpPredicate.apply(tp);
    }

    public static final Predicate<IOrder> isNoSLSet = isSLSetTo(platformSettings.noSLPrice());
    public static final Predicate<IOrder> isNoTPSet = isTPSetTo(platformSettings.noTPPrice());

    public static final PositionDirection direction(final IOrder order) {
        checkNotNull(order);

        if (isFilled.test(order))
            return order.isLong()
                    ? PositionDirection.LONG
                    : PositionDirection.SHORT;
        return PositionDirection.FLAT;
    }

    public static final PositionDirection positionDirection(final Collection<IOrder> positionOrders) {
        checkNotNull(positionOrders);

        final double signedAmount = combinedSignedAmount(positionOrders);
        if (signedAmount > 0)
            return PositionDirection.LONG;
        return signedAmount < 0
                ? PositionDirection.SHORT
                : PositionDirection.FLAT;
    }

    public static final double signedAmount(final double amount,
                                            final OrderCommand orderCommand) {
        return buyOrderCommands.contains(orderCommand)
                ? amount
                : -amount;
    }

    public static final double signedAmount(final IOrder order) {
        checkNotNull(order);

        return signedAmount(order.getAmount(),
                            order.getOrderCommand());
    }

    public static final double signedAmount(final OrderParams orderParams) {
        checkNotNull(orderParams);

        return signedAmount(orderParams.amount(),
                            orderParams.orderCommand());
    }

    public static final double combinedSignedAmount(final Collection<IOrder> orders) {
        checkNotNull(orders);

        return orders.isEmpty()
                ? 0
                : orders
                    .stream()
                    .mapToDouble(OrderStaticUtil::signedAmount)
                    .sum();
    }

    public static final OfferSide offerSideForOrderCommand(final OrderCommand orderCommand) {
        return buyOrderCommands.contains(orderCommand)
                ? OfferSide.ASK
                : OfferSide.BID;
    }

    public static final OrderCommand directionToCommand(final PositionDirection positionDirection) {
        return positionDirection == PositionDirection.LONG
                ? OrderCommand.BUY
                : OrderCommand.SELL;
    }

    public static final OrderCommand switchCommand(final OrderCommand orderCommand) {
        return orderCommands.containsKey(orderCommand)
                ? orderCommands.get(orderCommand)
                : orderCommands.inverse().get(orderCommand);
    }

    public static final PositionDirection switchDirection(final PositionDirection positionDirection) {
        if (positionDirection == PositionDirection.FLAT)
            return positionDirection;
        return positionDirection == PositionDirection.LONG
                ? PositionDirection.SHORT
                : PositionDirection.LONG;
    }

    public static final PositionDirection directionForSignedAmount(final double signedAmount) {
        if (signedAmount > 0)
            return PositionDirection.LONG;
        return signedAmount < 0
                ? PositionDirection.SHORT
                : PositionDirection.FLAT;
    }

    public static final double slPriceWithPips(final IOrder order,
                                               final double price,
                                               final double pips) {
        checkNotNull(order);

        return CalculationUtil.addPipsToPrice(order.getInstrument(),
                                              price,
                                              order.isLong() ? -pips : pips);
    }

    public static final double tpPriceWithPips(final IOrder order,
                                               final double price,
                                               final double pips) {
        checkNotNull(order);

        return slPriceWithPips(order,
                               price,
                               -pips);
    }

    public static final OrderParams adaptedOrderParamsForSignedAmount(final OrderParams orderParams,
                                                                      final double signedAmount) {
        checkNotNull(orderParams);

        final PositionDirection direction = directionForSignedAmount(signedAmount);
        return orderParams
            .clone()
            .withOrderCommand(directionToCommand(direction))
            .withAmount(Math.abs(signedAmount))
            .build();
    }
}
