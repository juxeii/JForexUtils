package com.jforex.programming.order;

import static com.google.common.base.Preconditions.checkNotNull;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.event.OrderEvent;

import io.reactivex.Observable;
import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Function;

public class MergeCommandWithParent<T> {

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

        public CommandParent<?, MergeCommandWithParent> done();
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

        public CommandParent<?, MergeCommandWithParent> done();
    }

    private MergeCommandWithParent(final Builder builder) {
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

    public final Function<Observable<OrderEvent>, Observable<OrderEvent>> cancelSLTPCompose() {
        return cancelSLTPCompose;
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

    public static MergeOption newBuilder(final CommandParent<?, MergeCommandWithParent> parent,
                                         final String mergeOrderLabel) {
        return new Builder(parent, mergeOrderLabel);
    }

    private static class Builder implements
                                 MergeOption,
                                 CancelSLOption,
                                 CancelTPOption,
                                 ExecutionOption,
                                 MergeComposeOption,
                                 BuildOption {

        private final CommandParent<?, MergeCommandWithParent> parent;
        private final String mergeOrderLabel;
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

        public Builder(final CommandParent<?, MergeCommandWithParent> parent,
                       final String mergeOrderLabel) {
            this.parent = parent;
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
        public CommandParent<?, MergeCommandWithParent> done() {
            return parent.addChild(this.build());;
        }

        private MergeCommandWithParent build() {
            return new MergeCommandWithParent(this);
        }
    }
}
