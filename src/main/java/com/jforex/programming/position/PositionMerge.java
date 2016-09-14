package com.jforex.programming.position;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.dukascopy.api.IOrder;
import com.dukascopy.api.Instrument;
import com.jforex.programming.order.OrderUtilCompletable;
import com.jforex.programming.order.command.option.MergeOption;

import io.reactivex.Completable;

public class PositionMerge {

    private final OrderUtilCompletable orderUtilCompletable;
    private final PositionFactory positionFactory;

    public PositionMerge(final OrderUtilCompletable orderUtilCompletable,
                         final PositionFactory positionFactory) {
        this.orderUtilCompletable = orderUtilCompletable;
        this.positionFactory = positionFactory;
    }

    public Completable merge(final Instrument instrument,
                             final Function<Collection<IOrder>, MergeOption> mergeOption) {
        return Completable.defer(() -> {
            final Position position = positionFactory.forInstrument(instrument);
            final Set<IOrder> toMergeOrders = position.filled();
            return toMergeOrders.size() < 2
                    ? Completable.complete()
                    : orderUtilCompletable.forCommandWithOrderMarking(mergeOption.apply(toMergeOrders).build(),
                                                                      toMergeOrders);
        });
    }

    public Completable mergeAll(final Function<Collection<IOrder>, MergeOption> mergeOption) {
        return Completable.defer(() -> {
            final List<Completable> completables = positionFactory
                .all()
                .stream()
                .map(position -> merge(position.instrument(), mergeOption))
                .collect(Collectors.toList());
            return Completable.merge(completables);
        });
    }
}
