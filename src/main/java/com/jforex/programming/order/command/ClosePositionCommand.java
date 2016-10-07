package com.jforex.programming.order.command;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Optional;
import java.util.function.Function;

import com.dukascopy.api.IOrder;
import com.dukascopy.api.Instrument;
import com.jforex.programming.order.BatchMode;
import com.jforex.programming.order.event.OrderEvent;

import io.reactivex.ObservableTransformer;

public class ClosePositionCommand {

    private final Instrument instrument;
    private final CloseExecutionMode executionMode;
    private final ObservableTransformer<OrderEvent, OrderEvent> closeFilledComposer;
    private final ObservableTransformer<OrderEvent, OrderEvent> closeOpenedComposer;
    private final ObservableTransformer<OrderEvent, OrderEvent> closeAllComposer;
    private final Function<IOrder, ObservableTransformer<OrderEvent, OrderEvent>> singleCloseComposer;
    private final Optional<MergeCommand> maybeMergeCommand;
    private final BatchMode closeBatchMode;

    public interface CloseOption {

        public CloseOption singleCloseComposer(Function<IOrder,
                                                        ObservableTransformer<OrderEvent,
                                                                              OrderEvent>> singleCloseComposer);

        public MergeForCloseOption closeFilledComposer(ObservableTransformer<OrderEvent,
                                                                             OrderEvent> closeFilledComposer,
                                                       BatchMode batchMode);

        public BuildOption closeOpenedComposer(ObservableTransformer<OrderEvent,
                                                                     OrderEvent> closeOpenedComposer,
                                               BatchMode batchMode);

        public MergeForCloseOption closeAllComposer(ObservableTransformer<OrderEvent,
                                                                          OrderEvent> closeAllComposer,
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

    public ObservableTransformer<OrderEvent, OrderEvent> closeFilledComposer() {
        return closeFilledComposer;
    }

    public ObservableTransformer<OrderEvent, OrderEvent> closeOpenedComposer() {
        return closeOpenedComposer;
    }

    public ObservableTransformer<OrderEvent, OrderEvent> closeAllComposer() {
        return closeAllComposer;
    }

    public ObservableTransformer<OrderEvent, OrderEvent> singleCloseComposer(final IOrder orderToClose) {
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
        private ObservableTransformer<OrderEvent, OrderEvent> closeFilledComposer =
                upstream -> upstream;
        private ObservableTransformer<OrderEvent, OrderEvent> closeOpenedComposer =
                upstream -> upstream;
        private ObservableTransformer<OrderEvent, OrderEvent> closeAllComposer =
                upstream -> upstream;
        private Function<IOrder, ObservableTransformer<OrderEvent, OrderEvent>> singleCloseComposer =
                order -> upstream -> upstream;
        private Optional<MergeCommand> maybeMergeCommand = Optional.empty();
        private BatchMode closeBatchMode;

        private Builder(final Instrument instrument) {
            this.instrument = instrument;
        }

        @Override
        public CloseOption singleCloseComposer(final Function<IOrder,
                                                              ObservableTransformer<OrderEvent,
                                                                                    OrderEvent>> singleCloseComposer) {
            this.singleCloseComposer = checkNotNull(singleCloseComposer);
            return this;
        }

        @Override
        public MergeForCloseOption closeFilledComposer(final ObservableTransformer<OrderEvent,
                                                                                   OrderEvent> closeFilledComposer,
                                                       final BatchMode batchMode) {
            this.executionMode = CloseExecutionMode.CloseFilled;
            this.closeFilledComposer = checkNotNull(closeFilledComposer);
            this.closeBatchMode = batchMode;
            return this;
        }

        @Override
        public BuildOption closeOpenedComposer(final ObservableTransformer<OrderEvent,
                                                                           OrderEvent> closeOpenedComposer,
                                               final BatchMode batchMode) {
            this.executionMode = CloseExecutionMode.CloseOpened;
            this.closeOpenedComposer = checkNotNull(closeOpenedComposer);
            this.closeBatchMode = batchMode;
            return this;
        }

        @Override
        public MergeForCloseOption closeAllComposer(final ObservableTransformer<OrderEvent,
                                                                                OrderEvent> closeAllComposer,
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
