package com.jforex.programming.order.command;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
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

    public List<Completable> toCompletables(final List<? extends CommonCommand> commands) {
        return commands
            .stream()
            .map(orderUtilCompletable::commandToCompletable)
            .collect(Collectors.toList());
    }

    public Completable merge(final List<? extends CommonCommand> commands) {
        return Completable.merge(toCompletables(commands));
    }

    public <T extends CommonCommand> Completable mergeFromFactory(final Set<IOrder> orders,
                                                                  final Function<IOrder, T> commandFactory) {
        return merge(fromFactory(orders, commandFactory));
    }

    public Completable concatCommands(final List<? extends CommonCommand> commands) {
        return Completable.concat(toCompletables(commands));
    }

    @SafeVarargs
    public final <T extends CommonCommand> Completable concatCommands(final T... commands) {
        return concatCommands(Arrays.asList(commands));
    }

    public <T extends CommonCommand> List<T> fromFactory(final Set<IOrder> orders,
                                                         final Function<IOrder, T> commandFactory) {
        return orders
            .stream()
            .map(commandFactory::apply)
            .collect(Collectors.toList());
    }
}
