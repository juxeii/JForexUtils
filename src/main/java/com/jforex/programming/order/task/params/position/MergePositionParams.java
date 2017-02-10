package com.jforex.programming.order.task.params.position;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.function.Consumer;
import java.util.function.Function;

import com.dukascopy.api.IOrder;
import com.dukascopy.api.Instrument;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.event.OrderEventType;
import com.jforex.programming.order.task.BatchMode;
import com.jforex.programming.order.task.CancelSLTPMode;
import com.jforex.programming.order.task.params.ComposeData;
import com.jforex.programming.order.task.params.EmptyTaskParams;
import com.jforex.programming.order.task.params.TaskParamsBase;
import com.jforex.programming.order.task.params.TaskParamsType;
import com.jforex.programming.order.task.params.TaskParamsWithType;

public class MergePositionParams extends TaskParamsWithType {

    private final Instrument instrument;
    private final String mergeOrderLabel;

    private final TaskParamsBase mergePositionComposeData;
    private final TaskParamsBase cancelSLTPComposeData;
    private final TaskParamsBase batchCancelSLComposeData;
    private final TaskParamsBase batchCancelTPComposeData;
    private final TaskParamsBase mergeComposeData;
    private final Function<IOrder, TaskParamsBase> cancelSLComposeData;
    private final Function<IOrder, TaskParamsBase> cancelTPComposeData;

    private final CancelSLTPMode mergeExecutionMode;
    private final BatchMode batchCancelSLMode;
    private final BatchMode batchCancelTPMode;

    private MergePositionParams(final Builder builder) {
        super(builder);

        instrument = builder.instrument;
        mergeOrderLabel = builder.mergeOrderLabel;

        mergePositionComposeData = builder.mergePositionComposeData;
        cancelSLTPComposeData = builder.cancelSLTPComposeData;
        batchCancelSLComposeData = builder.batchCancelSLComposeData;
        batchCancelTPComposeData = builder.batchCancelTPComposeData;
        cancelSLComposeData = builder.cancelSLComposeData;
        cancelTPComposeData = builder.cancelTPComposeData;
        mergeComposeData = builder.mergeComposeData;

        mergeExecutionMode = builder.mergeExecutionMode;
        batchCancelSLMode = builder.batchCancelSLMode;
        batchCancelTPMode = builder.batchCancelTPMode;
    }

    public Instrument instrument() {
        return instrument;
    }

    public String mergeOrderLabel() {
        return mergeOrderLabel;
    }

    public ComposeData mergeComposeData() {
        return mergeComposeData.composeData();
    }

    public ComposeData cancelSLTPComposeData() {
        return cancelSLTPComposeData.composeData();
    }

    public ComposeData batchCancelSLComposeData() {
        return batchCancelSLComposeData.composeData();
    }

    public ComposeData batchCancelTPComposeData() {
        return batchCancelTPComposeData.composeData();
    }

    public ComposeData cancelSLComposeData(final IOrder order) {
        return cancelSLComposeData
            .apply(order)
            .composeData();
    }

    public ComposeData cancelTPComposeData(final IOrder order) {
        return cancelTPComposeData
            .apply(order)
            .composeData();
    }

    public CancelSLTPMode mergeExecutionMode() {
        return mergeExecutionMode;
    }

    public BatchMode batchCancelSLMode() {
        return batchCancelSLMode;
    }

    public BatchMode batchCancelTPMode() {
        return batchCancelTPMode;
    }

    @Override
    public TaskParamsType type() {
        return TaskParamsType.MERGEPOSITION;
    }

    @Override
    public ComposeData composeData() {
        return mergePositionComposeData.composeData();
    }

    public static Builder newBuilder(final Instrument instrument,
                                     final String mergeOrderLabel) {
        checkNotNull(instrument);
        checkNotNull(mergeOrderLabel);

        return new Builder(instrument, mergeOrderLabel);
    }

    public static class Builder extends TaskParamsBase.Builder<Builder> {

        private final Instrument instrument;
        private final String mergeOrderLabel;

        private TaskParamsBase mergePositionComposeData = EmptyTaskParams.newBuilder().build();
        private TaskParamsBase cancelSLTPComposeData = EmptyTaskParams.newBuilder().build();
        private TaskParamsBase batchCancelSLComposeData = EmptyTaskParams.newBuilder().build();
        private TaskParamsBase batchCancelTPComposeData = EmptyTaskParams.newBuilder().build();
        private TaskParamsBase mergeComposeData = EmptyTaskParams.newBuilder().build();
        private Function<IOrder, TaskParamsBase> cancelSLComposeData =
                order -> EmptyTaskParams.newBuilder().build();
        private Function<IOrder, TaskParamsBase> cancelTPComposeData =
                order -> EmptyTaskParams.newBuilder().build();
        private CancelSLTPMode mergeExecutionMode = CancelSLTPMode.MergeCancelSLAndTP;
        private BatchMode batchCancelSLMode = BatchMode.MERGE;
        private BatchMode batchCancelTPMode = BatchMode.MERGE;

        public Builder(final Instrument instrument,
                       final String mergeOrderLabel) {
            this.instrument = instrument;
            this.mergeOrderLabel = mergeOrderLabel;
        }

        public Builder withMergeExecutionMode(final CancelSLTPMode mergeExecutionMode) {
            checkNotNull(mergeExecutionMode);

            this.mergeExecutionMode = mergeExecutionMode;
            return getThis();
        }

        public Builder withBatchCancelSLMode(final BatchMode batchCancelSLMode) {
            checkNotNull(batchCancelSLMode);

            this.batchCancelSLMode = batchCancelSLMode;
            return getThis();
        }

        public Builder withBatchCancelTPMode(final BatchMode batchCancelTPMode) {
            checkNotNull(batchCancelTPMode);

            this.batchCancelTPMode = batchCancelTPMode;
            return getThis();
        }

        public Builder withMergePositonParams(final TaskParamsBase mergePositionComposeData) {
            checkNotNull(mergePositionComposeData);

            this.mergePositionComposeData = mergePositionComposeData;
            return getThis();
        }

        public Builder withCancelSLTPParams(final TaskParamsBase cancelSLTPComposeData) {
            checkNotNull(cancelSLTPComposeData);

            this.cancelSLTPComposeData = cancelSLTPComposeData;
            return getThis();
        }

        public Builder withBatchCancelSLParams(final TaskParamsBase batchCancelSLComposeData) {
            checkNotNull(batchCancelSLComposeData);

            this.batchCancelSLComposeData = batchCancelSLComposeData;
            return getThis();
        }

        public Builder withBatchCancelTPParams(final TaskParamsBase batchCancelTPComposeData) {
            checkNotNull(batchCancelTPComposeData);

            this.batchCancelTPComposeData = batchCancelTPComposeData;
            return getThis();
        }

        public Builder withCancelSLParams(final Function<IOrder, TaskParamsBase> cancelSLComposeData) {
            checkNotNull(cancelSLComposeData);

            this.cancelSLComposeData = cancelSLComposeData;
            return getThis();
        }

        public Builder withCancelTPParams(final Function<IOrder, TaskParamsBase> cancelTPComposeData) {
            checkNotNull(cancelTPComposeData);

            this.cancelTPComposeData = cancelTPComposeData;
            return getThis();
        }

        public Builder withMergeParams(final TaskParamsBase mergeComposeData) {
            checkNotNull(mergeComposeData);

            this.mergeComposeData = mergeComposeData;
            return getThis();
        }

        public Builder doOnMerge(final Consumer<OrderEvent> mergeConsumer) {
            return setEventConsumer(OrderEventType.MERGE_OK, mergeConsumer);
        }

        public Builder doOnMergeClose(final Consumer<OrderEvent> mergeCloseConsumer) {
            return setEventConsumer(OrderEventType.MERGE_CLOSE_OK, mergeCloseConsumer);
        }

        public Builder doOnMergeReject(final Consumer<OrderEvent> rejectConsumer) {
            return setEventConsumer(OrderEventType.MERGE_REJECTED, rejectConsumer);
        }

        public Builder doOnCancelSL(final Consumer<OrderEvent> cancelSLConsumer) {
            return setEventConsumer(OrderEventType.CHANGED_SL, cancelSLConsumer);
        }

        public Builder doOnCancelSLReject(final Consumer<OrderEvent> cancelSLRejectConsumer) {
            return setEventConsumer(OrderEventType.CHANGE_SL_REJECTED, cancelSLRejectConsumer);
        }

        public Builder doOnCancelTP(final Consumer<OrderEvent> cancelTPConsumer) {
            return setEventConsumer(OrderEventType.CHANGED_TP, cancelTPConsumer);
        }

        public Builder doOnCancelTPReject(final Consumer<OrderEvent> cancelTPRejectConsumer) {
            return setEventConsumer(OrderEventType.CHANGE_TP_REJECTED, cancelTPRejectConsumer);
        }

        public MergePositionParams build() {
            return new MergePositionParams(this);
        }

        @Override
        public Builder getThis() {
            return this;
        }
    }
}
