package com.jforex.programming.position;

import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.dukascopy.api.IOrder;
import com.dukascopy.api.Instrument;
import com.jforex.programming.order.OrderUtilCompletable;
import com.jforex.programming.order.command.CloseCommand;
import com.jforex.programming.order.command.CommandUtil;
import com.jforex.programming.order.command.MergeCommand;

import rx.Completable;

public class PositionUtil {

    private final OrderUtilCompletable orderUtilCompletable;
    private final PositionFactory positionFactory;

    public PositionUtil(final OrderUtilCompletable orderUtilCompletable,
                        final PositionFactory positionFactory) {
        this.orderUtilCompletable = orderUtilCompletable;
        this.positionFactory = positionFactory;
    }

    public Completable mergePosition(final Instrument instrument,
                                     final Function<Set<IOrder>, MergeCommand> mergeCommandFactory) {
        return Completable.defer(() -> {
            final Set<IOrder> toMergeOrders = position(instrument).filled();
            return toMergeOrders.size() < 2
                    ? Completable.complete()
                    : orderUtilCompletable.mergeOrders(mergeCommandFactory.apply(toMergeOrders));
        });
    }

    public Completable mergeAllPositions(final Function<Set<IOrder>, MergeCommand> mergeCommandFactory) {
        return Completable.defer(() -> {
            final Function<Position, Completable> mapper =
                    position -> mergePosition(position.instrument(), mergeCommandFactory);
            return Completable.merge(completablesForAllPositions(mapper));
        });
    }

    private List<Completable> completablesForAllPositions(final Function<Position, Completable> mapper) {
        return positionFactory
            .allPositions()
            .stream()
            .map(mapper::apply)
            .collect(Collectors.toList());
    }

    public Completable closePosition(final Instrument instrument,
                                     final Function<Set<IOrder>, MergeCommand> mergeCommandFactory,
                                     final Function<IOrder, CloseCommand> closeCommandFactory) {
        return Completable.defer(() -> {
            final Completable mergeCompletable = mergePosition(instrument, mergeCommandFactory);
            final Completable closeCompletable = closePositionAfterMerge(instrument, closeCommandFactory);
            return Completable.concat(mergeCompletable, closeCompletable);
        });
    }

    private final Completable closePositionAfterMerge(final Instrument instrument,
                                                      final Function<IOrder, CloseCommand> closeCommandFactory) {
        return Completable.defer(() -> {
            final Set<IOrder> ordersToClose = position(instrument).filledOrOpened();
            final List<CloseCommand> closeCommands =
                    CommandUtil.batchCommands(ordersToClose, closeCommandFactory);
            return CommandUtil.runCommands(closeCommands);
        });
    }

    public Completable closeAllPositions(final Function<Set<IOrder>, MergeCommand> mergeCommandFactory,
                                         final Function<IOrder, CloseCommand> closeCommandFactory) {
        return Completable.defer(() -> {
            final Function<Position, Completable> mapper =
                    position -> closePosition(position.instrument(),
                                              mergeCommandFactory,
                                              closeCommandFactory);
            return Completable.merge(completablesForAllPositions(mapper));
        });
    }

    public PositionOrders positionOrders(final Instrument instrument) {
        return positionFactory.forInstrument(instrument);
    }

    private final Position position(final Instrument instrument) {
        return positionFactory.forInstrument(instrument);
    }
}
