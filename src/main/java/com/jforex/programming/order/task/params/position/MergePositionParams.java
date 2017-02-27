package com.jforex.programming.order.task.params.position;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.function.Function;

import com.dukascopy.api.IOrder;
import com.dukascopy.api.Instrument;
import com.jforex.programming.order.task.BatchMode;
import com.jforex.programming.order.task.CancelSLTPMode;
import com.jforex.programming.order.task.params.TaskParamsBase;
import com.jforex.programming.order.task.params.TaskParamsType;
import com.jforex.programming.order.task.params.TaskParamsWithType;
import com.jforex.programming.order.task.params.basic.CancelSLParams;
import com.jforex.programming.order.task.params.basic.CancelTPParams;
import com.jforex.programming.order.task.params.basic.MergeParamsForPosition;

public class MergePositionParams extends TaskParamsWithType {

    private final Instrument instrument;
    private final String mergeOrderLabel;
    private final MergeParamsForPosition mergeParamsForPosition;
    private final CancelSLTPMode mergeExecutionMode;
    private final BatchMode batchCancelSLMode;
    private final BatchMode batchCancelTPMode;
    private final TaskParamsBase cancelSLTPParams;
    private final TaskParamsBase batchCancelSLParams;
    private final TaskParamsBase batchCancelTPParams;
    private final Function<IOrder, CancelSLParams> cancelSLParamsFactory;
    private final Function<IOrder, CancelTPParams> cancelTPParamsFactory;

    private MergePositionParams(final Builder builder) {
        super(builder);

        instrument = builder.instrument;
        mergeOrderLabel = builder.mergeOrderLabel;
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

    public String mergeOrderLabel() {
        return mergeOrderLabel;
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

    public Function<IOrder, CancelSLParams> cancelSLParamsFactory() {
        return cancelSLParamsFactory;
    }

    public Function<IOrder, CancelTPParams> cancelTPParamsFactory() {
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
                                     final String mergeOrderLabel) {
        checkNotNull(instrument);
        checkNotNull(mergeOrderLabel);

        return new Builder(instrument, mergeOrderLabel);
    }

    public static class Builder extends TaskParamsBase.Builder<Builder> {

        private final Instrument instrument;
        private final String mergeOrderLabel;
        private MergeParamsForPosition mergeParamsForPosition;
        private CancelSLTPMode mergeExecutionMode = CancelSLTPMode.MergeCancelSLAndTP;
        private BatchMode batchCancelSLMode = BatchMode.MERGE;
        private BatchMode batchCancelTPMode = BatchMode.MERGE;
        private TaskParamsBase cancelSLTPParams;
        private TaskParamsBase batchCancelSLParams;
        private TaskParamsBase batchCancelTPParams;
        private Function<IOrder, CancelSLParams> cancelSLParamsFactory;
        private Function<IOrder, CancelTPParams> cancelTPParamsFactory;

        public Builder(final Instrument instrument,
                       final String mergeOrderLabel) {
            this.instrument = instrument;
            this.mergeOrderLabel = mergeOrderLabel;

            cancelSLTPParams = emptyParams();
            batchCancelSLParams = emptyParams();
            batchCancelTPParams = emptyParams();
            cancelSLParamsFactory = order -> CancelSLParams
                .withOrder(order)
                .build();
            cancelTPParamsFactory = order -> CancelTPParams
                .withOrder(order)
                .build();
            mergeParamsForPosition = MergeParamsForPosition
                .newBuilder()
                .build();
        }

        private TaskParamsBase emptyParams() {
            return TaskParamsBase
                .create()
                .build();
        }

        public Builder withMergeExecutionMode(final CancelSLTPMode mergeExecutionMode) {
            checkNotNull(mergeExecutionMode);

            this.mergeExecutionMode = mergeExecutionMode;
            return this;
        }

        public Builder withBatchCancelSLMode(final BatchMode batchCancelSLMode) {
            checkNotNull(batchCancelSLMode);

            this.batchCancelSLMode = batchCancelSLMode;
            return this;
        }

        public Builder withBatchCancelTPMode(final BatchMode batchCancelTPMode) {
            checkNotNull(batchCancelTPMode);

            this.batchCancelTPMode = batchCancelTPMode;
            return this;
        }

        public Builder withCancelSLTPParams(final TaskParamsBase cancelSLTPParams) {
            checkNotNull(cancelSLTPParams);

            this.cancelSLTPParams = cancelSLTPParams;
            return this;
        }

        public Builder withBatchCancelSLParams(final TaskParamsBase batchCancelSLParams) {
            checkNotNull(batchCancelSLParams);

            this.batchCancelSLParams = batchCancelSLParams;
            return this;
        }

        public Builder withBatchCancelTPParams(final TaskParamsBase batchCancelTPParams) {
            checkNotNull(batchCancelTPParams);

            this.batchCancelTPParams = batchCancelTPParams;
            return this;
        }

        public Builder withCancelSLParamsFactory(final Function<IOrder, CancelSLParams> cancelSLParamsFactory) {
            checkNotNull(cancelSLParamsFactory);

            this.cancelSLParamsFactory = cancelSLParamsFactory;
            return this;
        }

        public Builder withCancelTPParamsFactory(final Function<IOrder, CancelTPParams> cancelTPParamsFactory) {
            checkNotNull(cancelTPParamsFactory);

            this.cancelTPParamsFactory = cancelTPParamsFactory;
            return this;
        }

        public Builder withMergeParamsForPosition(final MergeParamsForPosition mergeParamsForPosition) {
            checkNotNull(mergeParamsForPosition);

            this.mergeParamsForPosition = mergeParamsForPosition;
            return this;
        }

        @Override
        public MergePositionParams build() {
            return new MergePositionParams(this);
        }
    }
}
