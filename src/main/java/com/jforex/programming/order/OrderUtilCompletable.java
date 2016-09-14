package com.jforex.programming.order;

import java.util.Collection;

import com.dukascopy.api.IOrder;
import com.dukascopy.api.Instrument;
import com.jforex.programming.order.command.Command;
import com.jforex.programming.position.Position;
import com.jforex.programming.position.PositionFactory;

import io.reactivex.Completable;

public class OrderUtilCompletable {

    private final OrderUtilHandler orderUtilHandler;
    private final PositionFactory positionFactory;

    public OrderUtilCompletable(final OrderUtilHandler orderUtilHandler,
                                final PositionFactory positionFactory) {
        this.orderUtilHandler = orderUtilHandler;
        this.positionFactory = positionFactory;
    }

    public Completable forCommand(final Command command) {
        return Completable.defer(() -> orderUtilHandler
            .callObservable(command)
            .toCompletable());
    }

    public Completable forCommandWithOrderMarking(final Command command,
                                                  final Collection<IOrder> ordersToMark) {
        return Completable.defer(() -> {
            final Instrument instrument = OrderStaticUtil.instrumentFromOrders(ordersToMark);
            final Position position = positionFactory.forInstrument(instrument);
            position.markOrdersActive(ordersToMark);
            return forCommand(command)
                .doOnTerminate(() -> position.markOrdersIdle(ordersToMark));
        });
    }
}
