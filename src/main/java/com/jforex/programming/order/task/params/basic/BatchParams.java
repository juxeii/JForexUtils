package com.jforex.programming.order.task.params.basic;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.List;

import com.jforex.programming.order.task.params.CommonParamsBase;

public class BatchParams extends CommonParamsBase {

    private final List<? extends CommonParamsBase> paramsList;

    private BatchParams(final Builder builder) {
        super(builder);

        this.paramsList = builder.paramsList;
    }

    public List<? extends CommonParamsBase> paramsList() {
        return paramsList;
    }

    public static Builder setBatchWith(List<? extends CommonParamsBase> paramsList) {
        checkNotNull(paramsList);

        return new Builder(paramsList);
    }

    public static class Builder extends BasicParamsBuilder<Builder> {

        private final List<? extends CommonParamsBase> paramsList;

        public Builder(List<? extends CommonParamsBase> paramsList) {
            this.paramsList = paramsList;
        }

        public BatchParams build() {
            return new BatchParams(this);
        }
    }
}
