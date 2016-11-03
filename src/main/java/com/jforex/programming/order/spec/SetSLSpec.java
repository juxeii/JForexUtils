package com.jforex.programming.order.spec;

import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.event.OrderEventType;

import io.reactivex.Observable;

public class SetSLSpec extends SpecBase {

    private SetSLSpec(final Builder builder) {
        super(builder);
    }

    public static Builder setSLSpec(final Observable<OrderEvent> observable) {
        return new Builder(observable);
    }

    public static class Builder extends SpecBuilderBase<Builder, SetSLSpec> {

        public Builder(final Observable<OrderEvent> observable) {
            super(observable);
        }

        public Builder doOnChangedSL(final OrderEventConsumer changedSLConsumer) {
            return setEventConsumer(OrderEventType.CHANGED_SL, changedSLConsumer);
        }

        public Builder doOnReject(final OrderEventConsumer changeRejectConsumer) {
            return setEventConsumer(OrderEventType.CHANGE_SL_REJECTED, changeRejectConsumer);
        }

        @Override
        public SetSLSpec start() {
            return new SetSLSpec(this);
        }
    }
}
