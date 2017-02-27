package com.jforex.programming.order;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.dukascopy.api.IOrder;
import com.dukascopy.api.Instrument;
import com.dukascopy.api.JFException;
import com.google.common.collect.ImmutableMap;
import com.jforex.programming.misc.Exposure;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.task.BasicTask;
import com.jforex.programming.order.task.ClosePositionTask;
import com.jforex.programming.order.task.MergePositionTask;
import com.jforex.programming.order.task.params.TaskParams;
import com.jforex.programming.order.task.params.TaskParamsBase;
import com.jforex.programming.order.task.params.TaskParamsType;
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
    private final PositionUtil positionUtil;
    private final TaskParamsUtil taskParamsUtil;
    private final Exposure exposure;
    private final Map<TaskParamsType, Function<TaskParams, Observable<OrderEvent>>> taskParamsMapper;

    public OrderUtil(final BasicTask basicTask,
                     final MergePositionTask mergePositionTask,
                     final ClosePositionTask closePositionTask,
                     final PositionUtil positionUtil,
                     final TaskParamsUtil taskParamsUtil,
                     final Exposure exposure) {
        this.basicTask = basicTask;
        this.positionUtil = positionUtil;
        this.taskParamsUtil = taskParamsUtil;
        this.exposure = exposure;

        taskParamsMapper = ImmutableMap.<TaskParamsType, Function<TaskParams, Observable<OrderEvent>>> builder()
            .put(TaskParamsType.SUBMIT,
                 params -> submitWithExposureCheck((SubmitParams) params))
            .put(TaskParamsType.MERGE,
                 params -> basicTask.mergeOrders((MergeParams) params))
            .put(TaskParamsType.CLOSE,
                 params -> closeWithExposureCheck((CloseParams) params))
            .put(TaskParamsType.SETLABEL,
                 params -> basicTask.setLabel((SetLabelParams) params))
            .put(TaskParamsType.SETGTT,
                 params -> basicTask.setGoodTillTime((SetGTTParams) params))
            .put(TaskParamsType.SETAMOUNT,
                 params -> basicTask.setRequestedAmount((SetAmountParams) params))
            .put(TaskParamsType.SETOPENPRICE,
                 params -> basicTask.setOpenPrice((SetOpenPriceParams) params))
            .put(TaskParamsType.SETSL,
                 params -> basicTask.setStopLossPrice((SetSLParams) params))
            .put(TaskParamsType.SETTP,
                 params -> basicTask.setTakeProfitPrice((SetTPParams) params))
            .put(TaskParamsType.MERGEPOSITION,
                 params -> mergePositionTask.merge((MergePositionParams) params))
            .put(TaskParamsType.MERGEALLPOSITIONS,
                 params -> mergePositionTask.mergeAll((MergeAllPositionsParams) params))
            .put(TaskParamsType.CLOSEPOSITION,
                 params -> closePositionTask.close((ClosePositionParams) params))
            .put(TaskParamsType.CLOSEALLPOSITIONS,
                 params -> closePositionTask.closeAll((CloseAllPositionsParams) params))
            .build();
    }

    private Observable<OrderEvent> submitWithExposureCheck(final SubmitParams submitParams) {
        final OrderParams orderParams = submitParams.orderParams();
        final double signedAmount = OrderStaticUtil.signedAmount(orderParams);
        final Instrument instrument = orderParams.instrument();
        positionUtil.create(instrument);

        final boolean wouldExceedAmount = exposure.wouldExceed(instrument, signedAmount);
        return wouldExceedAmount
                ? Observable.error(maxExposureException(instrument))
                : basicTask.submitOrder(submitParams);
    }

    private Observable<OrderEvent> closeWithExposureCheck(final CloseParams closeParams) {
        final IOrder order = closeParams.order();
        final double orderAmount = order.getAmount();
        final double partialCloseAmount = closeParams.partialCloseAmount();
        final double signedAmount = partialCloseAmount > 0
                ? -OrderStaticUtil.signedAmount(partialCloseAmount, order.getOrderCommand())
                : -OrderStaticUtil.signedAmount(orderAmount, order.getOrderCommand());
        final Instrument instrument = order.getInstrument();

        final boolean wouldExceedAmount = exposure.wouldExceed(instrument, signedAmount);
        return wouldExceedAmount
                ? Observable.error(maxExposureException(instrument))
                : basicTask.close(closeParams);
    }

    private JFException maxExposureException(final Instrument instrument) {
        return new JFException("Maximum exposure reached on " + instrument);
    }

    public Observable<OrderEvent> paramsToObservable(final TaskParams taskParams) {
        checkNotNull(taskParams);

        final Observable<OrderEvent> observable = taskParamsToObservable(taskParams);
        return taskParamsUtil.compose(observable, (TaskParamsBase) taskParams);
    }

    public void execute(final TaskParams taskParams) {
        checkNotNull(taskParams);

        final Observable<OrderEvent> observable = taskParamsToObservable(taskParams);
        taskParamsUtil.composeAndSubscribe(observable, (TaskParamsBase) taskParams);
    }

    public void executeBatch(final BatchParams batchParams) {
        checkNotNull(batchParams);

        final List<Observable<OrderEvent>> observables = batchParams
            .taskParams()
            .stream()
            .map(params -> taskParamsUtil.compose(taskParamsToObservable(params), (TaskParamsBase) params))
            .collect(Collectors.toList());

        taskParamsUtil.composeAndSubscribe(Observable.merge(observables), batchParams);
    }

    private final Observable<OrderEvent> taskParamsToObservable(final TaskParams taskParams) {
        return taskParamsMapper
            .get(taskParams.type())
            .apply(taskParams);
    }

    public PositionOrders positionOrders(final Instrument instrument) {
        checkNotNull(instrument);

        return positionUtil.positionOrders(instrument);
    }
}
