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
import com.jforex.programming.order.task.params.ComplexMergeParams;
import com.jforex.programming.order.task.params.MergeParams;
import com.jforex.programming.order.task.params.SetAmountParams;
import com.jforex.programming.order.task.params.SetGTTParams;
import com.jforex.programming.order.task.params.SetLabelParams;
import com.jforex.programming.order.task.params.SetOpenPriceParams;
import com.jforex.programming.order.task.params.SetSLParams;
import com.jforex.programming.order.task.params.SubmitParams;
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

    public void submitOrder(final SubmitParams submitParams) {
        checkNotNull(submitParams);

        submitParams.subscribe(basicTask);
    }

    public void mergeOrders(final MergeParams mergeParams) {
        checkNotNull(mergeParams);

        mergeParams.subscribe(basicTask);
    }

    public Observable<OrderEvent> mergeOrders(final Collection<IOrder> toMergeOrders,
                                              final ComplexMergeParams mergeParams) {
        checkNotNull(toMergeOrders);
        checkNotNull(mergeParams);

        return mergeTask.merge(toMergeOrders, mergeParams);
    }

    public void close(final CloseParams closeParams) {
        checkNotNull(closeParams);

        closeParams.subscribe(basicTask);
    }

    public void setLabel(final SetLabelParams setLabelParams) {
        checkNotNull(setLabelParams);

        setLabelParams.subscribe(basicTask);
    }

    public void setGoodTillTime(final SetGTTParams setGTTParams) {
        checkNotNull(setGTTParams);

        setGTTParams.subscribe(basicTask);
    }

    public void setRequestedAmount(final SetAmountParams setAmountParams) {
        checkNotNull(setAmountParams);

        setAmountParams.subscribe(basicTask);
    }

    public void setOpenPrice(final SetOpenPriceParams setOpenPriceParams) {
        checkNotNull(setOpenPriceParams);

        setOpenPriceParams.subscribe(basicTask);
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
                                                final ComplexMergeParams mergeParams) {
        checkNotNull(instrument);
        checkNotNull(mergeParams);

        return mergeTask.mergePosition(instrument, mergeParams);
    }

    public Observable<OrderEvent> mergeAllPositions(final Function<Instrument, ComplexMergeParams> paramsFactory) {
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
