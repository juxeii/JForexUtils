package com.jforex.programming.order;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Collection;

import com.dukascopy.api.IOrder;
import com.dukascopy.api.Instrument;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.task.BasicTask;
import com.jforex.programming.order.task.CloseTask;
import com.jforex.programming.order.task.MergeTask;
import com.jforex.programming.order.task.params.CloseParams;
import com.jforex.programming.order.task.params.ClosePositionParams;
import com.jforex.programming.order.task.params.MergeParams;
import com.jforex.programming.order.task.params.SetSLParams;
import com.jforex.programming.position.PositionOrders;
import com.jforex.programming.position.PositionUtil;

import io.reactivex.Observable;
import io.reactivex.functions.Function;

public class OrderUtil {

    private final BasicTask basicTask;
    private final MergeTask mergeTask;
    private final CloseTask closeTask;
    private final PositionUtil positionUtil;

    public OrderUtil(final BasicTask basicTask,
                     final MergeTask mergeTask,
                     final CloseTask closeTask,
                     final PositionUtil positionUtil) {
        this.basicTask = basicTask;
        this.mergeTask = mergeTask;
        this.closeTask = closeTask;
        this.positionUtil = positionUtil;
    }

    public Observable<OrderEvent> submitOrder(final OrderParams orderParams) {
        checkNotNull(orderParams);

        return basicTask.submitOrder(orderParams);
    }

    public Observable<OrderEvent> mergeOrders(final String mergeOrderLabel,
                                              final Collection<IOrder> toMergeOrders) {
        checkNotNull(mergeOrderLabel);
        checkNotNull(toMergeOrders);

        return basicTask.mergeOrders(mergeOrderLabel, toMergeOrders);
    }

    public Observable<OrderEvent> mergeOrders(final Collection<IOrder> toMergeOrders,
                                              final MergeParams mergeParams) {
        checkNotNull(toMergeOrders);
        checkNotNull(mergeParams);

        return mergeTask.merge(toMergeOrders, mergeParams);
    }

    public Observable<OrderEvent> close(final IOrder order) {
        checkNotNull(order);

        return basicTask.close(order);
    }

    public Observable<OrderEvent> close(final CloseParams closeParams) {
        checkNotNull(closeParams);

        return basicTask.close(closeParams);
    }

    public Observable<OrderEvent> setLabel(final IOrder order,
                                           final String label) {
        checkNotNull(order);
        checkNotNull(label);

        return basicTask.setLabel(order, label);
    }

    public Observable<OrderEvent> setGoodTillTime(final IOrder order,
                                                  final long newGTT) {
        checkNotNull(order);

        return basicTask.setGoodTillTime(order, newGTT);
    }

    public Observable<OrderEvent> setRequestedAmount(final IOrder order,
                                                     final double newRequestedAmount) {
        checkNotNull(order);

        return basicTask.setRequestedAmount(order, newRequestedAmount);
    }

    public Observable<OrderEvent> setOpenPrice(final IOrder order,
                                               final double newOpenPrice) {
        checkNotNull(order);

        return basicTask.setOpenPrice(order, newOpenPrice);
    }

    public Observable<OrderEvent> setStopLossPrice(final IOrder order,
                                                   final double newSL) {
        checkNotNull(order);

        return basicTask.setStopLossPrice(order, newSL);
    }

    public Observable<OrderEvent> setStopLossPrice(final SetSLParams setSLParams) {
        checkNotNull(setSLParams);

        return basicTask.setStopLossPrice(setSLParams);
    }

    public Observable<OrderEvent> setStopLossForPips(final IOrder order,
                                                     final double pips) {
        checkNotNull(order);

        return basicTask.setStopLossForPips(order, pips);
    }

    public Observable<OrderEvent> setTakeProfitPrice(final IOrder order,
                                                     final double newTP) {
        checkNotNull(order);

        return basicTask.setTakeProfitPrice(order, newTP);
    }

    public Observable<OrderEvent> setTakeProfitForPips(final IOrder order,
                                                       final double pips) {
        checkNotNull(order);

        return basicTask.setTakeProfitForPips(order, pips);
    }

    public Observable<OrderEvent> mergePosition(final Instrument instrument,
                                                final MergeParams mergeParams) {
        checkNotNull(instrument);
        checkNotNull(mergeParams);

        return mergeTask.mergePosition(instrument, mergeParams);
    }

    public Observable<OrderEvent> mergeAllPositions(final Function<Instrument, MergeParams> paramsFactory) {
        checkNotNull(paramsFactory);

        return mergeTask.mergeAllPositions(paramsFactory);
    }

    public Observable<OrderEvent> closePosition(final ClosePositionParams positionParams) {
        checkNotNull(positionParams);

        return closeTask.close(positionParams);
    }

    public Observable<OrderEvent> closeAllPositions(final Function<Instrument, ClosePositionParams> paramsFactory) {
        checkNotNull(paramsFactory);

        return closeTask.closeAllPositions(paramsFactory);
    }

    public PositionOrders positionOrders(final Instrument instrument) {
        checkNotNull(instrument);

        return positionUtil.positionOrders(instrument);
    }
}
