package com.jforex.programming.order.command;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Optional;

import com.dukascopy.api.IOrder;
import com.dukascopy.api.Instrument;
import com.jforex.programming.order.event.OrderEventTransformer;
import com.jforex.programming.order.event.OrderToEventTransformer;
import com.jforex.programming.order.task.BatchMode;
import com.jforex.programming.order.task.CloseExecutionMode;

import io.reactivex.functions.Function;

public class ClosePositionCommand {

    private final Instrument instrument;
    private final Function<IOrder, CloseParams> closeParamsProvider;
    private final CloseExecutionMode executionMode;
    private final OrderEventTransformer closeFilledComposer;
    private final OrderEventTransformer closeOpenedComposer;
    private final OrderEventTransformer closeAllComposer;
    private final OrderToEventTransformer singleCloseComposer;
    private final Optional<MergeCommand> maybeMergeCommand;
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

        BuildOption withMergeCommand(MergeCommand maybeMergeCommand);
    }

    public interface BuildOption {

        public ClosePositionCommand build();
    }

    private ClosePositionCommand(final Builder builder) {
        instrument = builder.instrument;
        closeParamsProvider = builder.closeParamsProvider;
        executionMode = builder.executionMode;
        closeFilledComposer = builder.closeFilledComposer;
        closeOpenedComposer = builder.closeOpenedComposer;
        closeAllComposer = builder.closeAllComposer;
        singleCloseComposer = builder.singleCloseComposer;
        maybeMergeCommand = builder.maybeMergeCommand;
        closeBatchMode = builder.closeBatchMode;
    }

    public Instrument instrument() {
        return instrument;
    }

    public Function<IOrder, CloseParams> closeParamsProvider() {
        return closeParamsProvider;
    }

    public Optional<MergeCommand> maybeMergeCommand() {
        return maybeMergeCommand;
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

    public static CloseOption newBuilder(final Instrument instrument,
                                         final Function<IOrder, CloseParams> closeParamsProvider) {
        checkNotNull(instrument);
        checkNotNull(closeParamsProvider);

        return new Builder(instrument, closeParamsProvider);
    }

    public static class Builder implements
                                CloseOption,
                                MergeForCloseOption,
                                BuildOption {

        private final Instrument instrument;
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
        private Optional<MergeCommand> maybeMergeCommand = Optional.empty();
        private BatchMode closeBatchMode;

        private Builder(final Instrument instrument,
                        final Function<IOrder, CloseParams> closeParamsProvider) {
            this.instrument = instrument;
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
        public BuildOption withMergeCommand(final MergeCommand maybeMergeCommand) {
            this.maybeMergeCommand = Optional.ofNullable(maybeMergeCommand);
            return this;
        }

        @Override
        public ClosePositionCommand build() {
            return new ClosePositionCommand(this);
        }
    }
}
