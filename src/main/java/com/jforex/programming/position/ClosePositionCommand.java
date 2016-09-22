package com.jforex.programming.position;

import static com.google.common.base.Preconditions.checkNotNull;

import com.dukascopy.api.Instrument;
import com.jforex.programming.order.CommandParent;
import com.jforex.programming.order.CommonMergeCommand;
import com.jforex.programming.order.event.OrderEvent;

import io.reactivex.Observable;
import io.reactivex.functions.Function;

public class ClosePositionCommand {

    private final Instrument instrument;
    private final CloseExecutionMode executionMode;
    private final Function<Observable<OrderEvent>, Observable<OrderEvent>> closeFilledCompose;
    private final Function<Observable<OrderEvent>, Observable<OrderEvent>> closeOpenedCompose;
    private final Function<Observable<OrderEvent>, Observable<OrderEvent>> closeAllCompose;
    private final CommonMergeCommand mergeCommandWithParent;

    public enum CloseExecutionMode {
        CloseFilled,
        CloseOpened,
        CloseAll
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

        CommonMergeCommand.MergeOption<BuildOption> withMergeOption(String mergeOrderLabel);
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

    public final CommonMergeCommand commonMergeCommand() {
        return mergeCommandWithParent;
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
        private Function<Observable<OrderEvent>, Observable<OrderEvent>> closeFilledCompose;
        private Function<Observable<OrderEvent>, Observable<OrderEvent>> closeOpenedCompose;
        private Function<Observable<OrderEvent>, Observable<OrderEvent>> closeAllCompose;
        private CommonMergeCommand mergeCommandWithParent;
        private CommonMergeCommand.MergeOption<BuildOption> option;

        private Builder(final Instrument instrument) {
            this.instrument = instrument;
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
        public ClosePositionCommand build() {
            return new ClosePositionCommand(this);
        }

        @Override
        public CommonMergeCommand.MergeOption<BuildOption> withMergeOption(final String mergeOrderLabel) {
            option = CommonMergeCommand.newBuilder(this, mergeOrderLabel);
            return option;
        }

        @Override
        public void addChild(final Object mergeCommandWithParent) {
            this.mergeCommandWithParent = (CommonMergeCommand) mergeCommandWithParent;
        }
    }
}
