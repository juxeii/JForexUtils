package com.jforex.programming.position;

import java.util.Set;

import com.dukascopy.api.IOrder;
import com.dukascopy.api.Instrument;
import com.jforex.programming.misc.JForexUtil;
import com.jforex.programming.order.OrderTask;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.position.MergePositionCommand.ExecutionMode;
import com.jforex.programming.settings.PlatformSettings;

import io.reactivex.Observable;
import io.reactivex.functions.Function;

public class PositionTask {

    private final OrderTask orderTask;
    private final PositionFactory positionFactory;

    private static final PlatformSettings platformSettings = JForexUtil.platformSettings;

    public PositionTask(final OrderTask orderTask,
                        final PositionFactory positionFactory) {
        this.orderTask = orderTask;
        this.positionFactory = positionFactory;
    }

    public PositionOrders positionOrders(final Instrument instrument) {
        return positionFactory.forInstrument(instrument);
    }

    public Observable<OrderEvent> merge(final Instrument instrument,
                                        final String mergeOrderLabel) {
        return Observable.defer(() -> {
            final Set<IOrder> toMergeOrders = filledOrders(instrument);
            return toMergeOrders.size() < 2
                    ? Observable.empty()
                    : orderTask.mergeOrders(mergeOrderLabel, toMergeOrders);
        });
    }

    public Observable<OrderEvent> merge(final MergePositionCommand command) {
        return Observable.defer(() -> {
            final Observable<OrderEvent> cancelSLTP =
                    createCancelSLTP(command).compose(command.maybeCancelSLTPCompose().get());
            final Observable<OrderEvent> merge =
                    merge(command.instrument(), command.mergeOrderLabel()).compose(command.mergeCompose());

            return cancelSLTP.concatWith(merge);
        });
    }

    private Observable<OrderEvent> createCancelSLTP(final MergePositionCommand command) {
        if (filledOrders(command.instrument()).size() < 2)
            return Observable.empty();

        if (!command.maybeCancelSLTPCompose().isPresent())
            return Observable.empty();

        final ExecutionMode executionMode = command.executionMode();
        if (executionMode == ExecutionMode.ConcatSLAndTP)
            return cancelSL(command).concatWith(cancelTP(command));
        if (executionMode == ExecutionMode.ConcatTPAndSL)
            return cancelTP(command).concatWith(cancelSL(command));

        return cancelSL(command).mergeWith(cancelTP(command));
    }

    private Observable<OrderEvent> cancelSL(final MergePositionCommand command) {
        return batchFilled(command.instrument(), order -> orderTask
            .setStopLossPrice(order, platformSettings.noSLPrice())
            .compose(command.cancelSLCompose(order)));
    }

    private Observable<OrderEvent> cancelTP(final MergePositionCommand command) {
        return batchFilled(command.instrument(), order -> orderTask
            .setTakeProfitPrice(order, platformSettings.noTPPrice())
            .compose(command.cancelTPCompose(order)));
    }

    public Observable<OrderEvent> close(final ClosePositionCommand command) {
        final Instrument instrument = command.instrument();
        final String mergeOrderLabel = command.mergeOrderLabel();

        final Observable<OrderEvent> mergeObservable =
                merge(instrument, mergeOrderLabel).compose(command.mergeComposer());
        final Observable<OrderEvent> closeObservable = batchFilledOrOpened(instrument, order -> orderTask
            .close(order)
            .compose(command.closeComposer(order)));

        return mergeObservable.concatWith(closeObservable);
    }

    private final Observable<OrderEvent> batchFilledOrOpened(final Instrument instrument,
                                                             final Function<IOrder, Observable<OrderEvent>> batchTask) {
        return Observable.defer(() -> Observable
            .fromIterable(filledOrOpenedOrders(instrument))
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

    private final Set<IOrder> filledOrOpenedOrders(final Instrument instrument) {
        return positionOrders(instrument).filledOrOpened();
    }
}
