package com.jforex.programming.position;

import java.util.Collection;
import java.util.Set;

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
        return Observable.defer(() -> mergeFromCommand(filledOrders(instrument), command));
    }

    private Observable<OrderEvent> mergeFromCommand(final Collection<IOrder> toMergeOrders,
                                                    final MergeCommand command) {
        final Observable<OrderEvent> cancelSLTP = orderTask.createCancelSLTP(toMergeOrders, command);
        final Observable<OrderEvent> merge = orderTask.createMerge(toMergeOrders, command);

        return cancelSLTP.concatWith(merge);
    }

    public Observable<OrderEvent> close(final ClosePositionCommand command) {
        final Observable<OrderEvent> merge = merge(command.instrument(), command.mergeCommand());
        final Observable<OrderEvent> close = createClose(command);

        return merge.concatWith(close);
    }

    private Observable<OrderEvent> createClose(final ClosePositionCommand command) {
        final CloseExecutionMode executionMode = command.executionMode();
        if (executionMode == CloseExecutionMode.CloseFilled)
            return batchFilled(command.instrument(), order -> orderTask
                .close(order)
                .compose(command.closeFilledCompose(order)));
        if (executionMode == CloseExecutionMode.CloseOpened)
            return batchOpened(command.instrument(), order -> orderTask
                .close(order)
                .compose(command.closeOpenedCompose(order)));

        return batchFilledOrOpened(command.instrument(), order -> orderTask
            .close(order)
            .compose(command.closeAllCompose(order)));
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
