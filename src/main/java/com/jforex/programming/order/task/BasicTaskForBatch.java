package com.jforex.programming.order.task;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.task.params.basic.CloseParams;
import com.jforex.programming.order.task.params.basic.SetSLParams;
import com.jforex.programming.order.task.params.basic.SetTPParams;
import com.jforex.programming.settings.PlatformSettings;
import com.jforex.programming.strategy.StrategyUtil;

import io.reactivex.Observable;

public class BasicTaskForBatch {

    private final BasicTask basicTask;

    private static final PlatformSettings platformSettings = StrategyUtil.platformSettings;

    public BasicTaskForBatch(final BasicTask basicTask) {
        this.basicTask = basicTask;
    }

    public Observable<OrderEvent> forClose(final CloseParams closeParams) {
        return basicTask.close(closeParams);
    }

    public Observable<OrderEvent> forCancelSL(final IOrder order) {
        final SetSLParams setSLParams = SetSLParams
            .setSLAtPrice(order, platformSettings.noSLPrice())
            .build();
        return basicTask.setStopLossPrice(setSLParams);
    }

    public Observable<OrderEvent> forCancelTP(final IOrder order) {
        final SetTPParams setTPParams = SetTPParams
            .setTPAtPrice(order, platformSettings.noTPPrice())
            .build();
        return basicTask.setTakeProfitPrice(setTPParams);
    }
}
