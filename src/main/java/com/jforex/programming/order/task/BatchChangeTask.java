package com.jforex.programming.order.task;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.task.params.TaskParamsUtil;
import com.jforex.programming.order.task.params.basic.CloseParams;
import com.jforex.programming.order.task.params.basic.SetSLParams;
import com.jforex.programming.order.task.params.basic.SetTPParams;
import com.jforex.programming.order.task.params.position.ClosePositionParams;
import com.jforex.programming.order.task.params.position.MergePositionParams;
import com.jforex.programming.settings.PlatformSettings;
import com.jforex.programming.strategy.StrategyUtil;

import io.reactivex.Observable;

public class BatchChangeTask {

    private final BasicTaskObservable basicTask;
    private final TaskParamsUtil taskParamsUtil;

    private static final PlatformSettings platformSettings = StrategyUtil.platformSettings;

    public BatchChangeTask(final BasicTaskObservable orderBasicTask,
                           final TaskParamsUtil taskParamsUtil) {
        this.basicTask = orderBasicTask;
        this.taskParamsUtil = taskParamsUtil;
    }

    public Observable<OrderEvent> close(final Collection<IOrder> orders,
                                        final ClosePositionParams closePositionParams) {
        return forBasicTask(orders,
                            BatchMode.MERGE,
                            order -> closeOrderConsumer(order, closePositionParams));
    }

    private Observable<OrderEvent> closeOrderConsumer(final IOrder order,
                                                      final ClosePositionParams closePositionParams) {
        final CloseParams closeParams =
                CloseParams
                    .withOrder(order)
                    .build();
        Observable<OrderEvent> closeObservable = basicTask.close(closeParams);
        closeObservable = taskParamsUtil.composeEventHandling(closeObservable,
                                                              closePositionParams.consumerForEvent());
        return taskParamsUtil.composeParamsForOrder(order,
                                                    closeObservable,
                                                    closePositionParams.closeComposeParams());
    }

    public Observable<OrderEvent> cancelSL(final Collection<IOrder> orders,
                                           final MergePositionParams mergePositionParams) {
        return forBasicTask(orders,
                            mergePositionParams.batchCancelSLMode(),
                            order -> cancelSLConsumer(order, mergePositionParams));
    }

    private Observable<OrderEvent> cancelSLConsumer(final IOrder order,
                                                    final MergePositionParams mergePositionParams) {
        final SetSLParams setSLParams =
                SetSLParams
                    .setSLAtPrice(order, platformSettings.noSLPrice())
                    .build();
        Observable<OrderEvent> cancelSLObservable = basicTask.setStopLossPrice(setSLParams);
        cancelSLObservable = taskParamsUtil.composeEventHandling(cancelSLObservable,
                                                                 mergePositionParams.consumerForEvent());
        return taskParamsUtil.composeParamsForOrder(order,
                                                    cancelSLObservable,
                                                    mergePositionParams.cancelSLComposeParams());
    }

    public Observable<OrderEvent> cancelTP(final Collection<IOrder> orders,
                                           final MergePositionParams mergePositionParams) {
        return forBasicTask(orders,
                            mergePositionParams.batchCancelTPMode(),
                            order -> cancelTPConsumer(order, mergePositionParams));
    }

    private Observable<OrderEvent> cancelTPConsumer(final IOrder order,
                                                    final MergePositionParams mergePositionParams) {
        final SetTPParams setTPParams =
                SetTPParams
                    .setTPAtPrice(order, platformSettings.noTPPrice())
                    .build();
        Observable<OrderEvent> cancelTPObservable = basicTask.setTakeProfitPrice(setTPParams);
        cancelTPObservable = taskParamsUtil.composeEventHandling(cancelTPObservable,
                                                                 mergePositionParams.consumerForEvent());
        return taskParamsUtil.composeParamsForOrder(order,
                                                    cancelTPObservable,
                                                    mergePositionParams.cancelTPComposeParams());
    }

    private Observable<OrderEvent> forBasicTask(final Collection<IOrder> orders,
                                                final BatchMode batchMode,
                                                final Function<IOrder, Observable<OrderEvent>> basicTask) {
        final List<Observable<OrderEvent>> observables = Observable
            .fromIterable(orders)
            .map(basicTask::apply)
            .toList()
            .blockingGet();

        return batchMode == BatchMode.MERGE
                ? Observable.merge(observables)
                : Observable.concat(observables);
    }
}
