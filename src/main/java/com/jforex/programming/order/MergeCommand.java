package com.jforex.programming.order;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Optional;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.position.ClosePositionCommand;

import io.reactivex.Observable;
import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Function;

public class MergeCommand {

    private final String mergeOrderLabel;
    private final MergeExecutionMode executionMode;
    private final Function<Observable<OrderEvent>, Observable<OrderEvent>> cancelSLTPCompose;
    private final Function<Observable<OrderEvent>, Observable<OrderEvent>> mergeCompose;
    private final BiFunction<Observable<OrderEvent>, IOrder, Observable<OrderEvent>> cancelSLCompose;
    private final BiFunction<Observable<OrderEvent>, IOrder, Observable<OrderEvent>> cancelTPCompose;

    public enum MergeExecutionMode {
        ConcatSLAndTP,
        ConcatTPAndSL,
        MergeSLAndTP
    }

    public interface MergeOption {

        public CancelSLOption withCancelSLAndTP(Function<Observable<OrderEvent>,
                                                         Observable<OrderEvent>> cancelSLTPCompose);

        public ClosePositionCommand.Builder done();

        public MergeCommand build();
    }

    public interface CancelSLOption {

        public CancelTPOption withCancelSL(BiFunction<Observable<OrderEvent>,
                                                      IOrder,
                                                      Observable<OrderEvent>> cancelSLCompose);
    }

    public interface CancelTPOption {

        public ExecutionOption withCancelTP(BiFunction<Observable<OrderEvent>,
                                                       IOrder,
                                                       Observable<OrderEvent>> cancelTPCompose);
    }

    public interface ExecutionOption {

        public MergeComposeOption withExecutionMode(MergeExecutionMode executionMode);
    }

    public interface MergeComposeOption {

        public BuildOption withMerge(Function<Observable<OrderEvent>,
                                              Observable<OrderEvent>> mergeCompose);
    }

    public interface BuildOption {

        public ClosePositionCommand.Builder done();

        public MergeCommand build();
    }

    private MergeCommand(final Builder builder) {
        mergeOrderLabel = builder.mergeOrderLabel;
        cancelSLTPCompose = builder.cancelSLTPCompose;
        cancelSLCompose = builder.cancelSLCompose;
        cancelTPCompose = builder.cancelTPCompose;
        executionMode = builder.executionMode;
        mergeCompose = builder.mergeCompose;
    }

    public final String mergeOrderLabel() {
        return mergeOrderLabel;
    }

    public final Optional<Function<Observable<OrderEvent>, Observable<OrderEvent>>> maybeCancelSLTPCompose() {
        return Optional.ofNullable(cancelSLTPCompose);
    }

    public final Function<Observable<OrderEvent>, Observable<OrderEvent>> cancelSLCompose(final IOrder order) {
        return obs -> cancelSLCompose.apply(obs, order);
    }

    public final Function<Observable<OrderEvent>, Observable<OrderEvent>> cancelTPCompose(final IOrder order) {
        return obs -> cancelTPCompose.apply(obs, order);
    }

    public final Function<Observable<OrderEvent>, Observable<OrderEvent>> mergeCompose() {
        return mergeCompose;
    }

    public final MergeExecutionMode executionMode() {
        return executionMode;
    }

    public static final MergeOption with(final String mergeOrderLabel) {
        return new Builder(checkNotNull(mergeOrderLabel));
    }

    public static final MergeOption with(final ClosePositionCommand.Builder closePositionCommandBuilder,
                                         final String mergeOrderLabel) {
        return new Builder(checkNotNull(closePositionCommandBuilder),
                           checkNotNull(mergeOrderLabel));
    }

    public static class Builder implements
                                MergeOption,
                                CancelSLOption,
                                CancelTPOption,
                                ExecutionOption,
                                MergeComposeOption,
                                BuildOption {

        private final String mergeOrderLabel;
        private ClosePositionCommand.Builder closePositionCommandBuilder;
        private Function<Observable<OrderEvent>, Observable<OrderEvent>> cancelSLTPCompose;
        private BiFunction<Observable<OrderEvent>,
                           IOrder,
                           Observable<OrderEvent>> cancelSLCompose = (observable, o) -> observable;
        private BiFunction<Observable<OrderEvent>,
                           IOrder,
                           Observable<OrderEvent>> cancelTPCompose = (observable, o) -> observable;
        private Function<Observable<OrderEvent>,
                         Observable<OrderEvent>> mergeCompose = observable -> observable;
        private MergeExecutionMode executionMode;

        public Builder(final String mergeOrderLabel) {
            this.mergeOrderLabel = mergeOrderLabel;
        }

        public Builder(final ClosePositionCommand.Builder closePositionCommandBuilder,
                       final String mergeOrderLabel) {
            this.closePositionCommandBuilder = closePositionCommandBuilder;
            this.mergeOrderLabel = mergeOrderLabel;
        }

        @Override
        public MergeComposeOption withExecutionMode(final MergeExecutionMode executionMode) {
            this.executionMode = executionMode;
            return this;
        }

        @Override
        public ExecutionOption withCancelTP(final BiFunction<Observable<OrderEvent>,
                                                             IOrder,
                                                             Observable<OrderEvent>> cancelTPCompose) {
            this.cancelTPCompose = checkNotNull(cancelTPCompose);
            return this;
        }

        @Override
        public CancelTPOption withCancelSL(final BiFunction<Observable<OrderEvent>,
                                                            IOrder,
                                                            Observable<OrderEvent>> cancelSLCompose) {
            this.cancelSLCompose = checkNotNull(cancelSLCompose);
            return this;
        }

        @Override
        public CancelSLOption withCancelSLAndTP(final Function<Observable<OrderEvent>,
                                                               Observable<OrderEvent>> cancelSLTPCompose) {
            this.cancelSLTPCompose = checkNotNull(cancelSLTPCompose);
            return this;
        }

        @Override
        public BuildOption withMerge(final Function<Observable<OrderEvent>,
                                                    Observable<OrderEvent>> mergeCompose) {
            this.mergeCompose = checkNotNull(mergeCompose);
            return this;
        }

        @Override
        public MergeCommand build() {
            return new MergeCommand(this);
        }

        @Override
        public ClosePositionCommand.Builder done() {
            closePositionCommandBuilder.registerMergeCommand(this.build());
            return closePositionCommandBuilder;
        }
    }
}
