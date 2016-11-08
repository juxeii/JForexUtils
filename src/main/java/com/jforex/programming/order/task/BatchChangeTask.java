package com.jforex.programming.order.task;

import java.util.Collection;
import java.util.List;

import com.dukascopy.api.IOrder;
import com.dukascopy.api.Instrument;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.task.params.TaskParamsUtil;
import com.jforex.programming.order.task.params.basic.CloseParams;
import com.jforex.programming.order.task.params.basic.SetSLParams;
import com.jforex.programming.order.task.params.basic.SetTPParams;
import com.jforex.programming.order.task.params.position.CancelSLParams;
import com.jforex.programming.order.task.params.position.CancelTPParams;
import com.jforex.programming.order.task.params.position.SimpleClosePositionParams;
import com.jforex.programming.settings.PlatformSettings;
import com.jforex.programming.strategy.StrategyUtil;

import io.reactivex.Observable;
import io.reactivex.functions.Function;

public class BatchChangeTask {

    private final BasicTask basicTask;
    private final TaskParamsUtil taskParamsUtil;

    private static final PlatformSettings platformSettings = StrategyUtil.platformSettings;

    public BatchChangeTask(final BasicTask orderBasicTask,
                           final TaskParamsUtil taskParamsUtil) {
        this.basicTask = orderBasicTask;
        this.taskParamsUtil = taskParamsUtil;
    }

    public Observable<OrderEvent> close(final Instrument instrument,
                                        final Collection<IOrder> orders,
                                        final SimpleClosePositionParams closePositionParams) {
        final Function<IOrder, Observable<OrderEvent>> taskCall =
                order -> taskParamsUtil.composePositionTask(order.getInstrument(),
                                                            basicTask.close(CloseParams
                                                                .closeOrder(order)
                                                                .build()),
                                                            closePositionParams);
        return forBasicTask(orders,
                            BatchMode.MERGE,
                            taskCall);
    }

    public Observable<OrderEvent> cancelSL(final Collection<IOrder> orders,
                                           final CancelSLParams cancelSLParams,
                                           final BatchMode batchMode) {
        final Function<IOrder, Observable<OrderEvent>> taskCall =
                order -> taskParamsUtil.composePositionTask(order,
                                                            basicTask.setStopLossPrice(SetSLParams
                                                                .setSLAtPrice(order, platformSettings.noSLPrice())
                                                                .build()),
                                                            cancelSLParams);
        return forBasicTask(orders,
                            batchMode,
                            taskCall);
    }

    public Observable<OrderEvent> cancelTP(final Collection<IOrder> orders,
                                           final CancelTPParams cancelTPParams,
                                           final BatchMode batchMode) {
        final Function<IOrder, Observable<OrderEvent>> taskCall =
                order -> taskParamsUtil.composePositionTask(order,
                                                            basicTask.setTakeProfitPrice(SetTPParams
                                                                .setTPAtPrice(order, platformSettings.noTPPrice())
                                                                .build()),
                                                            cancelTPParams);
        return forBasicTask(orders,
                            batchMode,
                            taskCall);
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
