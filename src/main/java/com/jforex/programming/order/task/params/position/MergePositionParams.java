package com.jforex.programming.order.task.params.position;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.function.Function;

import com.dukascopy.api.IOrder;
import com.dukascopy.api.Instrument;
import com.jforex.programming.order.task.BatchMode;
import com.jforex.programming.order.task.CancelSLTPMode;
import com.jforex.programming.order.task.params.ComposeData;
import com.jforex.programming.order.task.params.EmptyTaskParams;
import com.jforex.programming.order.task.params.TaskParamsBase;
import com.jforex.programming.order.task.params.TaskParamsType;
import com.jforex.programming.order.task.params.TaskParamsWithType;
import com.jforex.programming.order.task.params.basic.MergeParams;

public class MergePositionParams extends TaskParamsWithType {

    private final Instrument instrument;
    private final String mergeOrderLabel;

    private final TaskParamsBase cancelSLTPParams;
    private final TaskParamsBase batchCancelSLParams;
    private final TaskParamsBase batchCancelTPParams;
    private final TaskParamsBase mergeParams;
    private final Function<IOrder, TaskParamsBase> cancelSLParamsFactory;
    private final Function<IOrder, TaskParamsBase> cancelTPParamsFactory;

    private final CancelSLTPMode mergeExecutionMode;
    private final BatchMode batchCancelSLMode;
    private final BatchMode batchCancelTPMode;

    private MergePositionParams(final Builder builder) {
        super(builder);

        instrument = builder.instrument;
        mergeOrderLabel = builder.mergeOrderLabel;

        cancelSLTPParams = builder.cancelSLTPParams;
        batchCancelSLParams = builder.batchCancelSLParams;
        batchCancelTPParams = builder.batchCancelTPParams;
        cancelSLParamsFactory = builder.cancelSLParamsFactory;
        cancelTPParamsFactory = builder.cancelTPParamsFactory;
        mergeParams = builder.mergeParams;

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
        return mergeParams.composeData();
    }

    public ComposeData cancelSLTPComposeData() {
        return cancelSLTPParams.composeData();
    }

    public ComposeData batchCancelSLComposeData() {
        return batchCancelSLParams.composeData();
    }

    public ComposeData batchCancelTPComposeData() {
        return batchCancelTPParams.composeData();
    }

    public ComposeData createCancelSLComposeData(final IOrder order) {
        return cancelSLParamsFactory
            .apply(order)
            .composeData();
    }

    public ComposeData createCancelTPComposeData(final IOrder order) {
        return cancelTPParamsFactory
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

    public static Builder newBuilder(final Instrument instrument,
                                     final String mergeOrderLabel) {
        checkNotNull(instrument);
        checkNotNull(mergeOrderLabel);

        return new Builder(instrument, mergeOrderLabel);
    }

    public static class Builder extends TaskParamsBase.Builder<Builder> {

        private final Instrument instrument;
        private final String mergeOrderLabel;

        private TaskParamsBase cancelSLTPParams = EmptyTaskParams.newBuilder().build();
        private TaskParamsBase batchCancelSLParams = EmptyTaskParams.newBuilder().build();
        private TaskParamsBase batchCancelTPParams = EmptyTaskParams.newBuilder().build();
        private TaskParamsBase mergeParams = EmptyTaskParams.newBuilder().build();
        private Function<IOrder, TaskParamsBase> cancelSLParamsFactory =
                order -> EmptyTaskParams.newBuilder().build();
        private Function<IOrder, TaskParamsBase> cancelTPParamsFactory =
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

        public Builder withCancelSLTPParams(final TaskParamsBase cancelSLTPParams) {
            checkNotNull(cancelSLTPParams);

            this.cancelSLTPParams = cancelSLTPParams;
            return getThis();
        }

        public Builder withBatchCancelSLParams(final TaskParamsBase batchCancelSLParams) {
            checkNotNull(batchCancelSLParams);

            this.batchCancelSLParams = batchCancelSLParams;
            return getThis();
        }

        public Builder withBatchCancelTPParams(final TaskParamsBase batchCancelTPParams) {
            checkNotNull(batchCancelTPParams);

            this.batchCancelTPParams = batchCancelTPParams;
            return getThis();
        }

        public Builder withCancelSLParams(final Function<IOrder, TaskParamsBase> cancelSLParamsFactory) {
            checkNotNull(cancelSLParamsFactory);

            this.cancelSLParamsFactory = cancelSLParamsFactory;
            return getThis();
        }

        public Builder withCancelTPParams(final Function<IOrder, TaskParamsBase> cancelTPParamsFactory) {
            checkNotNull(cancelTPParamsFactory);

            this.cancelTPParamsFactory = cancelTPParamsFactory;
            return getThis();
        }

        public Builder withMergeParams(final MergeParams mergeParams) {
            checkNotNull(mergeParams);

            this.mergeParams = mergeParams;
            return getThis();
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
