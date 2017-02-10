package com.jforex.programming.order.task.params.position;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.function.Function;

import com.dukascopy.api.Instrument;
import com.jforex.programming.order.task.params.TaskParamsBase;
import com.jforex.programming.order.task.params.TaskParamsType;
import com.jforex.programming.order.task.params.TaskParamsWithType;

public class MergeAllPositionsParams extends TaskParamsWithType {

    private final Function<Instrument, MergePositionParams> mergeParamsFactory;

    private MergeAllPositionsParams(final Builder builder) {
        super(builder);

        mergeParamsFactory = builder.mergeParamsFactory;
    }

    public MergePositionParams createMergePositionParams(final Instrument instrument) {
        return mergeParamsFactory.apply(instrument);
    }

    @Override
    public TaskParamsType type() {
        return TaskParamsType.MERGEALLPOSITIONS;
    }

    public static Builder withMergeParamsFactory(final Function<Instrument, MergePositionParams> mergeParamsFactory) {
        checkNotNull(mergeParamsFactory);

        return new Builder(mergeParamsFactory);
    }

    public static class Builder extends TaskParamsBase.Builder<Builder> {

        private final Function<Instrument, MergePositionParams> mergeParamsFactory;

        public Builder(final Function<Instrument, MergePositionParams> mergeParamsFactory) {
            this.mergeParamsFactory = mergeParamsFactory;
        }

        public MergeAllPositionsParams build() {
            return new MergeAllPositionsParams(this);
        }
    }
}
