package com.jforex.programming.order;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Collection;

import com.dukascopy.api.IOrder;
import com.dukascopy.api.Instrument;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.task.BasicTask;
import com.jforex.programming.order.task.CloseTask;
import com.jforex.programming.order.task.MergeTask;
import com.jforex.programming.order.task.params.BasicTaskParamsBase;
import com.jforex.programming.order.task.params.CloseParams;
import com.jforex.programming.order.task.params.ClosePositionParams;
import com.jforex.programming.order.task.params.ComplexMergeParams;
import com.jforex.programming.order.task.params.MergeParams;
import com.jforex.programming.order.task.params.SetAmountParams;
import com.jforex.programming.order.task.params.SetGTTParams;
import com.jforex.programming.order.task.params.SetLabelParams;
import com.jforex.programming.order.task.params.SetOpenPriceParams;
import com.jforex.programming.order.task.params.SetSLParams;
import com.jforex.programming.order.task.params.SetTPParams;
import com.jforex.programming.order.task.params.SubmitParams;
import com.jforex.programming.order.task.params.TaskParamsUtil;
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

        subscribe(basicTask.submitOrder(submitParams), submitParams);
    }

    public void mergeOrders(final MergeParams mergeParams) {
        checkNotNull(mergeParams);

        subscribe(basicTask.mergeOrders(mergeParams), mergeParams);
    }

    public Observable<OrderEvent> mergeOrders(final Collection<IOrder> toMergeOrders,
                                              final ComplexMergeParams mergeParams) {
        checkNotNull(toMergeOrders);
        checkNotNull(mergeParams);

        return mergeTask.merge(toMergeOrders, mergeParams);
    }

    public void close(final CloseParams closeParams) {
        checkNotNull(closeParams);

        subscribe(basicTask.close(closeParams), closeParams);
    }

    public void setLabel(final SetLabelParams setLabelParams) {
        checkNotNull(setLabelParams);

        subscribe(basicTask.setLabel(setLabelParams), setLabelParams);
    }

    public void setGoodTillTime(final SetGTTParams setGTTParams) {
        checkNotNull(setGTTParams);

        subscribe(basicTask.setGoodTillTime(setGTTParams), setGTTParams);
    }

    public void setRequestedAmount(final SetAmountParams setAmountParams) {
        checkNotNull(setAmountParams);

        subscribe(basicTask.setRequestedAmount(setAmountParams), setAmountParams);
    }

    public void setOpenPrice(final SetOpenPriceParams setOpenPriceParams) {
        checkNotNull(setOpenPriceParams);

        subscribe(basicTask.setOpenPrice(setOpenPriceParams), setOpenPriceParams);
    }

    public void setStopLossPrice(final SetSLParams setSLParams) {
        checkNotNull(setSLParams);

        subscribe(basicTask.setStopLossPrice(setSLParams), setSLParams);
    }

    public void setTakeProfitPrice(final SetTPParams setTPParams) {
        checkNotNull(setTPParams);

        subscribe(basicTask.setTakeProfitPrice(setTPParams), setTPParams);
    }

    private void subscribe(final Observable<OrderEvent> observable,
                           final BasicTaskParamsBase basicTaskParamsBase) {
        TaskParamsUtil.subscribe(observable, basicTaskParamsBase.subscribeParams());
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
