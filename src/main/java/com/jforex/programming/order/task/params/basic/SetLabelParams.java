package com.jforex.programming.order.task.params.basic;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.function.Consumer;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.event.OrderEventType;
import com.jforex.programming.order.task.params.TaskParamsBase;
import com.jforex.programming.order.task.params.TaskParamsType;
import com.jforex.programming.order.task.params.TaskParamsWithType;

public class SetLabelParams extends TaskParamsWithType {

    private final IOrder order;
    private final String newLabel;

    private SetLabelParams(final Builder builder) {
        super(builder);

        this.order = builder.order;
        this.newLabel = builder.newLabel;
    }

    public IOrder order() {
        return order;
    }

    public String newLabel() {
        return newLabel;
    }

    @Override
    public TaskParamsType type() {
        return TaskParamsType.SETLABEL;
    }

    public static Builder setLabelWith(final IOrder order,
                                       final String newLabel) {
        checkNotNull(order);
        checkNotNull(newLabel);

        return new Builder(order, newLabel);
    }

    public static class Builder extends TaskParamsBase.Builder<Builder> {

        private final IOrder order;
        private final String newLabel;

        public Builder(final IOrder order,
                       final String newLabel) {
            this.order = order;
            this.newLabel = newLabel;
        }

        public Builder doOnChangedLabel(final Consumer<OrderEvent> changedLabelConsumer) {
            setEventConsumer(OrderEventType.CHANGED_LABEL, changedLabelConsumer);
            return this;
        }

        public Builder doOnReject(final Consumer<OrderEvent> changeRejectConsumer) {
            setEventConsumer(OrderEventType.CHANGE_LABEL_REJECTED, changeRejectConsumer);
            return this;
        }

        public SetLabelParams build() {
            return new SetLabelParams(this);
        }
    }
}
