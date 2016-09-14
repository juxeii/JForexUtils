package com.jforex.programming.position;

import java.util.Collection;
import java.util.function.Function;

import com.dukascopy.api.IOrder;
import com.dukascopy.api.Instrument;
import com.jforex.programming.order.command.option.CloseOption;
import com.jforex.programming.order.command.option.MergeOption;

import io.reactivex.Completable;

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
                             final Function<Collection<IOrder>, MergeOption> mergeOption) {
        return positionMerge.merge(instrument, mergeOption);
    }

    public Completable mergeAll(final Function<Collection<IOrder>, MergeOption> mergeOption) {
        return positionMerge.mergeAll(mergeOption);
    }

    public Completable close(final Instrument instrument,
                             final Function<Collection<IOrder>, MergeOption> mergeOption,
                             final Function<IOrder, CloseOption> closeOption) {
        return positionClose.close(instrument,
                                   mergeOption,
                                   closeOption);
    }

    public Completable closeAll(final Function<Collection<IOrder>, MergeOption> mergeOption,
                                final Function<IOrder, CloseOption> closeOption) {
        return positionClose.closeAll(mergeOption, closeOption);
    }
}
