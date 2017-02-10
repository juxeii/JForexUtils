package com.jforex.programming.order.task.params.position;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.function.Function;

import com.dukascopy.api.IOrder;
import com.dukascopy.api.Instrument;
import com.jforex.programming.order.task.BatchMode;
import com.jforex.programming.order.task.CancelSLTPMode;
import com.jforex.programming.order.task.params.EmptyTaskParams;
import com.jforex.programming.order.task.params.TaskParamsBase;
import com.jforex.programming.order.task.params.TaskParamsType;
import com.jforex.programming.order.task.params.TaskParamsWithType;
import com.jforex.programming.order.task.params.basic.MergeParamsForPosition;

public class MergePositionParams extends TaskParamsWithType {

    private final Instrument instrument;
    private final MergeParamsForPosition mergeParamsForPosition;
    private final CancelSLTPMode mergeExecutionMode;
    private final BatchMode batchCancelSLMode;
    private final BatchMode batchCancelTPMode;
    private final TaskParamsBase cancelSLTPParams;
    private final TaskParamsBase batchCancelSLParams;
    private final TaskParamsBase batchCancelTPParams;
    private final Function<IOrder, TaskParamsBase> cancelSLParamsFactory;
    private final Function<IOrder, TaskParamsBase> cancelTPParamsFactory;

    private MergePositionParams(final Builder builder) {
        super(builder);

        instrument = builder.instrument;
        mergeParamsForPosition = builder.mergeParamsForPosition;
        mergeExecutionMode = builder.mergeExecutionMode;
        batchCancelSLMode = builder.batchCancelSLMode;
        batchCancelTPMode = builder.batchCancelTPMode;
        cancelSLTPParams = builder.cancelSLTPParams;
        batchCancelSLParams = builder.batchCancelSLParams;
        batchCancelTPParams = builder.batchCancelTPParams;
        cancelSLParamsFactory = builder.cancelSLParamsFactory;
        cancelTPParamsFactory = builder.cancelTPParamsFactory;
    }

    public Instrument instrument() {
        return instrument;
    }

    public MergeParamsForPosition mergeParamsForPosition() {
        return mergeParamsForPosition;
    }

    public TaskParamsBase cancelSLTPParams() {
        return cancelSLTPParams;
    }

    public TaskParamsBase batchCancelSLParams() {
        return batchCancelSLParams;
    }

    public TaskParamsBase batchCancelTPParams() {
        return batchCancelTPParams;
    }

    public Function<IOrder, TaskParamsBase> cancelSLParamsFactory() {
        return cancelSLParamsFactory;
    }

    public Function<IOrder, TaskParamsBase> cancelTPParamsFactory() {
        return cancelTPParamsFactory;
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
                                     final MergeParamsForPosition mergeParamsForPosition) {
        checkNotNull(instrument);
        checkNotNull(mergeParamsForPosition);

        return new Builder(instrument, mergeParamsForPosition);
    }

    public static class Builder extends TaskParamsBase.Builder<Builder> {

        private final Instrument instrument;
        private final MergeParamsForPosition mergeParamsForPosition;
        private CancelSLTPMode mergeExecutionMode = CancelSLTPMode.MergeCancelSLAndTP;
        private BatchMode batchCancelSLMode = BatchMode.MERGE;
        private BatchMode batchCancelTPMode = BatchMode.MERGE;
        private TaskParamsBase cancelSLTPParams;
        private TaskParamsBase batchCancelSLParams;
        private TaskParamsBase batchCancelTPParams;
        private Function<IOrder, TaskParamsBase> cancelSLParamsFactory;
        private Function<IOrder, TaskParamsBase> cancelTPParamsFactory;

        public Builder(final Instrument instrument,
                       final MergeParamsForPosition mergeParamsForPosition) {
            this.instrument = instrument;
            this.mergeParamsForPosition = mergeParamsForPosition;

            cancelSLTPParams = emptyParams();
            batchCancelSLParams = emptyParams();
            batchCancelTPParams = emptyParams();
            cancelSLParamsFactory = order -> emptyParams();
            cancelTPParamsFactory = order -> emptyParams();
        }

        private EmptyTaskParams emptyParams() {
            return EmptyTaskParams
                .newBuilder()
                .build();
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

        public Builder withCancelSLParamsFactory(final Function<IOrder, TaskParamsBase> cancelSLParamsFactory) {
            checkNotNull(cancelSLParamsFactory);

            this.cancelSLParamsFactory = cancelSLParamsFactory;
            return getThis();
        }

        public Builder withCancelTPParamsFactory(final Function<IOrder, TaskParamsBase> cancelTPParamsFactory) {
            checkNotNull(cancelTPParamsFactory);

            this.cancelTPParamsFactory = cancelTPParamsFactory;
            return getThis();
        }

        public MergePositionParams build() {
            return new MergePositionParams(this);
        }

        @Override
        protected Builder getThis() {
            return this;
        }
    }
}
