package com.jforex.programming.position;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.dukascopy.api.IOrder;
import com.dukascopy.api.Instrument;
import com.jforex.programming.order.command.CloseCommand;
import com.jforex.programming.order.command.CommandUtil;
import com.jforex.programming.order.command.MergeCommand;

import io.reactivex.Completable;

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

    public Completable close(final Instrument instrument,
                             final Function<Collection<IOrder>, MergeCommand> mergeCommandFactory,
                             final Function<IOrder, CloseCommand> closeCommandFactory) {
        return Completable.defer(() -> {
            final Completable mergeCompletable = positionMerge.merge(instrument, mergeCommandFactory);
            final Completable closeCompletable = Completable.defer(() -> commandUtil
                .mergeFromFactory(position(instrument).filledOrOpened(), closeCommandFactory));

            // return Completable.merge(Lists.newArrayList(mergeCompletable,
            // closeCompletable));
            return mergeCompletable.andThen(closeCompletable);
        });
    }

    public Completable closeAll(final Function<Collection<IOrder>, MergeCommand> mergeCommandFactory,
                                final Function<IOrder, CloseCommand> closeCommandFactory) {
        return Completable.defer(() -> {
            final List<Completable> completables = positionFactory
                .all()
                .stream()
                .map(position -> close(position.instrument(),
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
