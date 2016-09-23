package com.jforex.programming.order.command;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.function.BiFunction;

import com.dukascopy.api.IOrder;
import com.dukascopy.api.Instrument;
import com.jforex.programming.order.event.OrderEvent;

import io.reactivex.Observable;
import io.reactivex.functions.Function;

public class ClosePositionCommand {

    private final Instrument instrument;
    private final CloseExecutionMode executionMode;
    private final Function<Observable<OrderEvent>, Observable<OrderEvent>> closeFilledCompose;
    private final Function<Observable<OrderEvent>, Observable<OrderEvent>> closeOpenedCompose;
    private final Function<Observable<OrderEvent>, Observable<OrderEvent>> closeAllCompose;
    private final BiFunction<Observable<OrderEvent>, IOrder, Observable<OrderEvent>> singleCloseCompose;
    private final MergeCommand mergeCommand;

    public enum CloseExecutionMode {
        CloseFilled,
        CloseOpened,
        CloseAll
    }

    public interface SingleCloseOption {

        CloseOption singleCloseCompose(BiFunction<Observable<OrderEvent>,
                                                  IOrder,
                                                  Observable<OrderEvent>> singleCloseCompose);
    }

    public interface CloseOption {

        public MergeForCloseOption closeFilled(Function<Observable<OrderEvent>,
                                                        Observable<OrderEvent>> closeFilledCompose);

        public BuildOption closeOpened(Function<Observable<OrderEvent>,
                                                Observable<OrderEvent>> closeOpenedCompose);

        public MergeForCloseOption closeAll(Function<Observable<OrderEvent>,
                                                     Observable<OrderEvent>> closeAllCompose);
    }

    public interface MergeForCloseOption {

        BuildOption withMergeCommand(MergeCommand mergeCommand);
    }

    public interface BuildOption {

        public ClosePositionCommand build();
    }

    private ClosePositionCommand(final Builder builder) {
        instrument = builder.instrument;
        executionMode = builder.executionMode;
        closeFilledCompose = builder.closeFilledCompose;
        closeOpenedCompose = builder.closeOpenedCompose;
        closeAllCompose = builder.closeAllCompose;
        singleCloseCompose = builder.singleCloseCompose;
        mergeCommand = builder.mergeCommand;
    }

    public final Instrument instrument() {
        return instrument;
    }

    public final MergeCommand commonMergeCommand() {
        return mergeCommand;
    }

    public final CloseExecutionMode executionMode() {
        return executionMode;
    }

    public final Function<Observable<OrderEvent>, Observable<OrderEvent>> closeFilledCompose() {
        return closeFilledCompose;
    }

    public final Function<Observable<OrderEvent>, Observable<OrderEvent>> closeOpenedCompose() {
        return closeOpenedCompose;
    }

    public final Function<Observable<OrderEvent>, Observable<OrderEvent>> closeAllCompose() {
        return closeAllCompose;
    }

    public final Function<Observable<OrderEvent>,
                          Observable<OrderEvent>>
           singleCloseCompose(final IOrder orderToClose) {
        return obs -> singleCloseCompose.apply(obs, orderToClose);
    }

    public static SingleCloseOption newBuilder(final Instrument instrument) {
        return new Builder(checkNotNull(instrument));
    }

    public static class Builder implements
                                CloseOption,
                                MergeForCloseOption,
                                SingleCloseOption,
                                BuildOption {

        private final Instrument instrument;
        private CloseExecutionMode executionMode;
        private Function<Observable<OrderEvent>, Observable<OrderEvent>> closeFilledCompose =
                observable -> observable;
        private Function<Observable<OrderEvent>, Observable<OrderEvent>> closeOpenedCompose =
                observable -> observable;
        private Function<Observable<OrderEvent>, Observable<OrderEvent>> closeAllCompose =
                observable -> observable;
        private BiFunction<Observable<OrderEvent>, IOrder, Observable<OrderEvent>> singleCloseCompose =
                (observable, order) -> observable;
        private MergeCommand mergeCommand;

        private Builder(final Instrument instrument) {
            this.instrument = instrument;
        }

        @Override
        public CloseOption singleCloseCompose(final BiFunction<Observable<OrderEvent>,
                                                               IOrder,
                                                               Observable<OrderEvent>> singleCloseCompose) {
            this.singleCloseCompose = checkNotNull(singleCloseCompose);
            return this;
        }

        @Override
        public MergeForCloseOption closeFilled(final Function<Observable<OrderEvent>,
                                                              Observable<OrderEvent>> closeFilledCompose) {
            this.executionMode = CloseExecutionMode.CloseFilled;
            this.closeFilledCompose = checkNotNull(closeFilledCompose);
            return this;
        }

        @Override
        public BuildOption closeOpened(final Function<Observable<OrderEvent>,
                                                      Observable<OrderEvent>> closeOpenedCompose) {
            this.executionMode = CloseExecutionMode.CloseOpened;
            this.closeOpenedCompose = checkNotNull(closeOpenedCompose);
            return this;
        }

        @Override
        public MergeForCloseOption closeAll(final Function<Observable<OrderEvent>,
                                                           Observable<OrderEvent>> closeAllCompose) {
            this.executionMode = CloseExecutionMode.CloseAll;
            this.closeAllCompose = checkNotNull(closeAllCompose);
            return this;
        }

        @Override
        public BuildOption withMergeCommand(final MergeCommand mergeCommand) {
            this.mergeCommand = checkNotNull(mergeCommand);
            return this;
        }

        @Override
        public ClosePositionCommand build() {
            return new ClosePositionCommand(this);
        }
    }
}
