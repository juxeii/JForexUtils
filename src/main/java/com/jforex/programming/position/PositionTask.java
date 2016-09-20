package com.jforex.programming.position;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.dukascopy.api.IOrder;
import com.dukascopy.api.Instrument;
import com.jforex.programming.order.MergeCommand;
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

    public Observable<OrderEvent> merge(final Instrument instrument,
                                        final MergeCommand command) {
        return Observable.defer(() -> {
            final Collection<IOrder> toMergeOrders = filledOrders(instrument);
            final Observable<OrderEvent> cancelSLTP = orderTask.createCancelSLTP(toMergeOrders, command);
            final Observable<OrderEvent> merge = orderTask.createMerge(toMergeOrders, command);

            return cancelSLTP.concatWith(merge);
        });
    }

    public Observable<OrderEvent> close(final Instrument instrument,
                                        final ClosePositionCommand command) {
        final Observable<OrderEvent> merge = merge(instrument, command.mergeCommand());
        final Observable<OrderEvent> close = createClose(instrument, command);

        return merge.concatWith(close);
    }

    private Observable<OrderEvent> createClose(final Instrument instrument,
                                               final ClosePositionCommand command) {
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
    }

    public Observable<OrderEvent> closeAll(final ClosePositionCommand command) {
        final List<Observable<OrderEvent>> observables = positionFactory
            .all()
            .stream()
            .map(Position::instrument)
            .map(instrument -> close(instrument, command))
            .collect(Collectors.toList());
        return Observable.merge(observables);
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
