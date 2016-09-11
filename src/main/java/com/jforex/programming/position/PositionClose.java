package com.jforex.programming.position;

import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.dukascopy.api.IOrder;
import com.dukascopy.api.Instrument;
import com.jforex.programming.order.command.CloseCommand;
import com.jforex.programming.order.command.CommandUtil;
import com.jforex.programming.order.command.MergeCommand;

import rx.Completable;

public class PositionClose {

    private final PositionMerge positionMerge;
    private final PositionFactory positionFactory;
    private final CommandUtil commandUtil;

    public PositionClose(final PositionMerge positionMerge,
                         final PositionFactory positionFactory,
                         final CommandUtil commandUtil) {
        this.positionMerge = positionMerge;
        this.positionFactory = positionFactory;
        this.commandUtil = commandUtil;
    }

    public Completable closePosition(final Instrument instrument,
                                     final Function<Set<IOrder>, MergeCommand> mergeCommandFactory,
                                     final Function<IOrder, CloseCommand> closeCommandFactory) {
        return Completable.defer(() -> {
            final Completable mergeCompletable = positionMerge.merge(instrument, mergeCommandFactory);
            final Completable closeCompletable = closePositionAfterMerge(instrument, closeCommandFactory);
            return Completable.concat(mergeCompletable, closeCompletable);
        });
    }

    private final Completable closePositionAfterMerge(final Instrument instrument,
                                                      final Function<IOrder, CloseCommand> closeCommandFactory) {
        return Completable.defer(() -> {
            final Set<IOrder> ordersToClose = position(instrument).filledOrOpened();
            final List<CloseCommand> closeCommands =
                    commandUtil.batchCommands(ordersToClose, closeCommandFactory);
            return commandUtil.runCommands(closeCommands);
        });
    }

    public Completable closeAllPositions(final Function<Set<IOrder>, MergeCommand> mergeCommandFactory,
                                         final Function<IOrder, CloseCommand> closeCommandFactory) {
        return Completable.defer(() -> {
            final List<Completable> completables = positionFactory
                .all()
                .stream()
                .map(position -> closePosition(position.instrument(),
                                               mergeCommandFactory,
                                               closeCommandFactory))
                .collect(Collectors.toList());
            return Completable.merge(completables);
        });
    }

    private final Position position(final Instrument instrument) {
        return positionFactory.forInstrument(instrument);
    }
}
