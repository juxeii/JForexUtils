package com.jforex.programming.order.command;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.dukascopy.api.IOrder;

import rx.Completable;

public class CommandUtil {

    public List<Completable> commandsToCompletables(final List<? extends CommonCommand> commands) {
        return commands
            .stream()
            .map(CommonCommand::completable)
            .collect(Collectors.toList());
    }

    public <T extends CommonCommand> Completable runCommandsOfFactory(final Set<IOrder> orders,
                                                                      final Function<IOrder, T> commandFactory) {
        return runCommands(batchCommands(orders, commandFactory));
    }

    public Completable runCommands(final List<? extends CommonCommand> commands) {
        return Completable.merge(commandsToCompletables(commands));
    }

    public Completable runCommandsConcatenated(final List<? extends CommonCommand> commands) {
        return Completable.concat(commandsToCompletables(commands));
    }

    @SuppressWarnings("unchecked")
    public <T extends CommonCommand> Completable runCommandsConcatenated(final T... commands) {
        return runCommandsConcatenated(Arrays.asList(commands));
    }

    public <T extends CommonCommand> List<T> batchCommands(final Set<IOrder> orders,
                                                           final Function<IOrder, T> commandFactory) {
        return orders
            .stream()
            .map(commandFactory::apply)
            .collect(Collectors.toList());
    }
}
