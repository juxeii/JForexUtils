package com.jforex.programming.order.task.params.basic;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Collection;

import com.jforex.programming.order.task.params.TaskComposeAndEventMapData;
import com.jforex.programming.order.task.params.TaskParamsBase;

public class BatchParams extends TaskComposeAndEventMapData {

    private final Collection<TaskParamsBase> paramsCollection;

    private BatchParams(final Builder builder) {
        super(builder);

        this.paramsCollection = builder.paramsCollection;
    }

    public Collection<TaskParamsBase> paramsCollection() {
        return paramsCollection;
    }

    public static Builder setBatchWith(final Collection<TaskParamsBase> paramsCollection) {
        checkNotNull(paramsCollection);

        return new Builder(paramsCollection);
    }

    public static class Builder extends BasicParamsBuilder<Builder> {

        private final Collection<TaskParamsBase> paramsCollection;

        public Builder(final Collection<TaskParamsBase> paramsCollection) {
            this.paramsCollection = paramsCollection;
        }

        public BatchParams build() {
            return new BatchParams(this);
        }
    }
}
