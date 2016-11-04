package com.jforex.programming.order.spec;

import java.util.Map;

import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.event.OrderEventType;
import com.jforex.programming.order.task.TaskRetry;

import io.reactivex.Observable;
import io.reactivex.functions.Action;

public class BasicSpec {

    protected Observable<OrderEvent> observable;
    protected final Map<OrderEventType, OrderEventConsumer> consumerForEvent;
    protected final ErrorConsumer errorConsumer;
    protected Action startAction;
    protected Action completeAction;
    private final int noOfRetries;
    private final long delayInMillis;

    protected BasicSpec(final SpecBuilderBase<?> specBuilderBase) {
        observable = specBuilderBase.observable;
        consumerForEvent = specBuilderBase.consumerForEvent;
        errorConsumer = specBuilderBase.errorConsumer;
        startAction = specBuilderBase.startAction;
        completeAction = specBuilderBase.completeAction;
        noOfRetries = specBuilderBase.noOfRetries;
        delayInMillis = specBuilderBase.delayInMillis;

        setupRetry();
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

    private void setupRetry() {
        if (noOfRetries > 0)
            observable = observable.compose(TaskRetry.onRejectRetryWith(noOfRetries, delayInMillis));
    }

    public static SubmitBuilder forSubmit(final Observable<OrderEvent> observable) {
        return new SubmitBuilder(observable);
    }

    public static MergeBuilder forMerge(final Observable<OrderEvent> observable) {
        return new MergeBuilder(observable);
    }

    public static CloseBuilder forClose(final Observable<OrderEvent> observable) {
        return new CloseBuilder(observable);
    }

    public static SetLabelBuilder forSetLabel(final Observable<OrderEvent> observable) {
        return new SetLabelBuilder(observable);
    }

    public static SetAmountBuilder forSetAmount(final Observable<OrderEvent> observable) {
        return new SetAmountBuilder(observable);
    }

    public static SetGTTBuilder forSetGTT(final Observable<OrderEvent> observable) {
        return new SetGTTBuilder(observable);
    }

    public static SetOpenPriceBuilder forSetOpenPrice(final Observable<OrderEvent> observable) {
        return new SetOpenPriceBuilder(observable);
    }

    public static SetSLBuilder forSetSL(final Observable<OrderEvent> observable) {
        return new SetSLBuilder(observable);
    }

    public static SetTPBuilder forSetTP(final Observable<OrderEvent> observable) {
        return new SetTPBuilder(observable);
    }

    public static class SubmitBuilder extends SpecBuilderBase<SubmitBuilder> {

        public SubmitBuilder(final Observable<OrderEvent> observable) {
            super(observable);
        }

        public SubmitBuilder doOnSubmit(final OrderEventConsumer submitConsumer) {
            return setEventConsumer(OrderEventType.SUBMIT_OK, submitConsumer);
        }

        public SubmitBuilder doOnPartialFill(final OrderEventConsumer partialFillConsumer) {
            return setEventConsumer(OrderEventType.PARTIAL_FILL_OK, partialFillConsumer);
        }

        public SubmitBuilder doOnFullFill(final OrderEventConsumer fullFillConsumer) {
            return setEventConsumer(OrderEventType.FULLY_FILLED, fullFillConsumer);
        }

        public SubmitBuilder doOnSubmitReject(final OrderEventConsumer submitRejectConsumer) {
            return setEventConsumer(OrderEventType.SUBMIT_REJECTED, submitRejectConsumer);
        }

        public SubmitBuilder doOnFillReject(final OrderEventConsumer fillRejectConsumer) {
            return setEventConsumer(OrderEventType.FILL_REJECTED, fillRejectConsumer);
        }
    }

    public static class MergeBuilder extends SpecBuilderBase<MergeBuilder> {

        public MergeBuilder(final Observable<OrderEvent> observable) {
            super(observable);
        }

        public MergeBuilder doOnMerge(final OrderEventConsumer mergeConsumer) {
            return setEventConsumer(OrderEventType.MERGE_OK, mergeConsumer);
        }

        public MergeBuilder doOnMergeClose(final OrderEventConsumer mergeCloseConsumer) {
            return setEventConsumer(OrderEventType.MERGE_CLOSE_OK, mergeCloseConsumer);
        }

        public MergeBuilder doOnReject(final OrderEventConsumer rejectConsumer) {
            return setEventConsumer(OrderEventType.MERGE_REJECTED, rejectConsumer);
        }
    }

    public static class CloseBuilder extends SpecBuilderBase<CloseBuilder> {

        public CloseBuilder(final Observable<OrderEvent> observable) {
            super(observable);
        }

        public CloseBuilder doOnClose(final OrderEventConsumer closeConsumer) {
            return setEventConsumer(OrderEventType.CLOSE_OK, closeConsumer);
        }

        public CloseBuilder doOnReject(final OrderEventConsumer closeRejectConsumer) {
            return setEventConsumer(OrderEventType.CLOSE_REJECTED, closeRejectConsumer);
        }
    }

    public static class SetLabelBuilder extends SpecBuilderBase<SetLabelBuilder> {

        public SetLabelBuilder(final Observable<OrderEvent> observable) {
            super(observable);
        }

        public SetLabelBuilder doOnChangedLabel(final OrderEventConsumer changedLabelConsumer) {
            return setEventConsumer(OrderEventType.CHANGED_LABEL, changedLabelConsumer);
        }

        public SetLabelBuilder doOnReject(final OrderEventConsumer changeRejectConsumer) {
            return setEventConsumer(OrderEventType.CHANGE_LABEL_REJECTED, changeRejectConsumer);
        }
    }

    public static class SetAmountBuilder extends SpecBuilderBase<SetAmountBuilder> {

        public SetAmountBuilder(final Observable<OrderEvent> observable) {
            super(observable);
        }

        public SetAmountBuilder doOnChangedAmount(final OrderEventConsumer changedAmountConsumer) {
            return setEventConsumer(OrderEventType.CHANGED_AMOUNT, changedAmountConsumer);
        }

        public SetAmountBuilder doOnReject(final OrderEventConsumer changeRejectConsumer) {
            return setEventConsumer(OrderEventType.CHANGE_AMOUNT_REJECTED, changeRejectConsumer);
        }
    }

    public static class SetGTTBuilder extends SpecBuilderBase<SetGTTBuilder> {

        public SetGTTBuilder(final Observable<OrderEvent> observable) {
            super(observable);
        }

        public SetGTTBuilder doOnChangedGTT(final OrderEventConsumer changedGTTConsumer) {
            return setEventConsumer(OrderEventType.CHANGED_GTT, changedGTTConsumer);
        }

        public SetGTTBuilder doOnReject(final OrderEventConsumer changeRejectConsumer) {
            return setEventConsumer(OrderEventType.CHANGE_GTT_REJECTED, changeRejectConsumer);
        }
    }

    public static class SetOpenPriceBuilder extends SpecBuilderBase<SetOpenPriceBuilder> {

        public SetOpenPriceBuilder(final Observable<OrderEvent> observable) {
            super(observable);
        }

        public SetOpenPriceBuilder doOnChangedOpenPrice(final OrderEventConsumer changedOpenPriceConsumer) {
            return setEventConsumer(OrderEventType.CHANGED_PRICE, changedOpenPriceConsumer);
        }

        public SetOpenPriceBuilder doOnReject(final OrderEventConsumer changeRejectConsumer) {
            return setEventConsumer(OrderEventType.CHANGE_PRICE_REJECTED, changeRejectConsumer);
        }
    }

    public static class SetSLBuilder extends SpecBuilderBase<SetSLBuilder> {

        public SetSLBuilder(final Observable<OrderEvent> observable) {
            super(observable);
        }

        public SetSLBuilder doOnChangedSL(final OrderEventConsumer changedSLConsumer) {
            return setEventConsumer(OrderEventType.CHANGED_SL, changedSLConsumer);
        }

        public SetSLBuilder doOnReject(final OrderEventConsumer changeRejectConsumer) {
            return setEventConsumer(OrderEventType.CHANGE_SL_REJECTED, changeRejectConsumer);
        }
    }

    public static class SetTPBuilder extends SpecBuilderBase<SetTPBuilder> {

        public SetTPBuilder(final Observable<OrderEvent> observable) {
            super(observable);
        }

        public SetTPBuilder doOnChangedTP(final OrderEventConsumer changedTPConsumer) {
            return setEventConsumer(OrderEventType.CHANGED_TP, changedTPConsumer);
        }

        public SetTPBuilder doOnReject(final OrderEventConsumer changeRejectConsumer) {
            return setEventConsumer(OrderEventType.CHANGE_TP_REJECTED, changeRejectConsumer);
        }
    }
}
