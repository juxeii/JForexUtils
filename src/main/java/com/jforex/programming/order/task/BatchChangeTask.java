package com.jforex.programming.order.task;

import java.util.Collection;
import java.util.List;

import com.dukascopy.api.IOrder;
import com.jforex.programming.init.JForexUtil;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.event.OrderToEventTransformer;
import com.jforex.programming.order.task.params.CloseParams;
import com.jforex.programming.settings.PlatformSettings;

import io.reactivex.Observable;
import io.reactivex.functions.Function;

public class BatchChangeTask {

    private final BasicTask basicTask;

    private static final PlatformSettings platformSettings = JForexUtil.platformSettings;

    public BatchChangeTask(final BasicTask orderBasicTask) {
        this.basicTask = orderBasicTask;
    }

    public Observable<OrderEvent> close(final Collection<IOrder> orders,
                                        final Function<IOrder, CloseParams> closeParamsProvider,
                                        final BatchMode batchMode,
                                        final OrderToEventTransformer composer) {
        final Function<IOrder, Observable<OrderEvent>> taskCall = order -> basicTask
            .close(closeParamsProvider.apply(order))
            .compose(composer.apply(order));
        return forBasicTask(orders,
                            batchMode,
                            taskCall);
    }

    public Observable<OrderEvent> cancelSL(final Collection<IOrder> orders,
                                           final BatchMode batchMode,
                                           final OrderToEventTransformer composer) {
        final Function<IOrder, Observable<OrderEvent>> taskCall = order -> basicTask
            .setStopLossPrice(order, platformSettings.noSLPrice())
            .compose(composer.apply(order));
        return forBasicTask(orders,
                            batchMode,
                            taskCall);
    }

    public Observable<OrderEvent> cancelTP(final Collection<IOrder> orders,
                                           final BatchMode batchMode,
                                           final OrderToEventTransformer composer) {
        final Function<IOrder, Observable<OrderEvent>> taskCall = order -> basicTask
            .setTakeProfitPrice(order, platformSettings.noTPPrice())
            .compose(composer.apply(order));
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
