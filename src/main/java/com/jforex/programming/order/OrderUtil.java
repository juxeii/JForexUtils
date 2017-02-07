package com.jforex.programming.order;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.dukascopy.api.Instrument;
import com.google.common.collect.ImmutableMap;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.task.BasicTask;
import com.jforex.programming.order.task.ClosePositionTask;
import com.jforex.programming.order.task.MergePositionTask;
import com.jforex.programming.order.task.params.CommonParamsBase;
import com.jforex.programming.order.task.params.ComposeData;
import com.jforex.programming.order.task.params.TaskParamsUtil;
import com.jforex.programming.order.task.params.basic.BatchParams;
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

    private final BasicTask basicTask;
    private final MergePositionTask mergePositionTask;
    private final ClosePositionTask closePositionTask;
    private final PositionUtil positionUtil;
    private final TaskParamsUtil taskParamsUtil;
    private Map<Class<?>, Function<CommonParamsBase, Observable<OrderEvent>>> paramsMapper;

    public OrderUtil(final BasicTask basicTask,
                     final MergePositionTask mergeTask,
                     final ClosePositionTask closePositionTask,
                     final PositionUtil positionUtil,
                     final TaskParamsUtil taskParamsUtil) {
        this.basicTask = basicTask;
        this.mergePositionTask = mergeTask;
        this.closePositionTask = closePositionTask;
        this.positionUtil = positionUtil;
        this.taskParamsUtil = taskParamsUtil;

        paramsMapper = ImmutableMap.<Class<?>, Function<CommonParamsBase, Observable<OrderEvent>>> builder()
            .put(SubmitParams.class, params -> basicTask.submitOrder((SubmitParams) params))
            .put(MergeParams.class, params -> basicTask.mergeOrders((MergeParams) params))
            .put(CloseParams.class, params -> basicTask.close((CloseParams) params))
            .put(SetLabelParams.class, params -> basicTask.setLabel((SetLabelParams) params))
            .put(SetGTTParams.class, params -> basicTask.setGoodTillTime((SetGTTParams) params))
            .put(SetAmountParams.class, params -> basicTask.setRequestedAmount((SetAmountParams) params))
            .put(SetOpenPriceParams.class, params -> basicTask.setOpenPrice((SetOpenPriceParams) params))
            .put(SetSLParams.class, params -> basicTask.setStopLossPrice((SetSLParams) params))
            .put(SetTPParams.class, params -> basicTask.setTakeProfitPrice((SetTPParams) params))
            .build();
    }

    public void submitOrder(final SubmitParams submitParams) {
        checkNotNull(submitParams);

        subscribeBasic(basicTask.submitOrder(submitParams), submitParams);
    }

    public void mergeOrders(final MergeParams mergeParams) {
        checkNotNull(mergeParams);

        subscribeBasic(basicTask.mergeOrders(mergeParams), mergeParams);
    }

    public void close(final CloseParams closeParams) {
        checkNotNull(closeParams);

        subscribeBasic(basicTask.close(closeParams), closeParams);
    }

    public void setLabel(final SetLabelParams setLabelParams) {
        checkNotNull(setLabelParams);

        subscribeBasic(basicTask.setLabel(setLabelParams), setLabelParams);
    }

    public void setGoodTillTime(final SetGTTParams setGTTParams) {
        checkNotNull(setGTTParams);

        subscribeBasic(basicTask.setGoodTillTime(setGTTParams), setGTTParams);
    }

    public void setRequestedAmount(final SetAmountParams setAmountParams) {
        checkNotNull(setAmountParams);

        subscribeBasic(basicTask.setRequestedAmount(setAmountParams), setAmountParams);
    }

    public void setOpenPrice(final SetOpenPriceParams setOpenPriceParams) {
        checkNotNull(setOpenPriceParams);

        subscribeBasic(basicTask.setOpenPrice(setOpenPriceParams), setOpenPriceParams);
    }

    public void setStopLossPrice(final SetSLParams setSLParams) {
        checkNotNull(setSLParams);

        subscribeBasic(basicTask.setStopLossPrice(setSLParams), setSLParams);
    }

    public void setTakeProfitPrice(final SetTPParams setTPParams) {
        checkNotNull(setTPParams);

        subscribeBasic(basicTask.setTakeProfitPrice(setTPParams), setTPParams);
    }

    public void executeBatch(final BatchParams batchParams) {
        List<Observable<OrderEvent>> observables = batchParams
            .paramsList()
            .stream()
            .map(this::paramsToObservable)
            .collect(Collectors.toList());

        taskParamsUtil.subscribeComposeData(Observable.merge(observables), batchParams.composeData());
    }

    private final Observable<OrderEvent> paramsToObservable(final CommonParamsBase commonParamsBase) {
        Observable<OrderEvent> observable = paramsMapper
            .get(commonParamsBase.getClass())
            .apply(commonParamsBase);

        return taskParamsUtil.composeParams(observable, commonParamsBase.composeData());
    }

    private final void subscribeBasic(final Observable<OrderEvent> observable,
                                      final CommonParamsBase commonParamsBase) {
        taskParamsUtil.subscribeBasicParams(observable, commonParamsBase);
    }

    public void mergePosition(final MergePositionParams mergePositionParams) {
        checkNotNull(mergePositionParams);

        subscribePosition(mergePositionTask.merge(mergePositionParams),
                          mergePositionParams.mergePositionComposeParams());
    }

    public void mergeAllPositions(final MergeAllPositionsParams mergeAllPositionParams) {
        checkNotNull(mergeAllPositionParams);

        subscribePosition(mergePositionTask.mergeAll(mergeAllPositionParams),
                          mergeAllPositionParams.mergeAllPositionsComposeData());
    }

    public void closePosition(final ClosePositionParams closePositionParams) {
        checkNotNull(closePositionParams);

        subscribePosition(closePositionTask.close(closePositionParams),
                          closePositionParams.closePositionComposeParams());
    }

    public void closeAllPositions(final CloseAllPositionsParams closeAllPositionParams) {
        checkNotNull(closeAllPositionParams);

        subscribePosition(closePositionTask.closeAll(closeAllPositionParams),
                          closeAllPositionParams.closeAllPositionsComposeData());
    }

    private final void subscribePosition(final Observable<OrderEvent> observable,
                                         final ComposeData composeData) {
        taskParamsUtil.subscribeComposeData(observable, composeData);
    }

    public PositionOrders positionOrders(final Instrument instrument) {
        checkNotNull(instrument);

        return positionUtil.positionOrders(instrument);
    }
}
