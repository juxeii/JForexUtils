package com.jforex.programming.order.spec;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Collection;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.event.OrderEventType;
import com.jforex.programming.order.task.BatchMode;
import com.jforex.programming.order.task.MergeExecutionMode;
import com.jforex.programming.order.task.MergeTask;
import com.jforex.programming.order.task.TaskRetry;

import io.reactivex.Observable;

public class ComplexMergeSpec extends GenericSpecBase {

    private final String mergeOrderLabel;
    private final Collection<IOrder> toMergeOrders;
    private final MergeTask mergeTask;
    private final MergeExecutionMode mergeExecutionMode;
    private final BatchMode cancelSLBatchMode;
    private final BatchMode cancelTPBatchMode;

    private final SimpleMergeBuilder simpleMergeBuilder;
    private final CancelSLTPBuilder cancelSLTPBuilder;
    private final CancelSLBuilder cancelSLBuilder;
    private final CancelTPBuilder cancelTPBuilder;

    protected ComplexMergeSpec(final ComplexMergeBuilder builder) {
        super(builder);

        mergeOrderLabel = builder.mergeOrderLabel;
        toMergeOrders = builder.toMergeOrders;
        mergeTask = builder.mergeTask;
        mergeExecutionMode = builder.mergeExecutionMode;

        simpleMergeBuilder = builder.simpleMergeBuilder;
        cancelSLTPBuilder = builder.cancelSLTPBuilder;
        cancelSLBatchMode = cancelSLTPBuilder.cancelSLBatchMode;
        cancelTPBatchMode = cancelSLTPBuilder.cancelTPBatchMode;
        cancelSLBuilder = builder.cancelSLTPBuilder.cancelSLBuilder;
        cancelTPBuilder = builder.cancelSLTPBuilder.cancelTPBuilder;

        Observable<OrderEvent> observable = mergeTask.merge(this);
        if (noOfRetries > 0)
            observable = observable.compose(TaskRetry.onRejectRetryWith(noOfRetries, delayInMillis));
        observable
            .doOnSubscribe(d -> startAction.run())
            .subscribe(this::handleEvent,
                       errorConsumer::accept,
                       completeAction::run);
    }

    private void handleEvent(final OrderEvent orderEvent) {
        final OrderEventType type = orderEvent.type();
        if (consumerForEvent.containsKey(type))
            consumerForEvent
                .get(type)
                .accept(orderEvent);
    }

    public Collection<IOrder> toMergeOrders() {
        return toMergeOrders;
    }

    public String mergeOrderLabel() {
        return mergeOrderLabel;
    }

    public MergeExecutionMode mergeExecutionMode() {
        return mergeExecutionMode;
    }

    public BatchMode cancelSLBatchMode() {
        return cancelSLBatchMode;
    }

    public BatchMode cancelTPBatchMode() {
        return cancelTPBatchMode;
    }

    public Observable<OrderEvent> composeMerge(final Observable<OrderEvent> observable) {
        return simpleMergeBuilder.composeObservable(observable);
    }

    public Observable<OrderEvent> composeCancelSLTP(final Observable<OrderEvent> observable) {
        return cancelSLTPBuilder.composeObservable(observable);
    }

    public Observable<OrderEvent> composeCancelSL(final Observable<OrderEvent> observable) {
        return cancelSLBuilder.composeObservable(observable);
    }

    public Observable<OrderEvent> composeCancelTP(final Observable<OrderEvent> observable) {
        return cancelTPBuilder.composeObservable(observable);
    }

    public static ComplexMergeBuilder forMerge(final String mergeOrderLabel,
                                               final Collection<IOrder> toMergeOrders,
                                               final MergeTask mergeTask) {
        return new ComplexMergeBuilder(mergeOrderLabel,
                                       toMergeOrders,
                                       mergeTask);
    }

    public static class ComplexMergeBuilder extends BuilderBase<ComplexMergeBuilder> {

        private final String mergeOrderLabel;
        private final Collection<IOrder> toMergeOrders;
        private final MergeTask mergeTask;
        private MergeExecutionMode mergeExecutionMode = MergeExecutionMode.MergeCancelSLAndTP;

        private final SimpleMergeBuilder simpleMergeBuilder;
        private final CancelSLTPBuilder cancelSLTPBuilder;

        public ComplexMergeBuilder(final String mergeOrderLabel,
                                   final Collection<IOrder> toMergeOrders,
                                   final MergeTask mergeTask) {
            this.mergeOrderLabel = mergeOrderLabel;
            this.toMergeOrders = toMergeOrders;
            this.mergeTask = mergeTask;

            simpleMergeBuilder = new SimpleMergeBuilder(this);
            cancelSLTPBuilder = new CancelSLTPBuilder(this);
        }

        public SimpleMergeBuilder withSimpleMergeOption() {
            return simpleMergeBuilder;
        }

        public CancelSLTPBuilder withCancelSLTPOption(final MergeExecutionMode mergeExecutionMode) {
            checkNotNull(mergeExecutionMode);

            this.mergeExecutionMode = mergeExecutionMode;
            return cancelSLTPBuilder;
        }

        public ComplexMergeSpec start() {
            return new ComplexMergeSpec(this);
        }
    }

    public static class SimpleMergeBuilder extends BuilderBase<SimpleMergeBuilder> {

        private final ComplexMergeBuilder complexMergeBuilder;

        public SimpleMergeBuilder(final ComplexMergeBuilder complexMergeBuilder) {
            this.complexMergeBuilder = complexMergeBuilder;
        }

        public SimpleMergeBuilder doOnMerge(final OrderEventConsumer mergeConsumer) {
            return setEventConsumer(OrderEventType.MERGE_OK, mergeConsumer);
        }

        public SimpleMergeBuilder doOnMergeClose(final OrderEventConsumer mergeCloseConsumer) {
            return setEventConsumer(OrderEventType.MERGE_CLOSE_OK, mergeCloseConsumer);
        }

        public SimpleMergeBuilder doOnMergeReject(final OrderEventConsumer rejectConsumer) {
            return setEventConsumer(OrderEventType.MERGE_REJECTED, rejectConsumer);
        }

        public ComplexMergeBuilder build() {
            return complexMergeBuilder;
        }
    }

    public static class CancelSLTPBuilder extends BuilderBase<CancelSLTPBuilder> {

        private final ComplexMergeBuilder complexMergeBuilder;
        public BatchMode cancelSLBatchMode = BatchMode.MERGE;
        public BatchMode cancelTPBatchMode = BatchMode.MERGE;
        public final CancelSLBuilder cancelSLBuilder;
        public final CancelTPBuilder cancelTPBuilder;

        public CancelSLTPBuilder(final ComplexMergeBuilder complexMergeBuilder) {
            this.complexMergeBuilder = complexMergeBuilder;

            cancelSLBuilder = new CancelSLBuilder(this);
            cancelTPBuilder = new CancelTPBuilder(this);
        }

        public CancelSLBuilder withCancelSLOption(final BatchMode cancelSLBatchMode) {
            checkNotNull(cancelSLBatchMode);

            this.cancelSLBatchMode = cancelSLBatchMode;
            return cancelSLBuilder;
        }

        public CancelTPBuilder withCancelTPOption(final BatchMode cancelTPBatchMode) {
            checkNotNull(cancelTPBatchMode);

            this.cancelTPBatchMode = cancelTPBatchMode;
            return cancelTPBuilder;
        }

        public ComplexMergeBuilder build() {
            return complexMergeBuilder;
        }
    }

    public static class CancelSLBuilder extends BuilderBase<CancelSLBuilder> {

        private final CancelSLTPBuilder cancelSLTPBuilder;

        public CancelSLBuilder(final CancelSLTPBuilder cancelSLTPBuilder) {
            this.cancelSLTPBuilder = cancelSLTPBuilder;
        }

        public CancelSLBuilder doOnCancelSL(final OrderEventConsumer cancelSLConsumer) {
            return setEventConsumer(OrderEventType.CHANGED_SL, cancelSLConsumer);
        }

        public CancelSLBuilder doOnRejectCancelSL(final OrderEventConsumer rejectConsumer) {
            return setEventConsumer(OrderEventType.CHANGE_SL_REJECTED, rejectConsumer);
        }

        public CancelSLTPBuilder build() {
            return cancelSLTPBuilder;
        }
    }

    public static class CancelTPBuilder extends BuilderBase<CancelTPBuilder> {

        private final CancelSLTPBuilder cancelSLTPBuilder;

        public CancelTPBuilder(final CancelSLTPBuilder cancelSLTPBuilder) {
            this.cancelSLTPBuilder = cancelSLTPBuilder;
        }

        public CancelTPBuilder doOnCancelTP(final OrderEventConsumer cancelTPConsumer) {
            return setEventConsumer(OrderEventType.CHANGED_TP, cancelTPConsumer);
        }

        public CancelTPBuilder doOnRejectCancelTP(final OrderEventConsumer rejectConsumer) {
            return setEventConsumer(OrderEventType.CHANGE_TP_REJECTED, rejectConsumer);
        }

        public CancelSLTPBuilder build() {
            return cancelSLTPBuilder;
        }
    }
}
