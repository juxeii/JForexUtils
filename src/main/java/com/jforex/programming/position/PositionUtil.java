package com.jforex.programming.position;

import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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

    private static final Logger logger = LogManager.getLogger(PositionUtil.class);

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
                    : orderUtilCompletable.mergeOrders(mergeCommandFactory.apply(toMergeOrders))
                        .doOnSubscribe(s -> logger.info("Start to merge position for " + instrument + "."))
                        .doOnError(e -> logger.error("Failed to merge position for " + instrument
                                + "!Excpetion: " + e.getMessage()))
                        .doOnCompleted(() -> logger.info("Merged position for " + instrument + "."));
        });
    }

    public Completable mergeAllPositions(final Function<Set<IOrder>, MergeCommand> mergeCommandFactory) {
        return Completable.defer(() -> {
            final List<Completable> completables = positionFactory
                .allPositions()
                .stream()
                .map(position -> mergePosition(position.instrument(), mergeCommandFactory))
                .collect(Collectors.toList());

            return Completable.merge(completables);
        });
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
            final List<Completable> completables = positionFactory
                .allPositions()
                .stream()
                .map(position -> closePosition(position.instrument(), mergeCommandFactory, closeCommandFactory))
                .collect(Collectors.toList());

            return Completable.merge(completables);
        });
    }

    public PositionOrders positionOrders(final Instrument instrument) {
        return positionFactory.forInstrument(instrument);
    }

    private final Position position(final Instrument instrument) {
        return positionFactory.forInstrument(instrument);
    }
}
