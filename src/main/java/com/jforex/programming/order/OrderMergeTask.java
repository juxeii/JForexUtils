package com.jforex.programming.order;

import java.util.Collection;

import com.dukascopy.api.IOrder;
import com.dukascopy.api.Instrument;
import com.jforex.programming.order.command.MergeCommand;
import com.jforex.programming.order.command.MergeCommandHandler;
import com.jforex.programming.order.command.MergeOrdersCommand;
import com.jforex.programming.order.command.MergePositionCommand;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.position.PositionUtil;

import io.reactivex.Observable;
import io.reactivex.functions.Function;

public class OrderMergeTask {

    private final MergeCommandHandler commandHandler;
    private final PositionUtil positionUtil;

    public OrderMergeTask(final MergeCommandHandler commandHandler,
                          final PositionUtil positionUtil) {
        this.commandHandler = commandHandler;
        this.positionUtil = positionUtil;
    }

    public Observable<OrderEvent> merge(final MergeOrdersCommand command) {
        return Observable.defer(() -> splitCancelSLTPAndMerge(command.toMergeOrders(),
                                                              command.mergeCommand()));
    }

    public Observable<OrderEvent> mergePosition(final MergePositionCommand command) {
        return Observable.defer(() -> splitCancelSLTPAndMerge(positionUtil.filledOrders(command.instrument()),
                                                              command.mergeCommand()));
    }

    public Observable<OrderEvent> merge(final MergeCommand command,
                                        final Collection<IOrder> toMergeOrders) {
        return Observable.defer(() -> splitCancelSLTPAndMerge(toMergeOrders, command));
    }

    public Observable<OrderEvent> splitCancelSLTPAndMerge(final Collection<IOrder> toMergeOrders,
                                                          final MergeCommand command) {
        final Observable<OrderEvent> cancelSLTP = commandHandler.observeCancelSLTP(toMergeOrders, command);
        final Observable<OrderEvent> merge = commandHandler.observeMerge(toMergeOrders, command);

        return cancelSLTP.concatWith(merge);
    }

    public Observable<OrderEvent> mergeAll(final Function<Instrument, MergePositionCommand> commandFactory) {
        return Observable.defer(() -> {
            final Function<Instrument, Observable<OrderEvent>> observablesFromFactory =
                    instrument -> mergePosition(commandFactory.apply(instrument));
            return Observable.merge(positionUtil.observablesFromFactory(observablesFromFactory));
        });
    }
}
