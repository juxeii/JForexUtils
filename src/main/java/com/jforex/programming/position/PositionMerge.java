package com.jforex.programming.position;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.dukascopy.api.IOrder;
import com.dukascopy.api.Instrument;
import com.google.common.collect.Lists;
import com.jforex.programming.order.OrderUtilCompletable;
import com.jforex.programming.order.command.MergeCommand;

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
                             final Function<List<IOrder>, MergeCommand> mergeCommandFactory) {
        return Completable.defer(() -> {
            final List<IOrder> toMergeOrders = Lists.newArrayList(positionFactory.forInstrument(instrument).filled());
            final MergeCommand command = mergeCommandFactory.apply(toMergeOrders);
            return orderUtilCompletable.mergeOrders(command);
        });
    }

    public Completable mergeAll(final Function<List<IOrder>, MergeCommand> mergeCommandFactory) {
        return Completable.defer(() -> {
            final List<Completable> completables = positionFactory
                .all()
                .stream()
                .map(position -> merge(position.instrument(), mergeCommandFactory))
                .collect(Collectors.toList());
            return Completable.merge(completables);
        });
    }
}
