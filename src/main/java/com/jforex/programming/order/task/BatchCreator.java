package com.jforex.programming.order.task;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.event.OrderEvent;

import io.reactivex.Observable;

public class BatchCreator {

    public Observable<OrderEvent> create(final Collection<IOrder> orders,
                                         final BatchMode batchMode,
                                         final Function<IOrder, Observable<OrderEvent>> basicTaskFunction) {
        final List<Observable<OrderEvent>> observables = Observable
            .fromIterable(orders)
            .map(basicTaskFunction::apply)
            .toList()
            .blockingGet();

        return batchMode == BatchMode.MERGE
                ? Observable.merge(observables)
                : Observable.concat(observables);
    }
}
