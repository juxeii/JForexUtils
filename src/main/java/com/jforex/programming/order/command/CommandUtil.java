package com.jforex.programming.order.command;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.OrderUtilCompletable;

import io.reactivex.Completable;

public class CommandUtil {

    private final OrderUtilCompletable orderUtilCompletable;

    public CommandUtil(final OrderUtilCompletable orderUtilCompletable) {
        this.orderUtilCompletable = orderUtilCompletable;
    }

    public Completable merge(final Collection<? extends CommonCommand> commands) {
        return Completable.merge(toCompletables(commands));
    }

    @SafeVarargs
    public final <T extends CommonCommand> Completable merge(final T... commands) {
        return merge(Arrays.asList(commands));
    }

    public <T extends CommonCommand> Completable mergeFromFactory(final Collection<IOrder> orders,
                                                                  final Function<IOrder, T> commandFactory) {
        return merge(fromFactory(orders, commandFactory));
    }

    public Completable concat(final List<? extends CommonCommand> commands) {
        return Completable.concat(toCompletables(commands));
    }

    @SafeVarargs
    public final <T extends CommonCommand> Completable concat(final T... commands) {
        return concat(Arrays.asList(commands));
    }

    public <T extends CommonCommand> Completable concatFromFactory(final List<IOrder> orders,
                                                                   final Function<IOrder, T> commandFactory) {
        return concat(fromFactory(orders, commandFactory));
    }

    public <T extends CommonCommand> List<T> fromFactory(final Collection<IOrder> orders,
                                                         final Function<IOrder, T> commandFactory) {
        return orders
            .stream()
            .map(commandFactory::apply)
            .collect(Collectors.toList());
    }

    public List<Completable> toCompletables(final Collection<? extends CommonCommand> commands) {
        return commands
            .stream()
            .map(orderUtilCompletable::commandToCompletable)
            .collect(Collectors.toList());
    }
}
