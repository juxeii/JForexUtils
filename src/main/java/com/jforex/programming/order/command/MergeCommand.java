package com.jforex.programming.order.command;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.function.Function;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.BatchMode;
import com.jforex.programming.order.event.OrderEvent;

import io.reactivex.ObservableTransformer;

public class MergeCommand {

    private final String mergeOrderLabel;
    private final MergeExecutionMode executionMode;
    private final ObservableTransformer<OrderEvent, OrderEvent> cancelSLTPComposer;
    private final ObservableTransformer<OrderEvent, OrderEvent> mergeComposer;
    private final ObservableTransformer<OrderEvent, OrderEvent> cancelSLComposer;
    private final ObservableTransformer<OrderEvent, OrderEvent> cancelTPComposer;
    private final Function<IOrder, ObservableTransformer<OrderEvent, OrderEvent>> orderCancelSLComposer;
    private final Function<IOrder, ObservableTransformer<OrderEvent, OrderEvent>> orderCancelTPComposer;
    private final BatchMode orderCancelSLMode;
    private final BatchMode orderCancelTPMode;

    public interface MergeOption {

        public CancelSLAndTPOption composeCancelSLAndTP(ObservableTransformer<OrderEvent,
                                                                              OrderEvent> cancelSLTPCompose);

        public MergeOption composeMerge(ObservableTransformer<OrderEvent,
                                                              OrderEvent> mergeCompose);

        public MergeCommand build();
    }

    public interface CancelSLAndTPOption {

        public CancelSLAndTPOption composeCancelSL(ObservableTransformer<OrderEvent,
                                                                         OrderEvent> cancelSLCompose);

        public CancelSLAndTPOption composeCancelTP(ObservableTransformer<OrderEvent,
                                                                         OrderEvent> cancelTPCompose);

        public CancelSLAndTPOption composeOrderCancelSL(Function<IOrder,
                                                                 ObservableTransformer<OrderEvent,
                                                                                       OrderEvent>> singleCancelSLCompose,
                                                        BatchMode batchMode);

        public CancelSLAndTPOption composeOrderCancelTP(Function<IOrder,
                                                                 ObservableTransformer<OrderEvent,
                                                                                       OrderEvent>> orderCancelTPComposer,
                                                        BatchMode batchMode);

        public CancelSLAndTPOption withExecutionMode(MergeExecutionMode executionMode);

        public MergeOption done();
    }

    private MergeCommand(final Builder builder) {
        mergeOrderLabel = builder.mergeOrderLabel;
        cancelSLTPComposer = builder.cancelSLTPComposer;
        cancelSLComposer = builder.cancelSLComposer;
        cancelTPComposer = builder.cancelTPComposer;
        orderCancelSLComposer = builder.singleCancelSLComposer;
        orderCancelTPComposer = builder.orderCancelTPComposer;
        executionMode = builder.executionMode;
        mergeComposer = builder.mergeComposer;
        orderCancelSLMode = builder.orderCancelSLMode;
        orderCancelTPMode = builder.orderCancelTPMode;
    }

    public String mergeOrderLabel() {
        return mergeOrderLabel;
    }

    public ObservableTransformer<OrderEvent, OrderEvent> cancelSLTPCompose() {
        return cancelSLTPComposer;
    }

    public ObservableTransformer<OrderEvent, OrderEvent> cancelSLCompose() {
        return cancelSLComposer;
    }

    public ObservableTransformer<OrderEvent, OrderEvent> cancelTPCompose() {
        return cancelTPComposer;
    }

    public ObservableTransformer<OrderEvent, OrderEvent> orderCancelSLComposer(final IOrder order) {
        return orderCancelSLComposer.apply(order);
    }

    public ObservableTransformer<OrderEvent, OrderEvent> orderCancelTPComposer(final IOrder order) {
        return orderCancelTPComposer.apply(order);
    }

    public ObservableTransformer<OrderEvent, OrderEvent> mergeCompose() {
        return mergeComposer;
    }

    public MergeExecutionMode executionMode() {
        return executionMode;
    }

    public BatchMode orderCancelSLMode() {
        return orderCancelSLMode;
    }

    public BatchMode orderCancelTPMode() {
        return orderCancelTPMode;
    }

    public static MergeOption newBuilder(final String mergeOrderLabel) {
        return new Builder(checkNotNull(mergeOrderLabel));
    }

    public static class Builder implements
                                MergeOption,
                                CancelSLAndTPOption {

        private final String mergeOrderLabel;
        private MergeExecutionMode executionMode = MergeExecutionMode.MergeCancelSLAndTP;
        private ObservableTransformer<OrderEvent, OrderEvent> cancelSLTPComposer =
                upstream -> upstream;
        private ObservableTransformer<OrderEvent, OrderEvent> cancelSLComposer =
                upstream -> upstream;
        private ObservableTransformer<OrderEvent, OrderEvent> cancelTPComposer =
                upstream -> upstream;
        private Function<IOrder, ObservableTransformer<OrderEvent, OrderEvent>> singleCancelSLComposer =
                order -> upstream -> upstream;
        private Function<IOrder, ObservableTransformer<OrderEvent, OrderEvent>> orderCancelTPComposer =
                order -> upstream -> upstream;
        private ObservableTransformer<OrderEvent, OrderEvent> mergeComposer =
                upstream -> upstream;
        private BatchMode orderCancelSLMode = BatchMode.MERGE;
        private BatchMode orderCancelTPMode = BatchMode.MERGE;

        public Builder(final String mergeOrderLabel) {
            this.mergeOrderLabel = mergeOrderLabel;
        }

        @Override
        public MergeCommand build() {
            return new MergeCommand(this);
        }

        @Override
        public CancelSLAndTPOption composeCancelSLAndTP(final ObservableTransformer<OrderEvent,
                                                                                    OrderEvent> cancelSLTPCompose) {
            this.cancelSLTPComposer = checkNotNull(cancelSLTPCompose);
            return this;
        }

        @Override
        public MergeOption composeMerge(final ObservableTransformer<OrderEvent, OrderEvent> mergeCompose) {
            this.mergeComposer = checkNotNull(mergeCompose);
            return this;
        }

        @Override
        public CancelSLAndTPOption withExecutionMode(final MergeExecutionMode executionMode) {
            this.executionMode = executionMode;
            return this;
        }

        @Override
        public CancelSLAndTPOption composeOrderCancelSL(final Function<IOrder,
                                                                       ObservableTransformer<OrderEvent,
                                                                                             OrderEvent>> singleCancelSLCompose,
                                                        final BatchMode batchMode) {
            this.singleCancelSLComposer = checkNotNull(singleCancelSLCompose);
            this.orderCancelSLMode = batchMode;
            return this;
        }

        @Override
        public CancelSLAndTPOption composeOrderCancelTP(final Function<IOrder,
                                                                       ObservableTransformer<OrderEvent,
                                                                                             OrderEvent>> orderCancelTPComposer,
                                                        final BatchMode batchMode) {
            this.orderCancelTPComposer = checkNotNull(orderCancelTPComposer);
            this.orderCancelTPMode = batchMode;
            return this;
        }

        @Override
        public CancelSLAndTPOption composeCancelTP(final ObservableTransformer<OrderEvent,
                                                                               OrderEvent> cancelTPCompose) {
            this.cancelTPComposer = checkNotNull(cancelTPCompose);
            return this;
        }

        @Override
        public CancelSLAndTPOption composeCancelSL(final ObservableTransformer<OrderEvent,
                                                                               OrderEvent> cancelSLCompose) {
            this.cancelSLComposer = checkNotNull(cancelSLCompose);
            return this;
        }

        @Override
        public MergeOption done() {
            return this;
        }
    }
}
