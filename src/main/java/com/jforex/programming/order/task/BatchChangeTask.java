package com.jforex.programming.order.task;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.task.params.TaskParamsUtil;
import com.jforex.programming.order.task.params.basic.CancelSLParams;
import com.jforex.programming.order.task.params.basic.CancelTPParams;
import com.jforex.programming.order.task.params.basic.CloseParams;
import com.jforex.programming.order.task.params.basic.SetSLParams;
import com.jforex.programming.order.task.params.basic.SetTPParams;
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
                                        final Function<IOrder, CloseParams> closeParamsFactory) {
        return forBasicTask(orders,
                            BatchMode.MERGE,
                            order -> closeOrderConsumer(order, closeParamsFactory));
    }

    private Observable<OrderEvent> closeOrderConsumer(final IOrder order,
                                                      final Function<IOrder, CloseParams> closeParamsFactory) {
        final CloseParams closeParams = closeParamsFactory.apply(order);
        final Observable<OrderEvent> closeObservable = basicTask.close(closeParams);
        return taskParamsUtil.composeTaskWithEventHandling(closeObservable, closeParams);
    }

    public Observable<OrderEvent> cancelSL(final Collection<IOrder> orders,
                                           final Function<IOrder, CancelSLParams> cancelSLParamsFactory,
                                           final BatchMode batchMode) {
        return forBasicTask(orders,
                            batchMode,
                            order -> cancelSLConsumer(order, cancelSLParamsFactory));
    }

    private Observable<OrderEvent> cancelSLConsumer(final IOrder order,
                                                    final Function<IOrder, CancelSLParams> cancelSLParamsFactory) {
        final CancelSLParams cancelSLParams = cancelSLParamsFactory.apply(order);
        final SetSLParams setSLParams =
                SetSLParams
                    .setSLAtPrice(order, platformSettings.noSLPrice())
                    .build();
        final Observable<OrderEvent> cancelSLObservable = basicTask.setStopLossPrice(setSLParams);
        return taskParamsUtil.composeTaskWithEventHandling(cancelSLObservable, cancelSLParams);
    }

    public Observable<OrderEvent> cancelTP(final Collection<IOrder> orders,
                                           final Function<IOrder, CancelTPParams> cancelTPParamsFactory,
                                           final BatchMode batchMode) {
        return forBasicTask(orders,
                            batchMode,
                            order -> cancelTPConsumer(order, cancelTPParamsFactory));
    }

    private Observable<OrderEvent> cancelTPConsumer(final IOrder order,
                                                    final Function<IOrder, CancelTPParams> cancelTPParamsFactory) {
        final CancelTPParams cancelTPParams = cancelTPParamsFactory.apply(order);
        final SetTPParams setTPParams =
                SetTPParams
                    .setTPAtPrice(order, platformSettings.noTPPrice())
                    .build();
        final Observable<OrderEvent> cancelTPObservable = basicTask.setTakeProfitPrice(setTPParams);
        return taskParamsUtil.composeTaskWithEventHandling(cancelTPObservable, cancelTPParams);
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
