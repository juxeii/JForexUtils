package com.jforex.programming.order.task.params.basic;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.List;

import com.jforex.programming.order.task.params.BasicTaskParamsBase;
import com.jforex.programming.order.task.params.TaskParamsType;

public class BatchParams extends BasicTaskParamsBase {

    private final List<? extends BasicTaskParamsBase> paramsList;

    private BatchParams(final Builder builder) {
        super(builder);

        this.paramsList = builder.paramsList;
    }

    public List<? extends BasicTaskParamsBase> paramsList() {
        return paramsList;
    }

    public static Builder setBatchWith(List<? extends BasicTaskParamsBase> paramsList) {
        checkNotNull(paramsList);

        return new Builder(paramsList);
    }

    public static class Builder extends BasicParamsBuilder<Builder> {

        private final List<? extends BasicTaskParamsBase> paramsList;

        public Builder(List<? extends BasicTaskParamsBase> paramsList) {
            this.paramsList = paramsList;
        }

        public BatchParams build() {
            return new BatchParams(this);
        }
    }

    @Override
    public TaskParamsType type() {
        return TaskParamsType.BATCH;
    }
}
