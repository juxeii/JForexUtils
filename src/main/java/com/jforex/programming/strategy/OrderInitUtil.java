package com.jforex.programming.strategy;

import com.dukascopy.api.IEngine;
import com.dukascopy.api.IMessage;
import com.jforex.programming.math.CalculationUtil;
import com.jforex.programming.misc.JFHotPublisher;
import com.jforex.programming.misc.StrategyThreadRunner;
import com.jforex.programming.order.OrderUtil;
import com.jforex.programming.order.OrderUtilHandler;
import com.jforex.programming.order.call.OrderCallRequest;
import com.jforex.programming.order.event.OrderEventFactory;
import com.jforex.programming.order.event.OrderEventGateway;
import com.jforex.programming.order.event.OrderEventTypeDataFactory;
import com.jforex.programming.order.task.BasicTaskObservable;
import com.jforex.programming.order.task.BatchChangeTask;
import com.jforex.programming.order.task.CancelSLTPAndMergeTask;
import com.jforex.programming.order.task.CancelSLTPTask;
import com.jforex.programming.order.task.CancelSLTask;
import com.jforex.programming.order.task.CancelTPTask;
import com.jforex.programming.order.task.ClosePositionTaskObservable;
import com.jforex.programming.order.task.MergePositionTaskObservable;
import com.jforex.programming.order.task.TaskExecutor;
import com.jforex.programming.order.task.params.TaskParamsUtil;
import com.jforex.programming.order.task.params.position.ClosePositionParamsHandler;
import com.jforex.programming.order.task.params.position.MergePositionParamsHandler;
import com.jforex.programming.position.PositionFactory;
import com.jforex.programming.position.PositionUtil;

import io.reactivex.Completable;
import io.reactivex.Observable;

public class OrderInitUtil {

    private final IEngine engine;
    private final PositionFactory positionFactory;
    private final PositionUtil positionUtil;
    private final OrderEventGateway orderEventGateway;
    private final StrategyThreadRunner strategyThreadRunner;
    private final TaskExecutor orderTaskExecutor;
    private final OrderUtilHandler orderUtilHandler;
    private final BasicTaskObservable orderBasicTask;
    private final BatchChangeTask batchChangeTask;
    private final MergePositionTaskObservable orderMergeTask;
    private final ClosePositionTaskObservable orderCloseTask;
    private final TaskParamsUtil taskParamsUtil = new TaskParamsUtil();
    private final MergePositionParamsHandler mergeParamsHandler;
    private final ClosePositionParamsHandler closePositionParamsHandler;
    private final CancelSLTPAndMergeTask cancelSLTPAndMergeTask;
    private final CancelSLTask cancelSLTask;
    private final CancelTPTask cancelTPTask;
    private final CancelSLTPTask cancelSLTPTask;
    private final OrderUtil orderUtil;
    private final OrderEventFactory orderEventFactory;
    private final OrderEventTypeDataFactory orderEventTypeDataFactory = new OrderEventTypeDataFactory();
    private final JFHotPublisher<OrderCallRequest> callRequestPublisher = new JFHotPublisher<>();

    public OrderInitUtil(final ContextUtil contextUtil,
                         final Observable<IMessage> messageObservable,
                         final CalculationUtil calculationUtil) {
        engine = contextUtil.engine();
        orderEventFactory = new OrderEventFactory(callRequestPublisher.observable());
        orderEventGateway = new OrderEventGateway(messageObservable, orderEventFactory);
        strategyThreadRunner = new StrategyThreadRunner(contextUtil.context());
        positionFactory = new PositionFactory(orderEventGateway.observable());
        positionUtil = new PositionUtil(positionFactory);
        orderUtilHandler = new OrderUtilHandler(orderEventGateway,
                                                orderEventTypeDataFactory,
                                                callRequestPublisher);
        orderTaskExecutor = new TaskExecutor(strategyThreadRunner, engine);
        orderBasicTask = new BasicTaskObservable(orderTaskExecutor,
                                                 orderUtilHandler,
                                                 calculationUtil);
        batchChangeTask = new BatchChangeTask(orderBasicTask, taskParamsUtil);
        cancelSLTask = new CancelSLTask(batchChangeTask, taskParamsUtil);
        cancelTPTask = new CancelTPTask(batchChangeTask, taskParamsUtil);
        cancelSLTPTask = new CancelSLTPTask(cancelSLTask,
                                            cancelTPTask,
                                            taskParamsUtil);
        mergeParamsHandler = new MergePositionParamsHandler(cancelSLTPTask,
                                                            orderBasicTask,
                                                            taskParamsUtil);
        cancelSLTPAndMergeTask = new CancelSLTPAndMergeTask(mergeParamsHandler);
        orderMergeTask = new MergePositionTaskObservable(cancelSLTPAndMergeTask, positionUtil);
        closePositionParamsHandler = new ClosePositionParamsHandler(orderMergeTask,
                                                                    batchChangeTask,
                                                                    positionUtil);
        orderCloseTask = new ClosePositionTaskObservable(closePositionParamsHandler, positionUtil);
        orderUtil = new OrderUtil(orderBasicTask,
                                  orderMergeTask,
                                  orderCloseTask,
                                  positionUtil,
                                  taskParamsUtil);
    }

    public OrderUtil orderUtil() {
        return orderUtil;
    }

    public PositionUtil positionUtil() {
        return positionUtil;
    }

    public StrategyThreadRunner strategyThreadRunner() {
        return strategyThreadRunner;
    }

    public void onStop() {
        callRequestPublisher.unsubscribe();
    }

    public Completable importOrders() {
        return Observable
            .fromCallable(() -> engine.getOrders())
            .flatMap(Observable::fromIterable)
            .doOnNext(order -> {
                positionFactory.createForInstrument(order.getInstrument());
                orderEventGateway.importOrder(order);
            })
            .ignoreElements();
    }
}
