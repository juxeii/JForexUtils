package com.jforex.programming.init;

import com.dukascopy.api.IEngine;
import com.dukascopy.api.IMessage;
import com.jforex.programming.misc.JFHotPublisher;
import com.jforex.programming.misc.StrategyThreadRunner;
import com.jforex.programming.order.OrderUtil;
import com.jforex.programming.order.OrderUtilHandler;
import com.jforex.programming.order.call.OrderCallRequest;
import com.jforex.programming.order.command.ClosePositionCommandHandler;
import com.jforex.programming.order.command.MergeCommandHandler;
import com.jforex.programming.order.event.OrderEventFactory;
import com.jforex.programming.order.event.OrderEventGateway;
import com.jforex.programming.order.event.OrderEventTypeDataFactory;
import com.jforex.programming.order.task.BasicTask;
import com.jforex.programming.order.task.BatchChangeTask;
import com.jforex.programming.order.task.CancelSLTPAndMergeTask;
import com.jforex.programming.order.task.CancelSLTPTask;
import com.jforex.programming.order.task.CancelSLTask;
import com.jforex.programming.order.task.CancelTPTask;
import com.jforex.programming.order.task.CloseTask;
import com.jforex.programming.order.task.MergeTask;
import com.jforex.programming.order.task.TaskExecutor;
import com.jforex.programming.position.PositionFactory;
import com.jforex.programming.position.PositionUtil;

import io.reactivex.Completable;
import io.reactivex.Observable;

public class OrderInitUtil {

    private final IEngine engine;
    private final PositionFactory positionFactory;
    private final PositionUtil positionUtil;
    private final OrderEventGateway orderEventGateway;
    private final StrategyThreadRunner strategyThreadTask;
    private final TaskExecutor orderTaskExecutor;
    private final OrderUtilHandler orderUtilHandler;
    private final BasicTask orderBasicTask;
    private final BatchChangeTask orderChangeBatch;
    private final MergeTask orderMergeTask;
    private final CloseTask orderCloseTask;
    private final MergeCommandHandler mergeCommandHandler;
    private final ClosePositionCommandHandler closePositionCommandHandler;
    private final CancelSLTPAndMergeTask cancelSLTPAndMergeTask;
    private final CancelSLTPTask orderCancelSLAndTP;
    private final CancelSLTask orderCancelSL;
    private final CancelTPTask orderCancelTP;
    private final OrderUtil orderUtil;
    private final OrderEventFactory orderEventFactory;
    private final OrderEventTypeDataFactory orderEventTypeDataFactory = new OrderEventTypeDataFactory();
    private final JFHotPublisher<OrderCallRequest> callRequestPublisher = new JFHotPublisher<>();

    public OrderInitUtil(final ContextUtil contextUtil,
                         final Observable<IMessage> messageObservable) {
        engine = contextUtil.engine();
        orderEventFactory = new OrderEventFactory(callRequestPublisher.observable());
        orderEventGateway = new OrderEventGateway(messageObservable, orderEventFactory);
        strategyThreadTask = new StrategyThreadRunner(contextUtil.context());
        positionFactory = new PositionFactory(orderEventGateway.observable());
        positionUtil = new PositionUtil(positionFactory);
        orderUtilHandler = new OrderUtilHandler(orderEventGateway,
                                                orderEventTypeDataFactory,
                                                callRequestPublisher);
        orderTaskExecutor = new TaskExecutor(strategyThreadTask, engine);
        orderBasicTask = new BasicTask(orderTaskExecutor, orderUtilHandler);
        orderChangeBatch = new BatchChangeTask(orderBasicTask);
        orderCancelSL = new CancelSLTask(orderChangeBatch);
        orderCancelTP = new CancelTPTask(orderChangeBatch);
        orderCancelSLAndTP = new CancelSLTPTask(orderCancelSL, orderCancelTP);
        mergeCommandHandler = new MergeCommandHandler(orderCancelSLAndTP, orderBasicTask);
        cancelSLTPAndMergeTask = new CancelSLTPAndMergeTask(mergeCommandHandler);
        orderMergeTask = new MergeTask(cancelSLTPAndMergeTask, positionUtil);
        closePositionCommandHandler = new ClosePositionCommandHandler(orderMergeTask,
                                                                      orderChangeBatch,
                                                                      positionUtil);
        orderCloseTask = new CloseTask(closePositionCommandHandler, positionUtil);
        orderUtil = new OrderUtil(orderBasicTask,
                                  orderMergeTask,
                                  orderCloseTask,
                                  positionUtil);
    }

    public OrderUtil orderUtil() {
        return orderUtil;
    }

    public PositionUtil positionUtil() {
        return positionUtil;
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
