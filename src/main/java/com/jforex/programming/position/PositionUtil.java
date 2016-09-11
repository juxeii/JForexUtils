package com.jforex.programming.position;

import java.util.Set;
import java.util.function.Function;

import com.dukascopy.api.IOrder;
import com.dukascopy.api.Instrument;
import com.jforex.programming.order.command.CloseCommand;
import com.jforex.programming.order.command.MergeCommand;

import rx.Completable;

public class PositionUtil {

    private final PositionMerge positionMerge;
    private final PositionClose positionClose;
    private final PositionFactory positionFactory;

    public PositionUtil(final PositionMerge positionMerge,
                        final PositionClose positionClose,
                        final PositionFactory positionFactory) {
        this.positionMerge = positionMerge;
        this.positionClose = positionClose;
        this.positionFactory = positionFactory;
    }

    public PositionOrders positionOrders(final Instrument instrument) {
        return positionFactory.forInstrument(instrument);
    }

    public Completable merge(final Instrument instrument,
                             final Function<Set<IOrder>, MergeCommand> mergeCommandFactory) {
        return positionMerge.merge(instrument, mergeCommandFactory);
    }

    public Completable mergeAll(final Function<Set<IOrder>, MergeCommand> mergeCommandFactory) {
        return positionMerge.mergeAll(mergeCommandFactory);
    }

    public Completable close(final Instrument instrument,
                             final Function<Set<IOrder>, MergeCommand> mergeCommandFactory,
                             final Function<IOrder, CloseCommand> closeCommandFactory) {
        return positionClose.close(instrument,
                                   mergeCommandFactory,
                                   closeCommandFactory);
    }

    public Completable closeAll(final Function<Set<IOrder>, MergeCommand> mergeCommandFactory,
                                final Function<IOrder, CloseCommand> closeCommandFactory) {
        return positionClose.closeAll(mergeCommandFactory, closeCommandFactory);
    }
}
