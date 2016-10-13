package com.jforex.programming.order.command;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Optional;

import com.dukascopy.api.IOrder;
import com.dukascopy.api.Instrument;
import com.jforex.programming.order.event.OrderEventTransformer;
import com.jforex.programming.order.event.OrderToEventTransformer;
import com.jforex.programming.order.task.BatchMode;

public class ClosePositionCommand {

    private final Instrument instrument;
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

    public static CloseOption newBuilder(final Instrument instrument) {
        return new Builder(checkNotNull(instrument));
    }

    public static class Builder implements
                                CloseOption,
                                MergeForCloseOption,
                                BuildOption {

        private final Instrument instrument;
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

        private Builder(final Instrument instrument) {
            this.instrument = instrument;
        }

        @Override
        public CloseOption singleCloseComposer(final OrderToEventTransformer singleCloseComposer) {
            this.singleCloseComposer = checkNotNull(singleCloseComposer);
            return this;
        }

        @Override
        public MergeForCloseOption closeFilledComposer(final OrderEventTransformer closeFilledComposer,
                                                       final BatchMode batchMode) {
            this.executionMode = CloseExecutionMode.CloseFilled;
            this.closeFilledComposer = checkNotNull(closeFilledComposer);
            this.closeBatchMode = batchMode;
            return this;
        }

        @Override
        public BuildOption closeOpenedComposer(final OrderEventTransformer closeOpenedComposer,
                                               final BatchMode batchMode) {
            this.executionMode = CloseExecutionMode.CloseOpened;
            this.closeOpenedComposer = checkNotNull(closeOpenedComposer);
            this.closeBatchMode = batchMode;
            return this;
        }

        @Override
        public MergeForCloseOption closeAllComposer(final OrderEventTransformer closeAllComposer,
                                                    final BatchMode batchMode) {
            this.executionMode = CloseExecutionMode.CloseAll;
            this.closeAllComposer = checkNotNull(closeAllComposer);
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
