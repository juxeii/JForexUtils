package com.jforex.programming.order;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.function.Function;
import java.util.function.Predicate;

import org.aeonbits.owner.ConfigFactory;

import com.dukascopy.api.IEngine.OrderCommand;
import com.dukascopy.api.IOrder;
import com.dukascopy.api.Instrument;
import com.dukascopy.api.OfferSide;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.jforex.programming.math.CalculationUtil;
import com.jforex.programming.misc.JFRunnable;
import com.jforex.programming.settings.PlatformSettings;

public final class OrderStaticUtil {

    private OrderStaticUtil() {
    }

    private static final PlatformSettings platformSettings = ConfigFactory.create(PlatformSettings.class);

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
        return instrumentPredicate.apply(checkNotNull(instrument));
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

    public static final OrderDirection direction(final IOrder order) {
        if (order != null && isFilled.test(order))
            return order.isLong()
                    ? OrderDirection.LONG
                    : OrderDirection.SHORT;
        return OrderDirection.FLAT;
    }

    public static final OrderDirection combinedDirection(final Collection<IOrder> orders) {
        final double signedAmount = combinedSignedAmount(checkNotNull(orders));
        if (signedAmount > 0)
            return OrderDirection.LONG;
        return signedAmount < 0
                ? OrderDirection.SHORT
                : OrderDirection.FLAT;
    }

    public static final double signedAmount(final double amount,
                                            final OrderCommand orderCommand) {
        return buyOrderCommands.contains(checkNotNull(orderCommand))
                ? amount
                : -amount;
    }

    public static final double signedAmount(final IOrder order) {
        return signedAmount(checkNotNull(order).getAmount(), order.getOrderCommand());
    }

    public static final double combinedSignedAmount(final Collection<IOrder> orders) {
        return checkNotNull(orders)
                .stream()
                .mapToDouble(OrderStaticUtil::signedAmount)
                .sum();
    }

    public static final OfferSide offerSideForOrderCommand(final OrderCommand orderCommand) {
        return buyOrderCommands.contains(checkNotNull(orderCommand))
                ? OfferSide.ASK
                : OfferSide.BID;
    }

    public static final OrderCommand directionToCommand(final OrderDirection orderDirection) {
        return checkNotNull(orderDirection) == OrderDirection.LONG
                ? OrderCommand.BUY
                : OrderCommand.SELL;
    }

    public static final OrderCommand switchCommand(final OrderCommand orderCommand) {
        return orderCommands.containsKey(checkNotNull(orderCommand))
                ? orderCommands.get(orderCommand)
                : orderCommands.inverse().get(orderCommand);
    }

    public static final OrderDirection switchDirection(final OrderDirection orderDirection) {
        if (checkNotNull(orderDirection) == OrderDirection.FLAT)
            return orderDirection;
        return orderDirection == OrderDirection.LONG
                ? OrderDirection.SHORT
                : OrderDirection.LONG;
    }

    public static final double slPriceWithPips(final IOrder order,
                                               final double price,
                                               final double pips) {
        return CalculationUtil.addPips(checkNotNull(order).getInstrument(),
                                       price,
                                       order.isLong() ? -pips : pips);
    }

    public static final double tpPriceWithPips(final IOrder order,
                                               final double price,
                                               final double pips) {
        return slPriceWithPips(checkNotNull(order), price, -pips);
    }

    public static Callable<IOrder> runnableToCallable(final JFRunnable runnable,
                                                      final IOrder order) {
        return () -> {
            runnable.run();
            return order;
        };
    }
}
