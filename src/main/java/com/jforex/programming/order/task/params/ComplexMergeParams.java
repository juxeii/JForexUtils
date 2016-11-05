package com.jforex.programming.order.task.params;

import static com.google.common.base.Preconditions.checkNotNull;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.event.OrderEventTransformer;
import com.jforex.programming.order.event.OrderToEventTransformer;
import com.jforex.programming.order.task.BatchMode;
import com.jforex.programming.order.task.MergeExecutionMode;

public class ComplexMergeParams {

    private final String mergeOrderLabel;
    private final MergeExecutionMode executionMode;
    private final OrderEventTransformer cancelSLTPComposer;
    private final OrderEventTransformer mergeComposer;
    private final OrderEventTransformer cancelSLComposer;
    private final OrderEventTransformer cancelTPComposer;
    private final OrderToEventTransformer orderCancelSLComposer;
    private final OrderToEventTransformer orderCancelTPComposer;
    private final BatchMode orderCancelSLMode;
    private final BatchMode orderCancelTPMode;

    public interface MergeOption {

        public CancelSLAndTPOption composeCancelSLAndTP(OrderEventTransformer cancelSLTPComposer);

        public MergeOption composeMerge(OrderEventTransformer mergeComposer);

        public ComplexMergeParams build();
    }

    public interface CancelSLAndTPOption {

        public CancelSLAndTPOption composeCancelSL(OrderEventTransformer cancelSLComposer);

        public CancelSLAndTPOption composeCancelTP(OrderEventTransformer cancelTPComposer);

        public CancelSLAndTPOption composeOrderCancelSL(OrderToEventTransformer singleCancelSLComposer,
                                                        BatchMode batchMode);

        public CancelSLAndTPOption composeOrderCancelTP(OrderToEventTransformer orderCancelTPComposer,
                                                        BatchMode batchMode);

        public CancelSLAndTPOption withExecutionMode(MergeExecutionMode executionMode);

        public MergeOption done();
    }

    private ComplexMergeParams(final Builder builder) {
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

    public OrderEventTransformer cancelSLTPComposer() {
        return cancelSLTPComposer;
    }

    public OrderEventTransformer cancelSLComposer() {
        return cancelSLComposer;
    }

    public OrderEventTransformer cancelTPComposer() {
        return cancelTPComposer;
    }

    public OrderEventTransformer orderCancelSLComposer(final IOrder order) {
        return orderCancelSLComposer.apply(order);
    }

    public OrderEventTransformer orderCancelTPComposer(final IOrder order) {
        return orderCancelTPComposer.apply(order);
    }

    public OrderEventTransformer mergeComposer() {
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
        checkNotNull(mergeOrderLabel);

        return new Builder(mergeOrderLabel);
    }

    public static class Builder implements
                                MergeOption,
                                CancelSLAndTPOption {

        private final String mergeOrderLabel;
        private MergeExecutionMode executionMode = MergeExecutionMode.MergeCancelSLAndTP;
        private OrderEventTransformer cancelSLTPComposer =
                upstream -> upstream;
        private OrderEventTransformer cancelSLComposer =
                upstream -> upstream;
        private OrderEventTransformer cancelTPComposer =
                upstream -> upstream;
        private OrderToEventTransformer singleCancelSLComposer =
                order -> upstream -> upstream;
        private OrderToEventTransformer orderCancelTPComposer =
                order -> upstream -> upstream;
        private OrderEventTransformer mergeComposer =
                upstream -> upstream;
        private BatchMode orderCancelSLMode = BatchMode.MERGE;
        private BatchMode orderCancelTPMode = BatchMode.MERGE;

        public Builder(final String mergeOrderLabel) {
            this.mergeOrderLabel = mergeOrderLabel;
        }

        @Override
        public ComplexMergeParams build() {
            return new ComplexMergeParams(this);
        }

        @Override
        public CancelSLAndTPOption composeCancelSLAndTP(final OrderEventTransformer cancelSLTPComposer) {
            this.cancelSLTPComposer = checkNotNull(cancelSLTPComposer);
            return this;
        }

        @Override
        public MergeOption composeMerge(final OrderEventTransformer mergeComposer) {
            this.mergeComposer = checkNotNull(mergeComposer);
            return this;
        }

        @Override
        public CancelSLAndTPOption withExecutionMode(final MergeExecutionMode executionMode) {
            this.executionMode = executionMode;
            return this;
        }

        @Override
        public CancelSLAndTPOption composeOrderCancelSL(final OrderToEventTransformer singleCancelSLComposer,
                                                        final BatchMode batchMode) {
            this.singleCancelSLComposer = checkNotNull(singleCancelSLComposer);
            this.orderCancelSLMode = batchMode;
            return this;
        }

        @Override
        public CancelSLAndTPOption composeOrderCancelTP(final OrderToEventTransformer orderCancelTPComposer,
                                                        final BatchMode batchMode) {
            this.orderCancelTPComposer = checkNotNull(orderCancelTPComposer);
            this.orderCancelTPMode = batchMode;
            return this;
        }

        @Override
        public CancelSLAndTPOption composeCancelTP(final OrderEventTransformer cancelTPComposer) {
            this.cancelTPComposer = checkNotNull(cancelTPComposer);
            return this;
        }

        @Override
        public CancelSLAndTPOption composeCancelSL(final OrderEventTransformer cancelSLComposer) {
            this.cancelSLComposer = checkNotNull(cancelSLComposer);
            return this;
        }

        @Override
        public MergeOption done() {
            return this;
        }
    }
}
