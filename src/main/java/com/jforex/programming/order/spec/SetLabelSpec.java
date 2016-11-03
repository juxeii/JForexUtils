package com.jforex.programming.order.spec;

import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.event.OrderEventType;

import io.reactivex.Observable;

public class SetLabelSpec extends SpecBase {

    private SetLabelSpec(final Builder builder) {
        super(builder);
    }

    public static Builder setLabelSpec(final Observable<OrderEvent> observable) {
        return new Builder(observable);
    }

    public static class Builder extends SpecBuilderBase<Builder, SetLabelSpec> {

        public Builder(final Observable<OrderEvent> observable) {
            super(observable);
        }

        public Builder doOnChangedLabel(final OrderEventConsumer changedLabelConsumer) {
            return setEventConsumer(OrderEventType.CHANGED_LABEL, changedLabelConsumer);
        }

        public Builder doOnReject(final OrderEventConsumer changeRejectConsumer) {
            return setEventConsumer(OrderEventType.CHANGE_LABEL_REJECTED, changeRejectConsumer);
        }

        @Override
        public SetLabelSpec start() {
            return new SetLabelSpec(this);
        }
    }
}
