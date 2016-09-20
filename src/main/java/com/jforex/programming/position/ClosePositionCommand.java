package com.jforex.programming.position;

import static com.google.common.base.Preconditions.checkNotNull;

import com.dukascopy.api.IOrder;
import com.dukascopy.api.Instrument;
import com.jforex.programming.order.MergeCommand;
import com.jforex.programming.order.MergeCommand.MergeOption;
import com.jforex.programming.order.event.OrderEvent;

import io.reactivex.Observable;
import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Function;

public class ClosePositionCommand {

    private final Instrument instrument;
    private final String mergeOrderLabel;
    private final CloseExecutionMode executionMode;
    private final BiFunction<Observable<OrderEvent>, IOrder, Observable<OrderEvent>> closeFilledCompose;
    private final BiFunction<Observable<OrderEvent>, IOrder, Observable<OrderEvent>> closeOpenedCompose;
    private final BiFunction<Observable<OrderEvent>, IOrder, Observable<OrderEvent>> closeAllCompose;
    private final MergeCommand mergeCommand;

    public enum CloseExecutionMode {
        CloseFilled,
        CloseOpened,
        CloseAll
    }

    public interface CloseOption {

        public MergeForCloseOption closeFilled(BiFunction<Observable<OrderEvent>,
                                                          IOrder,
                                                          Observable<OrderEvent>> closeFilledCompose);

        public BuildOption closeOpened(BiFunction<Observable<OrderEvent>,
                                                  IOrder,
                                                  Observable<OrderEvent>> closeOpenedCompose);

        public MergeForCloseOption closeAll(BiFunction<Observable<OrderEvent>,
                                                       IOrder,
                                                       Observable<OrderEvent>> closeAllCompose);
    }

    public interface MergeForCloseOption {

        public MergeOption withMergeCommand();
    }

    public interface BuildOption {

        public ClosePositionCommand build();
    }

    private ClosePositionCommand(final Builder builder) {
        instrument = builder.instrument;
        mergeOrderLabel = builder.mergeOrderLabel;
        executionMode = builder.executionMode;
        closeFilledCompose = builder.closeFilledCompose;
        closeOpenedCompose = builder.closeOpenedCompose;
        closeAllCompose = builder.closeAllCompose;
        mergeCommand = builder.mergeCommand;
    }

    public static final CloseOption with(final Instrument instrument,
                                         final String mergeOrderLabel) {
        return new Builder(checkNotNull(instrument),
                           checkNotNull(mergeOrderLabel));
    }

    public final MergeCommand mergeCommand() {
        return mergeCommand;
    }

    public final Instrument instrument() {
        return instrument;
    }

    public final String mergeOrderLabel() {
        return mergeOrderLabel;
    }

    public final CloseExecutionMode executionMode() {
        return executionMode;
    }

    public final Function<Observable<OrderEvent>, Observable<OrderEvent>> closeFilledCompose(final IOrder order) {
        return obs -> closeFilledCompose.apply(obs, order);
    }

    public final Function<Observable<OrderEvent>, Observable<OrderEvent>> closeOpenedCompose(final IOrder order) {
        return obs -> closeOpenedCompose.apply(obs, order);
    }

    public final Function<Observable<OrderEvent>, Observable<OrderEvent>> closeAllCompose(final IOrder order) {
        return obs -> closeAllCompose.apply(obs, order);
    }

    public static class Builder implements
                                CloseOption,
                                MergeForCloseOption,
                                BuildOption {

        private final Instrument instrument;
        private final String mergeOrderLabel;
        private CloseExecutionMode executionMode;
        private BiFunction<Observable<OrderEvent>, IOrder, Observable<OrderEvent>> closeFilledCompose;
        private BiFunction<Observable<OrderEvent>, IOrder, Observable<OrderEvent>> closeOpenedCompose;
        private BiFunction<Observable<OrderEvent>, IOrder, Observable<OrderEvent>> closeAllCompose;
        private MergeCommand mergeCommand;

        private Builder(final Instrument instrument,
                        final String mergeOrderLabel) {
            this.instrument = instrument;
            this.mergeOrderLabel = mergeOrderLabel;
        }

        @Override
        public MergeForCloseOption closeFilled(final BiFunction<Observable<OrderEvent>,
                                                                IOrder,
                                                                Observable<OrderEvent>> closeFilledCompose) {
            this.executionMode = CloseExecutionMode.CloseFilled;
            this.closeFilledCompose = checkNotNull(closeFilledCompose);
            return this;
        }

        @Override
        public BuildOption closeOpened(final BiFunction<Observable<OrderEvent>,
                                                        IOrder,
                                                        Observable<OrderEvent>> closeOpenedCompose) {
            this.executionMode = CloseExecutionMode.CloseOpened;
            this.closeOpenedCompose = checkNotNull(closeOpenedCompose);
            return this;
        }

        @Override
        public MergeForCloseOption closeAll(final BiFunction<Observable<OrderEvent>,
                                                             IOrder,
                                                             Observable<OrderEvent>> closeAllCompose) {
            this.executionMode = CloseExecutionMode.CloseAll;
            this.closeAllCompose = checkNotNull(closeAllCompose);
            return this;
        }

        @Override
        public MergeOption withMergeCommand() {
            return MergeCommand.with(this, mergeOrderLabel);
        }

        public void registerMergeCommand(final MergeCommand mergeCommand) {
            this.mergeCommand = mergeCommand;
        }

        @Override
        public ClosePositionCommand build() {
            return new ClosePositionCommand(this);
        }
    }
}
