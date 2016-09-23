package com.jforex.programming.order;

import java.util.Collection;

import com.dukascopy.api.IOrder;
import com.jforex.programming.misc.JForexUtil;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.settings.PlatformSettings;

import io.reactivex.Observable;
import io.reactivex.functions.Function;

public class OrderChangeBatch {

    private final OrderBasicTask orderBasicTask;

    private static final PlatformSettings platformSettings = JForexUtil.platformSettings;

    public OrderChangeBatch(final OrderBasicTask orderBasicTask) {
        this.orderBasicTask = orderBasicTask;
    }

    public Observable<OrderEvent> close(final Collection<IOrder> orders,
                                        final Function<IOrder,
                                                       Function<Observable<OrderEvent>,
                                                                Observable<OrderEvent>>> composer) {
        return forBasicTask(orders,
                            order -> orderBasicTask
                                .close(order)
                                .compose(composer.apply(order)));
    }

    public Observable<OrderEvent> cancelSL(final Collection<IOrder> orders,
                                           final Function<IOrder,
                                                          Function<Observable<OrderEvent>,
                                                                   Observable<OrderEvent>>> composer) {
        return forBasicTask(orders,
                            order -> orderBasicTask
                                .setStopLossPrice(order, platformSettings.noSLPrice())
                                .compose(composer.apply(order)));
    }

    public Observable<OrderEvent> cancelTP(final Collection<IOrder> orders,
                                           final Function<IOrder,
                                                          Function<Observable<OrderEvent>,
                                                                   Observable<OrderEvent>>> composer) {
        return forBasicTask(orders,
                            order -> orderBasicTask
                                .setTakeProfitPrice(order, platformSettings.noTPPrice())
                                .compose(composer.apply(order)));
    }

    public Observable<OrderEvent> forBasicTask(final Collection<IOrder> orders,
                                               final Function<IOrder, Observable<OrderEvent>> basicTask) {
        return Observable
            .fromIterable(orders)
            .flatMap(basicTask::apply);
    }
}
