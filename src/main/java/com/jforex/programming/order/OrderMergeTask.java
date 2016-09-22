package com.jforex.programming.order;

import java.util.Collection;
import java.util.function.Function;

import com.dukascopy.api.IOrder;
import com.dukascopy.api.Instrument;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.position.PositionUtil;

import io.reactivex.Observable;

public class OrderMergeTask {

    private final OrderCancelSLAndTP orderCancelSLAndTP;
    private final OrderBasicTask orderBasicTask;
    private final PositionUtil positionUtil;

    public OrderMergeTask(final OrderCancelSLAndTP orderCancelSLAndTP,
                          final OrderBasicTask orderBasicTask,
                          final PositionUtil positionUtil) {
        this.orderCancelSLAndTP = orderCancelSLAndTP;
        this.orderBasicTask = orderBasicTask;
        this.positionUtil = positionUtil;
    }

    public Observable<OrderEvent> observeCommand(final MergeCommand command) {
        return observeCommonCommand(command.commonMergeCommand(),
                                    command.toMergeOrders());
    }

    public Observable<OrderEvent> observePositionCommand(final MergePositionCommand command) {
        return observeCommonCommand(command.commonMergeCommand(),
                                    positionUtil.filledOrders(command.instrument()));
    }

    public Observable<OrderEvent> observeCommonCommand(final CommonMergeCommand command,
                                                       final Collection<IOrder> toMergeOrders) {
        return Observable.defer(() -> splitCancelSLTPAndMerge(toMergeOrders, command));
    }

    public Observable<OrderEvent> splitCancelSLTPAndMerge(final Collection<IOrder> toMergeOrders,
                                                          final CommonMergeCommand command) {
        final String mergeOrderLabel = command.mergeOrderLabel();
        final Observable<OrderEvent> cancelSLTP = orderCancelSLAndTP.observeTask(toMergeOrders, command);
        final Observable<OrderEvent> merge = orderBasicTask.mergeOrders(mergeOrderLabel, toMergeOrders);

        return cancelSLTP.concatWith(merge);
    }

    public Observable<OrderEvent> mergeAll(final Function<Instrument, MergePositionCommand> commandFactory) {
        // TODO Auto-generated method stub
        return Observable.empty();
    }
}
