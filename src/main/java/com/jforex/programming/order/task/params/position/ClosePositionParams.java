package com.jforex.programming.order.task.params.position;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.function.Function;

import com.dukascopy.api.IOrder;
import com.dukascopy.api.Instrument;
import com.jforex.programming.order.task.BatchMode;
import com.jforex.programming.order.task.CloseExecutionMode;
import com.jforex.programming.order.task.params.TaskParamsBase;
import com.jforex.programming.order.task.params.TaskParamsType;
import com.jforex.programming.order.task.params.TaskParamsWithType;
import com.jforex.programming.order.task.params.basic.CloseParams;

public class ClosePositionParams extends TaskParamsWithType {

    private final Instrument instrument;
    private final MergePositionParams mergePositionParams;
    private final Function<IOrder, CloseParams> closeParamsFactory;
    private final CloseExecutionMode closeExecutionMode;
    private final BatchMode closeBatchMode;

    private ClosePositionParams(final Builder builder) {
        super(builder);

        mergePositionParams = builder.mergePositionParams;
        closeParamsFactory = builder.closeParamsFactory;
        instrument = builder.instrument;
        closeExecutionMode = builder.closeExecutionMode;
        closeBatchMode = builder.closeBatchMode;
    }

    public MergePositionParams mergePositionParams() {
        return mergePositionParams;
    }

    public Function<IOrder, CloseParams> closeParamsFactory() {
        return closeParamsFactory;
    }

    public Instrument instrument() {
        return instrument;
    }

    public CloseExecutionMode closeExecutionMode() {
        return closeExecutionMode;
    }

    public BatchMode closeBatchMode() {
        return closeBatchMode;
    }

    @Override
    public TaskParamsType type() {
        return TaskParamsType.CLOSEPOSITION;
    }

    public static Builder newBuilder(final MergePositionParams mergePositionParams,
                                     final Function<IOrder, CloseParams> closeParamsFactory) {
        checkNotNull(mergePositionParams);
        checkNotNull(closeParamsFactory);

        return new Builder(mergePositionParams, closeParamsFactory);
    }

    public static class Builder extends TaskParamsBase.Builder<Builder> {

        private final MergePositionParams mergePositionParams;
        private final Function<IOrder, CloseParams> closeParamsFactory;
        private CloseExecutionMode closeExecutionMode = CloseExecutionMode.CloseAll;
        private BatchMode closeBatchMode = BatchMode.MERGE;
        private final Instrument instrument;

        public Builder(final MergePositionParams mergePositionParams,
                       final Function<IOrder, CloseParams> closeParamsFactory) {
            this.mergePositionParams = mergePositionParams;
            this.closeParamsFactory = closeParamsFactory;
            this.instrument = mergePositionParams.instrument();
        }

        public Builder withCloseExecutionMode(final CloseExecutionMode closeExecutionMode) {
            checkNotNull(closeExecutionMode);

            this.closeExecutionMode = closeExecutionMode;
            return this;
        }

        public Builder withCloseBatchMode(final BatchMode closeBatchMode) {
            checkNotNull(closeBatchMode);

            this.closeBatchMode = closeBatchMode;
            return this;
        }

        @Override
        public ClosePositionParams build() {
            return new ClosePositionParams(this);
        }
    }
}
