package com.jforex.programming.order.task.params.basic;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Collection;

import com.jforex.programming.order.task.params.TaskParams;
import com.jforex.programming.order.task.params.TaskParamsBase;

public class BatchParams extends TaskParamsBase {

    private final Collection<TaskParams> taskParams;

    private BatchParams(final Builder builder) {
        super(builder);

        this.taskParams = builder.taskParams;
    }

    public Collection<TaskParams> taskParams() {
        return taskParams;
    }

    public static Builder setBatchWith(final Collection<TaskParams> taskParams) {
        checkNotNull(taskParams);

        return new Builder(taskParams);
    }

    public static class Builder extends TaskParamsBase.Builder<Builder> {

        private final Collection<TaskParams> taskParams;

        public Builder(final Collection<TaskParams> taskParams) {
            this.taskParams = taskParams;
        }

        public BatchParams build() {
            return new BatchParams(this);
        }
    }
}
