package com.jforex.programming.order.spec;

import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.event.OrderEventType;

import io.reactivex.Observable;

public class SimpleMergeSpec extends GenericSpecBase {

    private final Observable<OrderEvent> observable;

    protected SimpleMergeSpec(final SimpleMergeBuilder builder) {
        super(builder);

        observable = composeObservable(builder.observable);
    }

    public Observable<OrderEvent> observable() {
        return observable;
    }

    public static SimpleMergeBuilder forSimpleMerge(final Observable<OrderEvent> observable) {
        return new SimpleMergeBuilder(observable);
    }

    public static class SimpleMergeBuilder extends SpecBuilderBase<SimpleMergeBuilder> {

        public SimpleMergeBuilder(final Observable<OrderEvent> observable) {
            super(observable);
        }

        public SimpleMergeBuilder doOnMerge(final OrderEventConsumer mergeConsumer) {
            return setEventConsumer(OrderEventType.MERGE_OK, mergeConsumer);
        }

        public SimpleMergeBuilder doOnMergeClose(final OrderEventConsumer mergeCloseConsumer) {
            return setEventConsumer(OrderEventType.MERGE_CLOSE_OK, mergeCloseConsumer);
        }

        public SimpleMergeBuilder doOnReject(final OrderEventConsumer rejectConsumer) {
            return setEventConsumer(OrderEventType.MERGE_REJECTED, rejectConsumer);
        }

        public SimpleMergeSpec build() {
            return new SimpleMergeSpec(this);
        }
    }
}
