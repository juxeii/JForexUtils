package com.jforex.programming.order.task;

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

public class BatchComposer {

    private final BasicTaskObservable basicTask;
    private final TaskParamsUtil taskParamsUtil;

    private static final PlatformSettings platformSettings = StrategyUtil.platformSettings;

    public BatchComposer(final BasicTaskObservable orderBasicTask,
                         final TaskParamsUtil taskParamsUtil) {
        this.basicTask = orderBasicTask;
        this.taskParamsUtil = taskParamsUtil;
    }

    public Function<IOrder, Observable<OrderEvent>> composeClose(final ClosePositionParams closePositionParams) {
        return order -> {
            final CloseParams closeParams =
                    CloseParams
                        .withOrder(order)
                        .build();
            return taskParamsUtil.composeParamsWithEvents(order,
                                                          basicTask.close(closeParams),
                                                          closePositionParams.closeComposeParams(order),
                                                          closePositionParams.consumerForEvent());
        };
    }

    public Function<IOrder, Observable<OrderEvent>> composeCancelSL(final MergePositionParams mergePositionParams) {
        return order -> {
            final SetSLParams setSLParams =
                    SetSLParams
                        .setSLAtPrice(order, platformSettings.noSLPrice())
                        .build();
            return taskParamsUtil.composeParamsWithEvents(order,
                                                          basicTask.setStopLossPrice(setSLParams),
                                                          mergePositionParams.cancelSLComposeParams(order),
                                                          mergePositionParams.consumerForEvent());
        };
    }

    public Function<IOrder, Observable<OrderEvent>> composeCancelTP(final MergePositionParams mergePositionParams) {
        return order -> {
            final SetTPParams setTPParams =
                    SetTPParams
                        .setTPAtPrice(order, platformSettings.noTPPrice())
                        .build();
            return taskParamsUtil.composeParamsWithEvents(order,
                                                          basicTask.setTakeProfitPrice(setTPParams),
                                                          mergePositionParams.cancelTPComposeParams(order),
                                                          mergePositionParams.consumerForEvent());
        };
    }
}
