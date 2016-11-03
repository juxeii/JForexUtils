package com.jforex.programming.order.spec;

import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.event.OrderEventType;

import io.reactivex.Observable;

public class SetTPSpec extends SpecBase {

    private SetTPSpec(final Builder builder) {
        super(builder);
    }

    public static Builder setTPSpec(final Observable<OrderEvent> observable) {
        return new Builder(observable);
    }

    public static class Builder extends SpecBuilderBase<Builder, SetTPSpec> {

        public Builder(final Observable<OrderEvent> observable) {
            super(observable);
        }

        public Builder doOnChangedTP(final OrderEventConsumer changedTPConsumer) {
            return setEventConsumer(OrderEventType.CHANGED_TP, changedTPConsumer);
        }

        public Builder doOnReject(final OrderEventConsumer changeRejectConsumer) {
            return setEventConsumer(OrderEventType.CHANGE_TP_REJECTED, changeRejectConsumer);
        }

        @Override
        public SetTPSpec start() {
            return new SetTPSpec(this);
        }
    }
}
