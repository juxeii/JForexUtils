package com.jforex.programming.order.spec;

import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.event.OrderEventType;

import io.reactivex.Observable;

public class SetGTTSpec extends SpecBase {

    private SetGTTSpec(final Builder builder) {
        super(builder);
    }

    public static Builder setGTTSpec(final Observable<OrderEvent> observable) {
        return new Builder(observable);
    }

    public static class Builder extends SpecBuilderBase<Builder, SetGTTSpec> {

        public Builder(final Observable<OrderEvent> observable) {
            super(observable);
        }

        public Builder doOnChangedGTT(final OrderEventConsumer changedGTTConsumer) {
            return setEventConsumer(OrderEventType.CHANGED_GTT, changedGTTConsumer);
        }

        public Builder doOnReject(final OrderEventConsumer changeRejectConsumer) {
            return setEventConsumer(OrderEventType.CHANGE_GTT_REJECTED, changeRejectConsumer);
        }

        @Override
        public SetGTTSpec start() {
            return new SetGTTSpec(this);
        }
    }
}
