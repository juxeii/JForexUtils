package com.jforex.programming.order.task.params.position;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

import com.dukascopy.api.IOrder;
import com.dukascopy.api.Instrument;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.event.OrderEventType;
import com.jforex.programming.order.task.BatchMode;
import com.jforex.programming.order.task.CancelSLTPMode;
import com.jforex.programming.order.task.params.CommonParamsBuilder;
import com.jforex.programming.order.task.params.ComposeData;

public class MergePositionParams {

    private final Instrument instrument;
    private final String mergeOrderLabel;
    private final Map<OrderEventType, Consumer<OrderEvent>> consumerForEvent;

    private final PositionParams mergePositionComposeParams;
    private final PositionParams cancelSLTPComposeParams;
    private final PositionParams batchCancelSLComposeParams;
    private final PositionParams batchCancelTPComposeParams;
    private final PositionParams mergeComposeParams;
    private final Function<IOrder, PositionParams> cancelSLComposeParams;
    private final Function<IOrder, PositionParams> cancelTPComposeParams;

    private final CancelSLTPMode mergeExecutionMode;
    private final BatchMode batchCancelSLMode;
    private final BatchMode batchCancelTPMode;

    private MergePositionParams(final Builder builder) {
        instrument = builder.instrument;
        mergeOrderLabel = builder.mergeOrderLabel;
        consumerForEvent = builder.consumerForEvent;

        mergePositionComposeParams = builder.mergePositionComposeParams;
        cancelSLTPComposeParams = builder.cancelSLTPComposeParams;
        batchCancelSLComposeParams = builder.batchCancelSLComposeParams;
        batchCancelTPComposeParams = builder.batchCancelTPComposeParams;
        cancelSLComposeParams = builder.cancelSLComposeParams;
        cancelTPComposeParams = builder.cancelTPComposeParams;
        mergeComposeParams = builder.mergeComposeParams;

        mergeExecutionMode = builder.mergeExecutionMode;
        batchCancelSLMode = builder.batchCancelSLMode;
        batchCancelTPMode = builder.batchCancelTPMode;
    }

    public Instrument instrument() {
        return instrument;
    }

    public String mergeOrderLabel() {
        return mergeOrderLabel;
    }

    public Map<OrderEventType, Consumer<OrderEvent>> consumerForEvent() {
        return consumerForEvent;
    }

    public ComposeData mergePositionComposeParams() {
        return mergePositionComposeParams.composeData();
    }

    public ComposeData cancelSLTPComposeParams() {
        return cancelSLTPComposeParams.composeData();
    }

    public ComposeData batchCancelSLComposeParams() {
        return batchCancelSLComposeParams.composeData();
    }

    public ComposeData batchCancelTPComposeParams() {
        return batchCancelTPComposeParams.composeData();
    }

    public ComposeData cancelSLComposeParams(final IOrder order) {
        return cancelSLComposeParams
            .apply(order)
            .composeData();
    }

    public ComposeData cancelTPComposeParams(final IOrder order) {
        return cancelTPComposeParams
            .apply(order)
            .composeData();
    }

    public ComposeData mergeComposeParams() {
        return mergeComposeParams.composeData();
    }

    public CancelSLTPMode mergeExecutionMode() {
        return mergeExecutionMode;
    }

    public BatchMode batchCancelSLMode() {
        return batchCancelSLMode;
    }

    public BatchMode batchCancelTPMode() {
        return batchCancelTPMode;
    }

    public static Builder newBuilder(final Instrument instrument,
                                     final String mergeOrderLabel) {
        checkNotNull(instrument);
        checkNotNull(mergeOrderLabel);

        return new Builder(instrument, mergeOrderLabel);
    }

    public static class Builder extends CommonParamsBuilder<Builder> {

        private final Instrument instrument;
        private final String mergeOrderLabel;

        private PositionParams mergePositionComposeParams = PositionParams.newBuilder().build();
        private PositionParams cancelSLTPComposeParams = PositionParams.newBuilder().build();
        private PositionParams batchCancelSLComposeParams = PositionParams.newBuilder().build();
        private PositionParams batchCancelTPComposeParams = PositionParams.newBuilder().build();
        private PositionParams mergeComposeParams = PositionParams.newBuilder().build();
        private Function<IOrder, PositionParams> cancelSLComposeParams =
                order -> PositionParams.newBuilder().build();
        private Function<IOrder, PositionParams> cancelTPComposeParams =
                order -> PositionParams.newBuilder().build();
        private CancelSLTPMode mergeExecutionMode = CancelSLTPMode.MergeCancelSLAndTP;
        private BatchMode batchCancelSLMode = BatchMode.MERGE;
        private BatchMode batchCancelTPMode = BatchMode.MERGE;

        public Builder(final Instrument instrument,
                       final String mergeOrderLabel) {
            this.instrument = instrument;
            this.mergeOrderLabel = mergeOrderLabel;
        }

        public Builder withMergeExecutionMode(final CancelSLTPMode mergeExecutionMode) {
            checkNotNull(mergeExecutionMode);

            this.mergeExecutionMode = mergeExecutionMode;
            return this;
        }

        public Builder withBatchCancelSLMode(final BatchMode batchCancelSLMode) {
            checkNotNull(batchCancelSLMode);

            this.batchCancelSLMode = batchCancelSLMode;
            return this;
        }

        public Builder withBatchCancelTPMode(final BatchMode batchCancelTPMode) {
            checkNotNull(batchCancelTPMode);

            this.batchCancelTPMode = batchCancelTPMode;
            return this;
        }

        public Builder withMergePositonParams(final PositionParams mergePositionComposeParams) {
            checkNotNull(mergePositionComposeParams);

            this.mergePositionComposeParams = mergePositionComposeParams;
            return this;
        }

        public Builder withCancelSLTPParams(final PositionParams cancelSLTPComposeParams) {
            checkNotNull(cancelSLTPComposeParams);

            this.cancelSLTPComposeParams = cancelSLTPComposeParams;
            return this;
        }

        public Builder withBatchCancelSLParams(final PositionParams batchCancelSLComposeParams) {
            checkNotNull(batchCancelSLComposeParams);

            this.batchCancelSLComposeParams = batchCancelSLComposeParams;
            return this;
        }

        public Builder withBatchCancelTPParams(final PositionParams batchCancelTPComposeParams) {
            checkNotNull(batchCancelTPComposeParams);

            this.batchCancelTPComposeParams = batchCancelTPComposeParams;
            return this;
        }

        public Builder withCancelSLParams(final Function<IOrder, PositionParams> cancelSLComposeParams) {
            checkNotNull(cancelSLComposeParams);

            this.cancelSLComposeParams = cancelSLComposeParams;
            return this;
        }

        public Builder withCancelTPParams(final Function<IOrder, PositionParams> cancelTPComposeParams) {
            checkNotNull(cancelTPComposeParams);

            this.cancelTPComposeParams = cancelTPComposeParams;
            return this;
        }

        public Builder withMergeParams(final PositionParams mergeComposeParams) {
            checkNotNull(mergeComposeParams);

            this.mergeComposeParams = mergeComposeParams;
            return this;
        }

        public Builder doOnMerge(final Consumer<OrderEvent> mergeConsumer) {
            return setEventConsumer(OrderEventType.MERGE_OK, mergeConsumer);
        }

        public Builder doOnMergeClose(final Consumer<OrderEvent> mergeCloseConsumer) {
            return setEventConsumer(OrderEventType.MERGE_CLOSE_OK, mergeCloseConsumer);
        }

        public Builder doOnMergeReject(final Consumer<OrderEvent> rejectConsumer) {
            return setEventConsumer(OrderEventType.MERGE_REJECTED, rejectConsumer);
        }

        public Builder doOnCancelSL(final Consumer<OrderEvent> cancelSLConsumer) {
            return setEventConsumer(OrderEventType.CHANGED_SL, cancelSLConsumer);
        }

        public Builder doOnCancelSLReject(final Consumer<OrderEvent> cancelSLRejectConsumer) {
            return setEventConsumer(OrderEventType.CHANGE_SL_REJECTED, cancelSLRejectConsumer);
        }

        public Builder doOnCancelTP(final Consumer<OrderEvent> cancelTPConsumer) {
            return setEventConsumer(OrderEventType.CHANGED_TP, cancelTPConsumer);
        }

        public Builder doOnCancelTPReject(final Consumer<OrderEvent> cancelTPRejectConsumer) {
            return setEventConsumer(OrderEventType.CHANGE_TP_REJECTED, cancelTPRejectConsumer);
        }

        public MergePositionParams build() {
            return new MergePositionParams(this);
        }
    }
}
