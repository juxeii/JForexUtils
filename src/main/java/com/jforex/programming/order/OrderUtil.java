package com.jforex.programming.order;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.function.Function;

import com.dukascopy.api.IOrder;
import com.dukascopy.api.Instrument;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.position.ClosePositionCommand;
import com.jforex.programming.position.PositionOrders;
import com.jforex.programming.position.PositionTask;

import io.reactivex.Observable;

public class OrderUtil {

    private final OrderTask orderTask;
    private final PositionTask positionTask;

    public OrderUtil(final OrderTask orderTask,
                     final PositionTask positionTask) {
        this.orderTask = orderTask;
        this.positionTask = positionTask;
    }

    public Observable<OrderEvent> submitOrder(final OrderParams orderParams) {
        checkNotNull(orderParams);

        return defer(orderTask.submitOrder(orderParams));
    }

    public Observable<OrderEvent> mergeOrders(final MergeCommand command) {
        checkNotNull(command);

        return defer(orderTask.mergeOrders(command));
    }

    public Observable<OrderEvent> close(final IOrder order) {
        checkNotNull(order);

        return defer(orderTask.close(order));
    }

    public Observable<OrderEvent> setLabel(final IOrder order,
                                           final String label) {
        checkNotNull(order);
        checkNotNull(label);

        return defer(orderTask.setLabel(order, label));
    }

    public Observable<OrderEvent> setGoodTillTime(final IOrder order,
                                                  final long newGTT) {
        checkNotNull(order);

        return defer(orderTask.setGoodTillTime(order, newGTT));
    }

    public Observable<OrderEvent> setRequestedAmount(final IOrder order,
                                                     final double newRequestedAmount) {
        checkNotNull(order);

        return defer(orderTask.setRequestedAmount(order, newRequestedAmount));
    }

    public Observable<OrderEvent> setOpenPrice(final IOrder order,
                                               final double newOpenPrice) {
        checkNotNull(order);

        return defer(orderTask.setOpenPrice(order, newOpenPrice));
    }

    public Observable<OrderEvent> setStopLossPrice(final IOrder order,
                                                   final double newSL) {
        checkNotNull(order);

        return defer(orderTask.setStopLossPrice(order, newSL));
    }

    public Observable<OrderEvent> setTakeProfitPrice(final IOrder order,
                                                     final double newTP) {
        checkNotNull(order);

        return defer(orderTask.setTakeProfitPrice(order, newTP));
    }

    public Observable<OrderEvent> mergePosition(final MergePositionCommand command) {
        checkNotNull(command);

        return defer(positionTask.merge(command));
    }

    public Observable<OrderEvent> closePosition(final ClosePositionCommand command) {
        checkNotNull(command);

        return defer(positionTask.close(command));
    }

    public Observable<OrderEvent> closeAllPositions(final Function<Instrument, ClosePositionCommand> commandFactory) {
        checkNotNull(commandFactory);

        return defer(positionTask.closeAll(commandFactory));
    }

    private final Observable<OrderEvent> defer(final Observable<OrderEvent> observable) {
        return Observable.defer(() -> observable);
    }

    public final PositionOrders positionOrders(final Instrument instrument) {
        checkNotNull(instrument);

        return positionTask.positionOrders(instrument);
    }
}
