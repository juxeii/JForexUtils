package com.jforex.programming.order;

import java.util.Collection;
import java.util.Map;

import com.dukascopy.api.IOrder;
import com.dukascopy.api.Instrument;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.position.ClosePositionCommand;
import com.jforex.programming.position.ClosePositionCommand.CloseExecutionMode;
import com.jforex.programming.position.PositionUtil;

import io.reactivex.Observable;
import io.reactivex.functions.Function;

public class OrderCloseTask {

    private final OrderMergeTask orderMergeTask;
    private final OrderChangeBatch orderChangeBatch;
    private final PositionUtil positionUtil;

    private final Map<CloseExecutionMode, Function<Instrument, Collection<IOrder>>> ordersToCloseByExecutionMode;

    public OrderCloseTask(final OrderMergeTask orderMergeTask,
                          final OrderChangeBatch orderChangeBatch,
                          final PositionUtil positionUtil) {
        this.orderMergeTask = orderMergeTask;
        this.orderChangeBatch = orderChangeBatch;
        this.positionUtil = positionUtil;

        ordersToCloseByExecutionMode = Maps.immutableEnumMap(ImmutableMap.<CloseExecutionMode,
                                                                           Function<Instrument,
                                                                                    Collection<IOrder>>> builder()
            .put(CloseExecutionMode.CloseFilled, positionUtil::filledOrders)
            .put(CloseExecutionMode.CloseOpened, positionUtil::openedOrders)
            .put(CloseExecutionMode.CloseAll, positionUtil::filledOrOpenedOrders)
            .build());
    }

    public Observable<OrderEvent> close(final ClosePositionCommand command) {
        return Observable.defer(() -> {
            final Instrument instrument = command.instrument();
            final Collection<IOrder> ordersToClose = positionUtil.filledOrders(instrument);
            final CommonMergeCommand mergeCommand = command.commonMergeCommand();

            final Observable<OrderEvent> merge = orderMergeTask.observeCommonCommand(mergeCommand, ordersToClose);
            final Observable<OrderEvent> close = observeClose(instrument, command);

            return merge.concatWith(close);
        });
    }

    private Observable<OrderEvent> observeClose(final Instrument instrument,
                                                final ClosePositionCommand command) {
        return Observable.defer(() -> {
            final CloseExecutionMode executionMode = command.executionMode();
            final Collection<IOrder> ordersToClose = ordersToCloseByExecutionMode
                .get(executionMode)
                .apply(instrument);

            final Observable<OrderEvent> batchClose = orderChangeBatch.close(ordersToClose);
            if (executionMode == CloseExecutionMode.CloseFilled)
                return batchClose.compose(command.closeFilledCompose());
            if (executionMode == CloseExecutionMode.CloseOpened)
                return batchClose.compose(command.closeOpenedCompose());
            return batchClose.compose(command.closeAllCompose());
        });
    }

    public Observable<OrderEvent> closeAll(final java.util.function.Function<Instrument,
                                                                             ClosePositionCommand> commandFactory) {
//        return Observable.defer(() -> {
//            final List<Observable<OrderEvent>> observables = positionFactory
//                .all()
//                .stream()
//                .map(Position::instrument)
//                .map(instrument -> close(commandFactory.apply(instrument)))
//                .collect(Collectors.toList());
//            return Observable.merge(observables);
//        });
        return Observable.empty();
    }
}
