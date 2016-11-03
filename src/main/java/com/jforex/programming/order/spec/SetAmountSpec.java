package com.jforex.programming.order.spec;

import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.event.OrderEventType;

import io.reactivex.Observable;

public class SetAmountSpec extends SpecBase {

    private SetAmountSpec(final Builder builder) {
        super(builder);
    }

    public static Builder setAmountSpec(final Observable<OrderEvent> observable) {
        return new Builder(observable);
    }

    public static class Builder extends SpecBuilderBase<Builder, SetAmountSpec> {

        public Builder(final Observable<OrderEvent> observable) {
            super(observable);
        }

        public Builder doOnChangedAmount(final OrderEventConsumer changedAmountConsumer) {
            return setEventConsumer(OrderEventType.CHANGED_AMOUNT, changedAmountConsumer);
        }

        public Builder doOnReject(final OrderEventConsumer changeRejectConsumer) {
            return setEventConsumer(OrderEventType.CHANGE_AMOUNT_REJECTED, changeRejectConsumer);
        }

        @Override
        public SetAmountSpec start() {
            return new SetAmountSpec(this);
        }
    }
}
