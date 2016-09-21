package com.jforex.programming.position;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.dukascopy.api.IOrder;
import com.dukascopy.api.Instrument;
import com.google.common.collect.Lists;
import com.jforex.programming.order.MergeCommandWithParent;
import com.jforex.programming.order.MergePositionCommand;
import com.jforex.programming.order.OrderTask;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.position.ClosePositionCommand.CloseExecutionMode;

import io.reactivex.Observable;
import io.reactivex.functions.Function;

public class PositionTask {

    private final OrderTask orderTask;
    private final PositionFactory positionFactory;

    public PositionTask(final OrderTask orderTask,
                        final PositionFactory positionFactory) {
        this.orderTask = orderTask;
        this.positionFactory = positionFactory;
    }

    public PositionOrders positionOrders(final Instrument instrument) {
        return positionFactory.forInstrument(instrument);
    }

    public Observable<OrderEvent> merge(final MergePositionCommand command) {
        return Observable.defer(() -> mergeForInnerCommand(command.mergeCommandWithParent(),
                                                           command.instrument()));
    }

    private Observable<OrderEvent> mergeForInnerCommand(final MergeCommandWithParent innerMerge,
                                                        final Instrument instrument) {
        final Collection<IOrder> toMergeOrders = filledOrders(instrument);
        final Observable<OrderEvent> cancelSLTP = orderTask.createCancelSLTP(toMergeOrders, innerMerge);
        final Observable<OrderEvent> merge = orderTask.createMerge(toMergeOrders, innerMerge);

        return cancelSLTP.concatWith(merge);
    }

    public Observable<OrderEvent> close(final ClosePositionCommand command) {
        return Observable.defer(() -> {
            final Observable<OrderEvent> merge = mergeForInnerCommand(command.mergeCommandWithParent(),
                                                                      command.instrument());
            final Observable<OrderEvent> close = createClose(command.instrument(), command);

            return Observable.concat(Lists.newArrayList(merge, close));
        });
    }

    private Observable<OrderEvent> createClose(final Instrument instrument,
                                               final ClosePositionCommand command) {
        return Observable.defer(() -> {
            final CloseExecutionMode executionMode = command.executionMode();
            if (executionMode == CloseExecutionMode.CloseFilled)
                return batchFilled(instrument, order -> orderTask
                    .close(order)
                    .compose(command.closeFilledCompose(order)));
            if (executionMode == CloseExecutionMode.CloseOpened)
                return batchOpened(instrument, order -> orderTask
                    .close(order)
                    .compose(command.closeOpenedCompose(order)));

            return batchFilledOrOpened(instrument, order -> orderTask
                .close(order)
                .compose(command.closeAllCompose(order)));
        });
    }

    public Observable<OrderEvent> closeAll(final java.util.function.Function<Instrument,
                                                                             ClosePositionCommand> commandFactory) {
        return Observable.defer(() -> {
            final List<Observable<OrderEvent>> observables = positionFactory
                .all()
                .stream()
                .map(Position::instrument)
                .map(instrument -> close(commandFactory.apply(instrument)))
                .collect(Collectors.toList());
            return Observable.merge(observables);
        });
    }

    private final Observable<OrderEvent> batchFilledOrOpened(final Instrument instrument,
                                                             final Function<IOrder, Observable<OrderEvent>> batchTask) {
        return Observable.defer(() -> Observable
            .fromIterable(filledOrOpenedOrders(instrument))
            .flatMap(batchTask::apply));
    }

    private final Observable<OrderEvent> batchOpened(final Instrument instrument,
                                                     final Function<IOrder, Observable<OrderEvent>> batchTask) {
        return Observable.defer(() -> Observable
            .fromIterable(openedOrders(instrument))
            .flatMap(batchTask::apply));
    }

    private final Observable<OrderEvent> batchFilled(final Instrument instrument,
                                                     final Function<IOrder, Observable<OrderEvent>> batchTask) {
        return Observable.defer(() -> Observable
            .fromIterable(filledOrders(instrument))
            .flatMap(batchTask::apply));
    }

    private final Set<IOrder> filledOrders(final Instrument instrument) {
        return positionOrders(instrument).filled();
    }

    private final Set<IOrder> openedOrders(final Instrument instrument) {
        return positionOrders(instrument).opened();
    }

    private final Set<IOrder> filledOrOpenedOrders(final Instrument instrument) {
        return positionOrders(instrument).filledOrOpened();
    }
}
