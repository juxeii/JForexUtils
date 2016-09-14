package com.jforex.programming.order.command;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.jforex.programming.order.event.OrderEventTypeMapper.changeDoneByReason;
import static com.jforex.programming.order.event.OrderEventTypeMapper.changeRejectEventByReason;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.call.OrderCallReason;
import com.jforex.programming.order.command.option.ChangeOption;
import com.jforex.programming.order.command.option.CloseOption;
import com.jforex.programming.order.command.option.MergeOption;
import com.jforex.programming.order.command.option.SubmitOption;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.event.OrderEventType;
import com.jforex.programming.order.event.OrderEventTypeData;

import io.reactivex.functions.Action;

public class Command {

    private final Action startAction;
    private final Action completeAction;
    private final Consumer<Throwable> errorAction;
    private final Consumer<OrderEvent> eventAction;
    private final Callable<IOrder> callable;
    private final OrderCallReason callReason;
    private final OrderEventTypeData orderEventTypeData;
    private final int noOfRetries;
    private final long retryDelayInMillis;
    private final Map<OrderEventType, Consumer<IOrder>> eventHandlerForType;

    public Command(final CommonBuilder<?> builder) {
        callable = builder.callable;
        callReason = builder.callReason;
        orderEventTypeData = builder.orderEventTypeData;
        startAction = builder.startAction;
        completeAction = builder.completedAction;
        eventAction = builder.eventAction;
        errorAction = builder.errorAction;
        noOfRetries = builder.noOfRetries;
        retryDelayInMillis = builder.retryDelayInMillis;
        eventHandlerForType = builder.eventHandlerForType;
    }

    public Action startAction() {
        return startAction;
    }

    public Action completedAction() {
        return completeAction;
    }

    public Consumer<OrderEvent> eventAction() {
        return eventAction;
    }

    public Consumer<Throwable> errorAction() {
        return errorAction;
    }

    public Callable<IOrder> callable() {
        return callable;
    }

    public OrderCallReason callReason() {
        return callReason;
    }

    public boolean isEventTypeForCommand(final OrderEventType orderEventType) {
        return orderEventTypeData
            .allEventTypes()
            .contains(orderEventType);
    }

    private boolean isDoneEventType(final OrderEventType orderEventType) {
        return orderEventTypeData
            .doneEventTypes()
            .contains(orderEventType);
    }

    public boolean isRejectEventType(final OrderEventType orderEventType) {
        return orderEventTypeData
            .rejectEventTypes()
            .contains(orderEventType);
    }

    public boolean isFinishEventType(final OrderEventType orderEventType) {
        return isDoneEventType(orderEventType) || isRejectEventType(orderEventType);
    }

    public int noOfRetries() {
        return noOfRetries;
    }

    public long retryDelayInMillis() {
        return retryDelayInMillis;
    }

    public Map<OrderEventType, Consumer<IOrder>> eventHandlerForType() {
        return eventHandlerForType;
    }

    public static final SubmitOption forSubmit(final Callable<IOrder> callable,
                                               final OrderEventTypeData orderEventTypeData) {
        return new SubmitBuilder(callable, orderEventTypeData);
    }

    private static class SubmitBuilder extends CommonBuilder<SubmitOption>
            implements SubmitOption {

        private SubmitBuilder(final Callable<IOrder> callable,
                              final OrderEventTypeData orderEventTypeData) {
            this.callable = callable;
            this.callReason = OrderCallReason.SUBMIT;
            this.orderEventTypeData = orderEventTypeData;
        }

        @Override
        public SubmitOption doOnSubmit(final Consumer<IOrder> submitAction) {
            eventHandlerForType.put(OrderEventType.SUBMIT_OK, checkNotNull(submitAction));
            eventHandlerForType.put(OrderEventType.SUBMIT_CONDITIONAL_OK, checkNotNull(submitAction));
            return this;
        }

        @Override
        public SubmitOption doOnSubmitReject(final Consumer<IOrder> submitRejectAction) {
            return registerTypeHandler(OrderEventType.SUBMIT_REJECTED, submitRejectAction);
        }

        @Override
        public SubmitOption doOnFillReject(final Consumer<IOrder> fillRejectAction) {
            return registerTypeHandler(OrderEventType.FILL_REJECTED, fillRejectAction);
        }

        @Override
        public SubmitOption doOnPartialFill(final Consumer<IOrder> partialFillAction) {
            return registerTypeHandler(OrderEventType.PARTIAL_FILL_OK, partialFillAction);
        }

        @Override
        public SubmitOption doOnFill(final Consumer<IOrder> fillAction) {
            return registerTypeHandler(OrderEventType.FULLY_FILLED, fillAction);
        }

        @Override
        public Command build() {
            return new Command(this);
        }
    }

    public static final MergeOption forMerge(final Callable<IOrder> callable,
                                             final OrderEventTypeData orderEventTypeData) {
        return new MergeBuilder(callable, orderEventTypeData);
    }

    private static class MergeBuilder extends CommonBuilder<MergeOption>
            implements MergeOption {

        private MergeBuilder(final Callable<IOrder> callable,
                             final OrderEventTypeData orderEventTypeData) {
            this.callable = callable;
            this.callReason = OrderCallReason.MERGE;
            this.orderEventTypeData = orderEventTypeData;
        }

        @Override
        public MergeOption doOnMergeReject(final Consumer<IOrder> rejectAction) {
            return registerTypeHandler(OrderEventType.MERGE_REJECTED, rejectAction);
        }

        @Override
        public MergeOption doOnMergeClose(final Consumer<IOrder> mergeCloseAction) {
            return registerTypeHandler(OrderEventType.MERGE_CLOSE_OK, mergeCloseAction);
        }

        @Override
        public MergeOption doOnMerge(final Consumer<IOrder> doneAction) {
            return registerTypeHandler(OrderEventType.MERGE_OK, doneAction);
        }

        @Override
        public Command build() {
            return new Command(this);
        }
    }

    public static final CloseOption forClose(final Callable<IOrder> callable,
                                             final OrderEventTypeData orderEventTypeData) {
        return new CloseBuilder(callable, orderEventTypeData);
    }

    private static class CloseBuilder extends CommonBuilder<CloseOption>
            implements CloseOption {

        private CloseBuilder(final Callable<IOrder> callable,
                             final OrderEventTypeData orderEventTypeData) {
            this.callable = callable;
            this.callReason = OrderCallReason.CLOSE;
            this.orderEventTypeData = orderEventTypeData;
        }

        @Override
        public CloseOption doOnCloseReject(final Consumer<IOrder> rejectAction) {
            return registerTypeHandler(OrderEventType.CLOSE_REJECTED, rejectAction);
        }

        @Override
        public CloseOption doOnPartialClose(final Consumer<IOrder> partialCloseAction) {
            return registerTypeHandler(OrderEventType.PARTIAL_CLOSE_OK, partialCloseAction);
        }

        @Override
        public CloseOption doOnClose(final Consumer<IOrder> doneAction) {
            return registerTypeHandler(OrderEventType.CLOSE_OK, doneAction);
        }

        @Override
        public Command build() {
            return new Command(this);
        }
    }

    public static final ChangeOption forChange(final Callable<IOrder> callable,
                                               final OrderCallReason callReason,
                                               final OrderEventTypeData orderEventTypeData) {
        return new ChangeBuilder(callable,
                                 callReason,
                                 orderEventTypeData);
    }

    private static class ChangeBuilder extends CommonBuilder<ChangeOption>
            implements ChangeOption {

        private ChangeBuilder(final Callable<IOrder> callable,
                              final OrderCallReason callReason,
                              final OrderEventTypeData orderEventTypeData) {
            this.callable = callable;
            this.callReason = callReason;
            this.orderEventTypeData = orderEventTypeData;
        }

        @Override
        public ChangeOption doOnReject(final Consumer<IOrder> rejectConsumer) {
            eventHandlerForType.put(changeRejectEventByReason.get(callReason),
                                    checkNotNull(rejectConsumer));
            return this;
        }

        @Override
        public ChangeOption doOnChange(final Consumer<IOrder> changeConsumer) {
            eventHandlerForType.put(changeDoneByReason.get(callReason),
                                    checkNotNull(changeConsumer));
            return this;
        }

        @Override
        public Command build() {
            return new Command(this);
        }
    }
}
