package com.jforex.programming.order.task;

import java.util.Collection;
import java.util.List;

import com.dukascopy.api.IOrder;
import com.dukascopy.api.Instrument;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.task.params.BatchCancelSLParams;
import com.jforex.programming.order.task.params.BatchCancelTPParams;
import com.jforex.programming.order.task.params.CloseParams;
import com.jforex.programming.order.task.params.ClosePositionParams;
import com.jforex.programming.order.task.params.SetSLParams;
import com.jforex.programming.order.task.params.SetTPParams;
import com.jforex.programming.order.task.params.TaskParamsUtil;
import com.jforex.programming.settings.PlatformSettings;
import com.jforex.programming.strategy.StrategyUtil;

import io.reactivex.Observable;
import io.reactivex.functions.Function;

public class BatchChangeTask {

    private final BasicTask basicTask;

    private static final PlatformSettings platformSettings = StrategyUtil.platformSettings;

    public BatchChangeTask(final BasicTask orderBasicTask) {
        this.basicTask = orderBasicTask;
    }

    public Observable<OrderEvent> close(final Instrument instrument,
                                        final Collection<IOrder> orders,
                                        final ClosePositionParams closePositionParams) {
        final Function<IOrder, Observable<OrderEvent>> taskCall =
                order -> TaskParamsUtil.composeBatchClose(order.getInstrument(),
                                                          basicTask.close(CloseParams
                                                              .closeWith(order)
                                                              .build()),
                                                          closePositionParams);
        return forBasicTask(orders,
                            BatchMode.MERGE,
                            taskCall);
    }

    public Observable<OrderEvent> cancelSL(final Collection<IOrder> orders,
                                           final BatchCancelSLParams batchCancelSLParams) {
        final Function<IOrder, Observable<OrderEvent>> taskCall =
                order -> TaskParamsUtil.composeBatchCancelSL(order.getInstrument(),
                                                             basicTask.setStopLossPrice(SetSLParams
                                                                 .setSLAtPrice(order, platformSettings.noSLPrice())
                                                                 .build()),
                                                             batchCancelSLParams);
        return forBasicTask(orders,
                            batchCancelSLParams.batchMode(),
                            taskCall);
    }

    public Observable<OrderEvent> cancelTP(final Collection<IOrder> orders,
                                           final BatchCancelTPParams batchCancelTPParams) {
        final Function<IOrder, Observable<OrderEvent>> taskCall =
                order -> TaskParamsUtil.composeBatchCancelTP(order.getInstrument(),
                                                             basicTask.setTakeProfitPrice(SetTPParams
                                                                 .setTPAtPrice(order, platformSettings.noTPPrice())
                                                                 .build()),
                                                             batchCancelTPParams);
        return forBasicTask(orders,
                            batchCancelTPParams.batchMode(),
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
