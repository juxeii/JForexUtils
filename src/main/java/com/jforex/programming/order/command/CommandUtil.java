package com.jforex.programming.order.command;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.dukascopy.api.IOrder;

import rx.Completable;

public final class CommandUtil {

    private CommandUtil() {
    }

    public static final List<Completable> commandsToCompletables(final List<? extends CommonCommand> commands) {
        return commands
            .stream()
            .map(CommonCommand::completable)
            .collect(Collectors.toList());
    }

    public static final Completable runCommands(final List<? extends CommonCommand> commands) {
        return Completable.merge(commandsToCompletables(commands));
    }

    public static final Completable runCommandsConcatenated(final List<? extends CommonCommand> commands) {
        return Completable.concat(commandsToCompletables(commands));
    }

    @SafeVarargs
    public static final <T extends CommonCommand> Completable runCommandsConcatenated(final T... commands) {
        return runCommandsConcatenated(Arrays.asList(commands));
    }

    public static final <T extends CommonCommand> List<T>
           createBatchCommands(final Set<IOrder> orders,
                               final Function<IOrder, T> commandFactory) {
        return orders
            .stream()
            .map(commandFactory::apply)
            .collect(Collectors.toList());
    }
}
