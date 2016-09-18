package com.jforex.programming.position;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Collection;
import java.util.Set;

import com.dukascopy.api.IOrder;
import com.dukascopy.api.Instrument;
import com.jforex.programming.order.OrderUtil;
import com.jforex.programming.order.event.OrderEvent;

import io.reactivex.Observable;

public class PositionUtil {

    private final OrderUtil orderUtil;
    private final PositionFactory positionFactory;

    public PositionUtil(final OrderUtil orderUtil,
                        final PositionFactory positionFactory) {
        this.orderUtil = orderUtil;
        this.positionFactory = positionFactory;
    }

    public final PositionOrders positionOrders(final Instrument instrument) {
        return positionFactory.forInstrument(checkNotNull(instrument));
    }

    public final Observable<OrderEvent> merge(final Instrument instrument,
                                              final String mergeOrderLabel) {
        return Observable.defer(() -> {
            final Set<IOrder> toMergeOrders = filledOrders(checkNotNull(instrument));
            return toMergeOrders.size() < 2
                    ? Observable.empty()
                    : orderUtil.mergeOrders(mergeOrderLabel, toMergeOrders);
        });
    }

    public final Observable<OrderEvent> close(final Instrument instrument,
                                              final String mergeOrderLabel) {
        return Observable.defer(() -> {
            final Observable<OrderEvent> mergeObservable = merge(instrument, mergeOrderLabel);
            final Observable<OrderEvent> closeObservable =
                    Observable.defer(() -> closeBatch(filledOrOpenedOrders(instrument)));

            return mergeObservable.concatWith(closeObservable);
        });
    }

    private final Observable<OrderEvent> closeBatch(final Collection<IOrder> toCloseOrders) {
        return Observable
            .fromIterable(toCloseOrders)
            .flatMap(orderUtil::close);
    }

    private final Set<IOrder> filledOrders(final Instrument instrument) {
        return positionOrders(instrument).filled();
    }

    private final Set<IOrder> filledOrOpenedOrders(final Instrument instrument) {
        return positionOrders(instrument).filledOrOpened();
    }
}
