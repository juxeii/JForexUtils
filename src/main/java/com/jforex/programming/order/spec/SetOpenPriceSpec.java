package com.jforex.programming.order.spec;

import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.event.OrderEventType;

import io.reactivex.Observable;

public class SetOpenPriceSpec extends SpecBase {

    private SetOpenPriceSpec(final Builder builder) {
        super(builder);
    }

    public static Builder setOpenPriceSpec(final Observable<OrderEvent> observable) {
        return new Builder(observable);
    }

    public static class Builder extends SpecBuilderBase<Builder, SetOpenPriceSpec> {

        public Builder(final Observable<OrderEvent> observable) {
            super(observable);
        }

        public Builder doOnChangedOpenPrice(final OrderEventConsumer changedOpenPriceConsumer) {
            return setEventConsumer(OrderEventType.CHANGED_PRICE, changedOpenPriceConsumer);
        }

        public Builder doOnReject(final OrderEventConsumer changeRejectConsumer) {
            return setEventConsumer(OrderEventType.CHANGE_PRICE_REJECTED, changeRejectConsumer);
        }

        @Override
        public SetOpenPriceSpec start() {
            return new SetOpenPriceSpec(this);
        }
    }
}
