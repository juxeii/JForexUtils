package com.jforex.programming.order.spec;

import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.event.OrderEventType;

import io.reactivex.Observable;

public class MergeSpec extends SpecBase {

    private MergeSpec(final Builder builder) {
        super(builder);
    }

    public static Builder mergeSpec(final Observable<OrderEvent> observable) {
        return new Builder(observable);
    }

    public static class Builder extends SpecBuilderBase<Builder, MergeSpec> {

        public Builder(final Observable<OrderEvent> observable) {
            super(observable);
        }

        public Builder doOnMerge(final OrderEventConsumer mergeConsumer) {
            return setEventConsumer(OrderEventType.MERGE_OK, mergeConsumer);
        }

        public Builder doOnMergeClose(final OrderEventConsumer mergeCloseConsumer) {
            return setEventConsumer(OrderEventType.MERGE_CLOSE_OK, mergeCloseConsumer);
        }

        public Builder doOnReject(final OrderEventConsumer rejectConsumer) {
            return setEventConsumer(OrderEventType.MERGE_REJECTED, rejectConsumer);
        }

        @Override
        public MergeSpec start() {
            return new MergeSpec(this);
        }
    }
}
