package com.jforex.programming.order.task.params.position;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.function.Function;

import com.dukascopy.api.Instrument;
import com.jforex.programming.order.task.params.TaskParamsBase;
import com.jforex.programming.order.task.params.TaskParamsType;
import com.jforex.programming.order.task.params.TaskParamsWithType;

public class MergeAllPositionsParams extends TaskParamsWithType {

    private final Function<Instrument, MergePositionParams> mergePositionParamsFactory;

    private MergeAllPositionsParams(final Builder builder) {
        super(builder);

        mergePositionParamsFactory = builder.mergePositionParamsFactory;
    }

    public Function<Instrument, MergePositionParams> mergePositionParamsFactory() {
        return mergePositionParamsFactory;
    }

    @Override
    public TaskParamsType type() {
        return TaskParamsType.MERGEALLPOSITIONS;
    }

    public static Builder withMergeParamsFactory(final Function<Instrument,
                                                                MergePositionParams> mergePositionParamsFactory) {
        checkNotNull(mergePositionParamsFactory);

        return new Builder(mergePositionParamsFactory);
    }

    public static class Builder extends TaskParamsBase.Builder<Builder> {

        private final Function<Instrument, MergePositionParams> mergePositionParamsFactory;

        public Builder(final Function<Instrument, MergePositionParams> mergePositionParamsFactory) {
            this.mergePositionParamsFactory = mergePositionParamsFactory;
        }

        @Override
        public MergeAllPositionsParams build() {
            return new MergeAllPositionsParams(this);
        }
    }
}
