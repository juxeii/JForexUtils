package com.jforex.programming.position;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Set;

import com.dukascopy.api.IOrder;
import com.dukascopy.api.Instrument;
import com.jforex.programming.order.OrderUtil;
import com.jforex.programming.order.event.OrderEvent;

import io.reactivex.Observable;
import io.reactivex.functions.Function;

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
        final Observable<OrderEvent> mergeObservable = merge(instrument, mergeOrderLabel);
        final Observable<OrderEvent> closeObservable = batch(instrument, orderUtil::close);
        return mergeObservable.concatWith(closeObservable);
    }

    public final Observable<OrderEvent> cancelStopLossPrice(final Instrument instrument) {
        return batch(checkNotNull(instrument), orderUtil::cancelStopLossPrice);
    }

    public final Observable<OrderEvent> cancelTakeProfitPrice(final Instrument instrument) {
        return batch(checkNotNull(instrument), orderUtil::cancelTakeProfitPrice);
    }

    private final Observable<OrderEvent> batch(final Instrument instrument,
                                               final Function<IOrder, Observable<OrderEvent>> batchTask) {
        return Observable.defer(() -> Observable
            .fromIterable(filledOrOpenedOrders(instrument))
            .flatMap(batchTask::apply));
    }

    private final Set<IOrder> filledOrders(final Instrument instrument) {
        return positionOrders(instrument).filled();
    }

    private final Set<IOrder> filledOrOpenedOrders(final Instrument instrument) {
        return positionOrders(instrument).filledOrOpened();
    }
}
