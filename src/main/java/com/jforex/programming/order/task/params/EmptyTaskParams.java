package com.jforex.programming.order.task.params;

public class EmptyTaskParams extends TaskParamsBase {

    private EmptyTaskParams(final Builder builder) {
        super(builder);
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static class Builder extends TaskParamsBase.Builder<Builder> {

        public EmptyTaskParams build() {
            return new EmptyTaskParams(this);
        }

        @Override
        protected Builder getThis() {
            return this;
        }
    }
}
