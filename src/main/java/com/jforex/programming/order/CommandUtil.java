package com.jforex.programming.order;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.command.OrderUtilCommand;

import rx.Completable;

public final class CommandUtil {

    public static final Completable runCommands(final List<? extends OrderUtilCommand> commands) {
        return Completable.merge(commandsToCompletables(commands));
    }

    public static final Completable runBatchCommands(final Set<IOrder> orders,
                                                     final Function<IOrder, ? extends OrderUtilCommand> commandCreator) {
        return runCommands(createBatchCommands(orders, commandCreator));
    }

    public static final List<Completable> commandsToCompletables(final List<? extends OrderUtilCommand> commands) {
        return commands
            .stream()
            .map(OrderUtilCommand::completable)
            .collect(Collectors.toList());
    }

    public static final <T extends OrderUtilCommand> List<T> createBatchCommands(final Set<IOrder> orders,
                                                                                 final Function<IOrder, T> commandCreator) {
        return orders
            .stream()
            .map(order -> commandCreator.apply(order))
            .collect(Collectors.toList());
    }

    public static final Completable commandSequence(final List<? extends OrderUtilCommand> commands) {
        return Completable.concat(commandsToCompletables(commands));
    }

    @SafeVarargs
    public static final <T extends OrderUtilCommand> Completable commandSequence(final T... commands) {
        return commandSequence(Arrays.asList(commands));
    }
}
