package com.jforex.programming.order.task.params;

import static com.google.common.base.Preconditions.checkNotNull;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.event.OrderEventType;
import com.jforex.programming.order.task.BasicTask;

public class SetLabelParams extends BasicTaskParams {

    private final IOrder order;
    private final String newLabel;

    private SetLabelParams(final Builder builder) {
        super(builder);

        this.order = builder.order;
        this.newLabel = builder.newLabel;
    }

    public void subscribe(final BasicTask basicTask) {
        subscribe(basicTask.setLabel(order, newLabel));
    }

    public static Builder setLabelWith(final IOrder order,
                                       final String newLabel) {
        checkNotNull(order);
        checkNotNull(newLabel);

        return new Builder(order, newLabel);
    }

    public static class Builder extends ParamsBuilderBase<Builder> {

        private final IOrder order;
        private final String newLabel;

        public Builder(final IOrder order,
                       final String newLabel) {
            this.order = order;
            this.newLabel = newLabel;
        }

        public Builder doOnChangedLabel(final OrderEventConsumer changedLabelConsumer) {
            return setEventConsumer(OrderEventType.CHANGED_LABEL, changedLabelConsumer);
        }

        public Builder doOnReject(final OrderEventConsumer changeRejectConsumer) {
            return setEventConsumer(OrderEventType.CHANGE_LABEL_REJECTED, changeRejectConsumer);
        }

        public SetLabelParams build() {
            return new SetLabelParams(this);
        }
    }
}
