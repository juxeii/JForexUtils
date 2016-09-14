package com.jforex.programming.order.command;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.OrderUtilCompletable;
import com.jforex.programming.order.command.option.CommonOption;

import io.reactivex.Completable;

public class CommandUtil {

    private final OrderUtilCompletable orderUtilCompletable;

    public CommandUtil(final OrderUtilCompletable orderUtilCompletable) {
        this.orderUtilCompletable = orderUtilCompletable;
    }

    public Completable merge(final Collection<? extends Command> commands) {
        return Completable.merge(toCompletables(commands));
    }

    @SafeVarargs
    public final <T extends Command> Completable merge(final T... commands) {
        return merge(Arrays.asList(commands));
    }

    public <T extends CommonOption<T>> Completable mergeFromOption(final Collection<IOrder> orders,
                                                                   final Function<IOrder, T> option) {
        return merge(fromOption(orders, option));
    }

    public Completable concat(final List<? extends Command> commands) {
        return Completable.concat(toCompletables(commands));
    }

    @SafeVarargs
    public final <T extends Command> Completable concat(final T... commands) {
        return concat(Arrays.asList(commands));
    }

    public <T extends CommonOption<T>> Completable concatFromOption(final List<IOrder> orders,
                                                                    final Function<IOrder, T> option) {
        return concat(fromOption(orders, option));
    }

    public <T extends CommonOption<T>> List<Command> fromOption(final Collection<IOrder> orders,
                                                                final Function<IOrder, T> option) {
        return orders
            .stream()
            .map(order -> option.apply(order).build())
            .collect(Collectors.toList());
    }

    public List<Completable> toCompletables(final Collection<? extends Command> commands) {
        return commands
            .stream()
            .map(orderUtilCompletable::forCommand)
            .collect(Collectors.toList());
    }
}
