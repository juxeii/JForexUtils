package com.jforex.programming.strategy;

import com.dukascopy.api.IEngine;
import com.dukascopy.api.IMessage;
import com.jforex.programming.math.CalculationUtil;
import com.jforex.programming.misc.Exposure;
import com.jforex.programming.misc.StrategyThreadRunner;
import com.jforex.programming.order.OrderUtil;
import com.jforex.programming.order.OrderUtilHandler;
import com.jforex.programming.order.call.OrderCallRequest;
import com.jforex.programming.order.event.OrderEventFactory;
import com.jforex.programming.order.event.OrderEventGateway;
import com.jforex.programming.order.event.OrderEventTypeDataFactory;
import com.jforex.programming.order.task.BasicTask;
import com.jforex.programming.order.task.BasicTaskForBatch;
import com.jforex.programming.order.task.BatchCancelSLTask;
import com.jforex.programming.order.task.BatchCancelTPTask;
import com.jforex.programming.order.task.BatchChangeTask;
import com.jforex.programming.order.task.BatchComposer;
import com.jforex.programming.order.task.BatchCreator;
import com.jforex.programming.order.task.CancelSLTPAndMergeTask;
import com.jforex.programming.order.task.CancelSLTPTask;
import com.jforex.programming.order.task.ClosePositionTask;
import com.jforex.programming.order.task.MergeAndClosePositionTask;
import com.jforex.programming.order.task.MergePositionTask;
import com.jforex.programming.order.task.OrdersForPositionClose;
import com.jforex.programming.order.task.TaskExecutor;
import com.jforex.programming.order.task.params.TaskParamsUtil;
import com.jforex.programming.position.PositionFactory;
import com.jforex.programming.position.PositionUtil;
import com.jforex.programming.rx.JFHotPublisher;

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
    private final BasicTask orderBasicTask;
    private final BatchCreator batchCreator = new BatchCreator();
    private final BasicTaskForBatch basicTaskForBatch;
    private final BatchComposer batchComposer;
    private final BatchChangeTask batchChangeTask;
    private final MergePositionTask orderMergeTask;
    private final ClosePositionTask orderCloseTask;
    private final TaskParamsUtil taskParamsUtil = new TaskParamsUtil();
    private final OrdersForPositionClose ordersForPositionClose;
    private final MergeAndClosePositionTask mergeAndClosePositionTask;
    private final CancelSLTPAndMergeTask cancelSLTPAndMergeTask;
    private final BatchCancelSLTask cancelSLTask;
    private final BatchCancelTPTask cancelTPTask;
    private final CancelSLTPTask cancelSLTPTask;
    private final Exposure exposure;
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
        orderBasicTask = new BasicTask(orderTaskExecutor,
                                       orderUtilHandler,
                                       calculationUtil);
        basicTaskForBatch = new BasicTaskForBatch(orderBasicTask);
        batchComposer = new BatchComposer(taskParamsUtil, basicTaskForBatch);
        batchChangeTask = new BatchChangeTask(batchComposer, batchCreator);
        cancelSLTask = new BatchCancelSLTask(batchChangeTask, taskParamsUtil);
        cancelTPTask = new BatchCancelTPTask(batchChangeTask, taskParamsUtil);
        cancelSLTPTask = new CancelSLTPTask(cancelSLTask, cancelTPTask);
        cancelSLTPAndMergeTask = new CancelSLTPAndMergeTask(cancelSLTPTask,
                                                            orderBasicTask,
                                                            taskParamsUtil);
        orderMergeTask = new MergePositionTask(cancelSLTPAndMergeTask, positionUtil);
        ordersForPositionClose = new OrdersForPositionClose(positionUtil);
        mergeAndClosePositionTask = new MergeAndClosePositionTask(orderMergeTask,
                                                                  batchChangeTask,
                                                                  ordersForPositionClose);
        orderCloseTask = new ClosePositionTask(mergeAndClosePositionTask, positionUtil);
        exposure = new Exposure(engine);
        orderUtil = new OrderUtil(orderBasicTask,
                                  orderMergeTask,
                                  orderCloseTask,
                                  positionUtil,
                                  taskParamsUtil,
                                  exposure);
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
                positionFactory.forInstrument(order.getInstrument());
                orderEventGateway.importOrder(order);
            })
            .ignoreElements();
    }
}
