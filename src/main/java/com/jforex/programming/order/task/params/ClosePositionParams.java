package com.jforex.programming.order.task.params;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Optional;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.event.OrderEventTransformer;
import com.jforex.programming.order.event.OrderToEventTransformer;
import com.jforex.programming.order.task.BatchMode;
import com.jforex.programming.order.task.CloseExecutionMode;

import io.reactivex.functions.Function;

public class ClosePositionParams {

    private final Function<IOrder, CloseParams> closeParamsProvider;
    private final CloseExecutionMode executionMode;
    private final OrderEventTransformer closeFilledComposer;
    private final OrderEventTransformer closeOpenedComposer;
    private final OrderEventTransformer closeAllComposer;
    private final OrderToEventTransformer singleCloseComposer;
    private final Optional<MergePositionParams> maybeMergeParams;
    private final BatchMode closeBatchMode;

    public interface CloseOption {

        public CloseOption singleCloseComposer(OrderToEventTransformer singleCloseComposer);

        public MergeForCloseOption closeFilledComposer(OrderEventTransformer closeFilledComposer,
                                                       BatchMode batchMode);

        public BuildOption closeOpenedComposer(OrderEventTransformer closeOpenedComposer,
                                               BatchMode batchMode);

        public MergeForCloseOption closeAllComposer(OrderEventTransformer closeAllComposer,
                                                    BatchMode batchMode);
    }

    public interface MergeForCloseOption {

        BuildOption withMergeParams(MergePositionParams maybeMergeParams);
    }

    public interface BuildOption {

        public ClosePositionParams build();
    }

    private ClosePositionParams(final Builder builder) {
        closeParamsProvider = builder.closeParamsProvider;
        executionMode = builder.executionMode;
        closeFilledComposer = builder.closeFilledComposer;
        closeOpenedComposer = builder.closeOpenedComposer;
        closeAllComposer = builder.closeAllComposer;
        singleCloseComposer = builder.singleCloseComposer;
        maybeMergeParams = builder.maybeMergeParams;
        closeBatchMode = builder.closeBatchMode;
    }

    public Function<IOrder, CloseParams> closeParamsProvider() {
        return closeParamsProvider;
    }

    public Optional<MergePositionParams> maybeMergeParams() {
        return maybeMergeParams;
    }

    public CloseExecutionMode executionMode() {
        return executionMode;
    }

    public OrderEventTransformer closeFilledComposer() {
        return closeFilledComposer;
    }

    public OrderEventTransformer closeOpenedComposer() {
        return closeOpenedComposer;
    }

    public OrderEventTransformer closeAllComposer() {
        return closeAllComposer;
    }

    public OrderEventTransformer singleCloseComposer(final IOrder orderToClose) {
        return singleCloseComposer.apply(orderToClose);
    }

    public BatchMode closeBatchMode() {
        return closeBatchMode;
    }

    public static CloseOption newBuilder(final Function<IOrder, CloseParams> closeParamsProvider) {
        checkNotNull(closeParamsProvider);

        return new Builder(closeParamsProvider);
    }

    public static class Builder implements
                                CloseOption,
                                MergeForCloseOption,
                                BuildOption {

        private final Function<IOrder, CloseParams> closeParamsProvider;
        private CloseExecutionMode executionMode;
        private OrderEventTransformer closeFilledComposer =
                upstream -> upstream;
        private OrderEventTransformer closeOpenedComposer =
                upstream -> upstream;
        private OrderEventTransformer closeAllComposer =
                upstream -> upstream;
        private OrderToEventTransformer singleCloseComposer =
                order -> upstream -> upstream;
        private Optional<MergePositionParams> maybeMergeParams = Optional.empty();
        private BatchMode closeBatchMode;

        private Builder(final Function<IOrder, CloseParams> closeParamsProvider) {
            this.closeParamsProvider = closeParamsProvider;
        }

        @Override
        public CloseOption singleCloseComposer(final OrderToEventTransformer singleCloseComposer) {
            checkNotNull(singleCloseComposer);

            this.singleCloseComposer = singleCloseComposer;
            return this;
        }

        @Override
        public MergeForCloseOption closeFilledComposer(final OrderEventTransformer closeFilledComposer,
                                                       final BatchMode batchMode) {
            checkNotNull(closeFilledComposer);

            this.executionMode = CloseExecutionMode.CloseFilled;
            this.closeFilledComposer = closeFilledComposer;
            this.closeBatchMode = batchMode;
            return this;
        }

        @Override
        public BuildOption closeOpenedComposer(final OrderEventTransformer closeOpenedComposer,
                                               final BatchMode batchMode) {
            checkNotNull(closeOpenedComposer);

            this.executionMode = CloseExecutionMode.CloseOpened;
            this.closeOpenedComposer = closeOpenedComposer;
            this.closeBatchMode = batchMode;
            return this;
        }

        @Override
        public MergeForCloseOption closeAllComposer(final OrderEventTransformer closeAllComposer,
                                                    final BatchMode batchMode) {
            checkNotNull(closeFilledComposer);

            this.executionMode = CloseExecutionMode.CloseAll;
            this.closeAllComposer = closeAllComposer;
            this.closeBatchMode = batchMode;
            return this;
        }

        @Override
        public BuildOption withMergeParams(final MergePositionParams maybeMergeParams) {
            this.maybeMergeParams = Optional.ofNullable(maybeMergeParams);
            return this;
        }

        @Override
        public ClosePositionParams build() {
            return new ClosePositionParams(this);
        }
    }
}
