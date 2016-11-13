package com.jforex.programming.order;

import static com.google.common.base.Preconditions.checkNotNull;

import com.dukascopy.api.Instrument;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.task.BasicTaskObservable;
import com.jforex.programming.order.task.ClosePositionTask;
import com.jforex.programming.order.task.MergePositionTask;
import com.jforex.programming.order.task.params.CommonParamsBase;
import com.jforex.programming.order.task.params.TaskParamsUtil;
import com.jforex.programming.order.task.params.basic.CloseParams;
import com.jforex.programming.order.task.params.basic.MergeParams;
import com.jforex.programming.order.task.params.basic.SetAmountParams;
import com.jforex.programming.order.task.params.basic.SetGTTParams;
import com.jforex.programming.order.task.params.basic.SetLabelParams;
import com.jforex.programming.order.task.params.basic.SetOpenPriceParams;
import com.jforex.programming.order.task.params.basic.SetSLParams;
import com.jforex.programming.order.task.params.basic.SetTPParams;
import com.jforex.programming.order.task.params.basic.SubmitParams;
import com.jforex.programming.order.task.params.position.CloseAllPositionsParams;
import com.jforex.programming.order.task.params.position.ClosePositionParams;
import com.jforex.programming.order.task.params.position.MergeAllPositionsParams;
import com.jforex.programming.order.task.params.position.MergePositionParams;
import com.jforex.programming.position.PositionOrders;
import com.jforex.programming.position.PositionUtil;

import io.reactivex.Observable;

public class OrderUtil {

    private final BasicTaskObservable basicTask;
    private final MergePositionTask mergePositionTask;
    private final ClosePositionTask closePositionTask;
    private final PositionUtil positionUtil;
    private final TaskParamsUtil taskParamsUtil;

    public OrderUtil(final BasicTaskObservable basicTask,
                     final MergePositionTask mergeTask,
                     final ClosePositionTask closePositionTask,
                     final PositionUtil positionUtil,
                     final TaskParamsUtil taskParamsUtil) {
        this.basicTask = basicTask;
        this.mergePositionTask = mergeTask;
        this.closePositionTask = closePositionTask;
        this.positionUtil = positionUtil;
        this.taskParamsUtil = taskParamsUtil;
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
                           final CommonParamsBase commonParamsBase) {
        taskParamsUtil.subscribeBasicParams(observable, commonParamsBase);
    }

    public void mergePosition(final MergePositionParams mergePositionParams) {
        checkNotNull(mergePositionParams);

        final Observable<OrderEvent> observable = mergePositionTask.merge(mergePositionParams);
        taskParamsUtil.subscribeComposeParams(observable,
                                              mergePositionParams.mergePositionComposeParams());
    }

    public void mergeAllPositions(final MergeAllPositionsParams mergeAllPositionParams) {
        checkNotNull(mergeAllPositionParams);

        taskParamsUtil.subscribeComposeParams(mergePositionTask.mergeAll(mergeAllPositionParams),
                                              mergeAllPositionParams.mergeAllPositionsComposeParams());
    }

    public void closePosition(final ClosePositionParams closePositionParams) {
        checkNotNull(closePositionParams);

        final Observable<OrderEvent> observable = closePositionTask.close(closePositionParams);
        taskParamsUtil.subscribeComposeParams(observable,
                                              closePositionParams.closePositionComposeParams());
    }

    public void closeAllPositions(final CloseAllPositionsParams closeAllPositionParams) {
        checkNotNull(closeAllPositionParams);

        taskParamsUtil.subscribeComposeParams(closePositionTask.closeAll(closeAllPositionParams),
                                              closeAllPositionParams.closeAllPositionsComposeParams());
    }

    public PositionOrders positionOrders(final Instrument instrument) {
        checkNotNull(instrument);

        return positionUtil.positionOrders(instrument);
    }
}
