package com.jforex.programming.order;

import static com.google.common.base.Preconditions.checkNotNull;

import com.dukascopy.api.Instrument;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.task.BasicTask;
import com.jforex.programming.order.task.ClosePositionTask;
import com.jforex.programming.order.task.ComplexMergeTask;
import com.jforex.programming.order.task.params.TaskParamsUtil;
import com.jforex.programming.order.task.params.basic.BasicParamsBase;
import com.jforex.programming.order.task.params.basic.CloseParams;
import com.jforex.programming.order.task.params.basic.MergeParams;
import com.jforex.programming.order.task.params.basic.SetAmountParams;
import com.jforex.programming.order.task.params.basic.SetGTTParams;
import com.jforex.programming.order.task.params.basic.SetLabelParams;
import com.jforex.programming.order.task.params.basic.SetOpenPriceParams;
import com.jforex.programming.order.task.params.basic.SetSLParams;
import com.jforex.programming.order.task.params.basic.SetTPParams;
import com.jforex.programming.order.task.params.basic.SubmitParams;
import com.jforex.programming.order.task.params.position.ComplexClosePositionParams;
import com.jforex.programming.order.task.params.position.ComplexMergePositionParams;
import com.jforex.programming.position.PositionOrders;
import com.jforex.programming.position.PositionUtil;

import io.reactivex.Observable;

public class OrderUtil {

    private final BasicTask basicTask;
    private final ComplexMergeTask mergeTask;
    private final ClosePositionTask closePositionTask;
    private final PositionUtil positionUtil;

    public OrderUtil(final BasicTask basicTask,
                     final ComplexMergeTask mergeTask,
                     final ClosePositionTask closePositionTask,
                     final PositionUtil positionUtil) {
        this.basicTask = basicTask;
        this.mergeTask = mergeTask;
        this.closePositionTask = closePositionTask;
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
                           final BasicParamsBase basicTaskParamsBase) {
        TaskParamsUtil.subscribe(observable, basicTaskParamsBase.subscribeParams());
    }

    public void mergePosition(final Instrument instrument,
                              final ComplexMergePositionParams complexMergePositionParams) {
        checkNotNull(instrument);
        checkNotNull(complexMergePositionParams);

        TaskParamsUtil.subscribePositionTask(instrument,
                                             mergeTask.mergePosition(instrument, complexMergePositionParams),
                                             complexMergePositionParams);
    }

    public void mergeAllPositions(final ComplexMergePositionParams complexMergePositionParams) {
        checkNotNull(complexMergePositionParams);

        // TODO: fill handlers
        mergeTask
            .mergeAll(complexMergePositionParams)
            .subscribe();
    }

    public void closePosition(final Instrument instrument,
                              final ComplexClosePositionParams complexClosePositionParams) {
        checkNotNull(complexClosePositionParams);

        TaskParamsUtil.subscribePositionTask(instrument,
                                             closePositionTask.close(instrument, complexClosePositionParams),
                                             complexClosePositionParams);
    }

    public void closeAllPositions(final ComplexClosePositionParams complexClosePositionParams) {
        checkNotNull(complexClosePositionParams);

        // TODO: fill handlers
        closePositionTask
            .closeAll(complexClosePositionParams)
            .subscribe();
    }

    public PositionOrders positionOrders(final Instrument instrument) {
        checkNotNull(instrument);

        return positionUtil.positionOrders(instrument);
    }
}
