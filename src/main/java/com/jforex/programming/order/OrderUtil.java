package com.jforex.programming.order;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Collection;

import com.dukascopy.api.IOrder;
import com.dukascopy.api.Instrument;
import com.jforex.programming.order.command.ClosePositionCommand;
import com.jforex.programming.order.command.MergeCommand;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.task.BasicTask;
import com.jforex.programming.order.task.CloseTask;
import com.jforex.programming.order.task.MergeTask;
import com.jforex.programming.position.PositionOrders;
import com.jforex.programming.position.PositionUtil;

import io.reactivex.Observable;
import io.reactivex.functions.Function;

public class OrderUtil {

    private final BasicTask orderBasicTask;
    private final MergeTask orderMergeTask;
    private final CloseTask orderCloseTask;
    private final PositionUtil positionUtil;

    public OrderUtil(final BasicTask orderBasicTask,
                     final MergeTask orderMergeTask,
                     final CloseTask orderCloseTask,
                     final PositionUtil positionUtil) {
        this.orderBasicTask = orderBasicTask;
        this.orderMergeTask = orderMergeTask;
        this.orderCloseTask = orderCloseTask;
        this.positionUtil = positionUtil;
    }

    public Observable<OrderEvent> submitOrder(final OrderParams orderParams) {
        checkNotNull(orderParams);

        return orderBasicTask.submitOrder(orderParams);
    }

    public Observable<OrderEvent> mergeOrders(final String mergeOrderLabel,
                                              final Collection<IOrder> toMergeOrders) {
        checkNotNull(mergeOrderLabel);
        checkNotNull(toMergeOrders);

        return orderBasicTask.mergeOrders(mergeOrderLabel, toMergeOrders);
    }

    public Observable<OrderEvent> mergeOrders(final Collection<IOrder> toMergeOrders,
                                              final MergeCommand command) {
        checkNotNull(toMergeOrders);
        checkNotNull(command);

        return orderMergeTask.merge(toMergeOrders, command);
    }

    public Observable<OrderEvent> close(final IOrder order) {
        checkNotNull(order);

        return orderBasicTask.close(order);
    }

    public Observable<OrderEvent> setLabel(final IOrder order,
                                           final String label) {
        checkNotNull(order);
        checkNotNull(label);

        return orderBasicTask.setLabel(order, label);
    }

    public Observable<OrderEvent> setGoodTillTime(final IOrder order,
                                                  final long newGTT) {
        checkNotNull(order);

        return orderBasicTask.setGoodTillTime(order, newGTT);
    }

    public Observable<OrderEvent> setRequestedAmount(final IOrder order,
                                                     final double newRequestedAmount) {
        checkNotNull(order);

        return orderBasicTask.setRequestedAmount(order, newRequestedAmount);
    }

    public Observable<OrderEvent> setOpenPrice(final IOrder order,
                                               final double newOpenPrice) {
        checkNotNull(order);

        return orderBasicTask.setOpenPrice(order, newOpenPrice);
    }

    public Observable<OrderEvent> setStopLossPrice(final IOrder order,
                                                   final double newSL) {
        checkNotNull(order);

        return orderBasicTask.setStopLossPrice(order, newSL);
    }

    public Observable<OrderEvent> setTakeProfitPrice(final IOrder order,
                                                     final double newTP) {
        checkNotNull(order);

        return orderBasicTask.setTakeProfitPrice(order, newTP);
    }

    public Observable<OrderEvent> mergePosition(final Instrument instrument,
                                                final MergeCommand command) {
        checkNotNull(instrument);
        checkNotNull(command);

        return orderMergeTask.mergePosition(instrument, command);
    }

    public Observable<OrderEvent> mergeAllPositions(final Function<Instrument, MergeCommand> commandFactory) {
        checkNotNull(commandFactory);

        return orderMergeTask.mergeAllPositions(commandFactory);
    }

    public Observable<OrderEvent> closePosition(final ClosePositionCommand command) {
        checkNotNull(command);

        return orderCloseTask.close(command);
    }

    public Observable<OrderEvent> closeAllPositions(final Function<Instrument, ClosePositionCommand> commandFactory) {
        checkNotNull(commandFactory);

        return orderCloseTask.closeAllPositions(commandFactory);
    }

    public PositionOrders positionOrders(final Instrument instrument) {
        checkNotNull(instrument);

        return positionUtil.positionOrders(instrument);
    }
}
