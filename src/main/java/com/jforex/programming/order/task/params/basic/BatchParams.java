package com.jforex.programming.order.task.params.basic;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Collection;

import com.jforex.programming.order.task.params.TaskParams;
import com.jforex.programming.order.task.params.TaskParamsBase;

public class BatchParams extends TaskParamsBase {

    private final Collection<TaskParams> paramsCollection;

    private BatchParams(final Builder builder) {
        super(builder);

        this.paramsCollection = builder.paramsCollection;
    }

    public Collection<TaskParams> paramsCollection() {
        return paramsCollection;
    }

    public static Builder setBatchWith(final Collection<TaskParams> paramsCollection) {
        checkNotNull(paramsCollection);

        return new Builder(paramsCollection);
    }

    public static class Builder extends TaskParamsBase.Builder<Builder> {

        private final Collection<TaskParams> paramsCollection;

        public Builder(final Collection<TaskParams> paramsCollection) {
            this.paramsCollection = paramsCollection;
        }

        public BatchParams build() {
            return new BatchParams(this);
        }

        @Override
        public Builder getThis() {
            return this;
        }
    }
}
