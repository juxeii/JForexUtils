package com.jforex.programming.position;

import static com.google.common.base.Preconditions.checkNotNull;

import com.dukascopy.api.IOrder;
import com.dukascopy.api.Instrument;
import com.jforex.programming.order.CommandParent;
import com.jforex.programming.order.MergeCommandWithParent;
import com.jforex.programming.order.event.OrderEvent;

import io.reactivex.Observable;
import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Function;

public class ClosePositionCommand {

    private final Instrument instrument;
    private final CloseExecutionMode executionMode;
    private final BiFunction<Observable<OrderEvent>, IOrder, Observable<OrderEvent>> closeFilledCompose;
    private final BiFunction<Observable<OrderEvent>, IOrder, Observable<OrderEvent>> closeOpenedCompose;
    private final BiFunction<Observable<OrderEvent>, IOrder, Observable<OrderEvent>> closeAllCompose;
    private final MergeCommandWithParent mergeCommandWithParent;

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

        MergeCommandWithParent.MergeOption<BuildOption> withMergeOption(String mergeOrderLabel);
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
        mergeCommandWithParent = builder.mergeCommandWithParent;
    }

    public final Instrument instrument() {
        return instrument;
    }

    public final MergeCommandWithParent mergeCommandWithParent() {
        return mergeCommandWithParent;
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

    public static CloseOption newBuilder(final Instrument instrument) {
        return new Builder(checkNotNull(instrument));
    }

    public static class Builder implements
                                CloseOption,
                                MergeForCloseOption,
                                CommandParent<BuildOption>,
                                BuildOption {

        private final Instrument instrument;
        private CloseExecutionMode executionMode;
        private BiFunction<Observable<OrderEvent>, IOrder, Observable<OrderEvent>> closeFilledCompose;
        private BiFunction<Observable<OrderEvent>, IOrder, Observable<OrderEvent>> closeOpenedCompose;
        private BiFunction<Observable<OrderEvent>, IOrder, Observable<OrderEvent>> closeAllCompose;
        private MergeCommandWithParent mergeCommandWithParent;
        private MergeCommandWithParent.MergeOption<BuildOption> option;

        private Builder(final Instrument instrument) {
            this.instrument = instrument;
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
        public ClosePositionCommand build() {
            return new ClosePositionCommand(this);
        }

        @Override
        public MergeCommandWithParent.MergeOption<BuildOption> withMergeOption(final String mergeOrderLabel) {
            option = MergeCommandWithParent.newBuilder(this, mergeOrderLabel);
            return option;
        }

        @Override
        public void addChild(final Object mergeCommandWithParent) {
            this.mergeCommandWithParent = (MergeCommandWithParent) mergeCommandWithParent;
        }
    }
}
