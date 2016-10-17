package com.jforex.programming.object;

import com.dukascopy.api.IContext;
import com.dukascopy.api.IMessage;
import com.jforex.programming.misc.JFHotPublisher;
import com.jforex.programming.misc.StrategyThreadTask;
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

public class OrderObjects {

    private final PositionFactory positionFactory;
    private final PositionUtil positionUtil;
    private final OrderEventGateway orderEventGateway;
    private final StrategyThreadTask strategyThreadTask;
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

    public OrderObjects(final IContext context,
                        final JFHotPublisher<IMessage> messagePublisher,
                        final JFHotPublisher<OrderCallRequest> callRequestPublisher) {
        orderEventFactory = new OrderEventFactory(callRequestPublisher.observable());
        orderEventGateway = new OrderEventGateway(messagePublisher.observable(), orderEventFactory);
        strategyThreadTask = new StrategyThreadTask(context);
        positionFactory = new PositionFactory(orderEventGateway.observable());
        positionUtil = new PositionUtil(positionFactory);
        orderUtilHandler = new OrderUtilHandler(orderEventGateway,
                                                orderEventTypeDataFactory,
                                                callRequestPublisher);
        orderTaskExecutor = new TaskExecutor(strategyThreadTask, context.getEngine());
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
}
