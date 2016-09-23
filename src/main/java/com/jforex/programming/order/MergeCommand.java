package com.jforex.programming.order;

import static com.google.common.base.Preconditions.checkNotNull;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.event.OrderEvent;

import io.reactivex.Observable;
import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Function;

public class MergeCommand {

    private final String mergeOrderLabel;
    private final MergeExecutionMode executionMode;
    private final Function<Observable<OrderEvent>, Observable<OrderEvent>> cancelSLTPCompose;
    private final Function<Observable<OrderEvent>, Observable<OrderEvent>> mergeCompose;
    private final Function<Observable<OrderEvent>, Observable<OrderEvent>> cancelSLCompose;
    private final Function<Observable<OrderEvent>, Observable<OrderEvent>> cancelTPCompose;
    private final BiFunction<Observable<OrderEvent>, IOrder, Observable<OrderEvent>> singleCancelSLCompose;
    private final BiFunction<Observable<OrderEvent>, IOrder, Observable<OrderEvent>> singleCancelTPCompose;

    public enum MergeExecutionMode {
        ConcatSLAndTP,
        ConcatTPAndSL,
        MergeSLAndTP
    }

    public interface MergeOption {

        public CancelSLOption withCancelSLAndTP(Function<Observable<OrderEvent>,
                                                         Observable<OrderEvent>> cancelSLTPCompose);

        public MergeCommand build();
    }

    public interface CancelSLOption {

        public SingleCancelSLOption withCancelSL(Function<Observable<OrderEvent>,
                                                          Observable<OrderEvent>> cancelSLCompose);
    }

    public interface SingleCancelSLOption {

        public CancelTPOption withSingleCancelSL(BiFunction<Observable<OrderEvent>,
                                                            IOrder,
                                                            Observable<OrderEvent>> singleCancelSLCompose);
    }

    public interface CancelTPOption {

        public SingleCancelTPOption withCancelTP(Function<Observable<OrderEvent>,
                                                          Observable<OrderEvent>> cancelTPCompose);
    }

    public interface SingleCancelTPOption {

        public ExecutionOption withSingleCancelTP(BiFunction<Observable<OrderEvent>,
                                                             IOrder,
                                                             Observable<OrderEvent>> singleCancelTPCompose);
    }

    public interface ExecutionOption {

        public MergeComposeOption withExecutionMode(MergeExecutionMode executionMode);
    }

    public interface MergeComposeOption {

        public BuildOption withMerge(Function<Observable<OrderEvent>,
                                              Observable<OrderEvent>> mergeCompose);
    }

    public interface BuildOption {

        public MergeCommand build();
    }

    private MergeCommand(final Builder builder) {
        mergeOrderLabel = builder.mergeOrderLabel;
        cancelSLTPCompose = builder.cancelSLTPCompose;
        cancelSLCompose = builder.cancelSLCompose;
        cancelTPCompose = builder.cancelTPCompose;
        singleCancelSLCompose = builder.singleCancelSLCompose;
        singleCancelTPCompose = builder.singleCancelTPCompose;
        executionMode = builder.executionMode;
        mergeCompose = builder.mergeCompose;
    }

    public final String mergeOrderLabel() {
        return mergeOrderLabel;
    }

    public final Function<Observable<OrderEvent>, Observable<OrderEvent>> cancelSLTPCompose() {
        return cancelSLTPCompose;
    }

    public final Function<Observable<OrderEvent>, Observable<OrderEvent>> cancelSLCompose() {
        return cancelSLCompose;
    }

    public final Function<Observable<OrderEvent>, Observable<OrderEvent>> cancelTPCompose() {
        return cancelTPCompose;
    }

    public final Function<Observable<OrderEvent>, Observable<OrderEvent>> singleCancelSLCompose(final IOrder order) {
        return obs -> singleCancelSLCompose.apply(obs, order);
    }

    public final Function<Observable<OrderEvent>, Observable<OrderEvent>> singleCancelTPCompose(final IOrder order) {
        return obs -> singleCancelTPCompose.apply(obs, order);
    }

    public final Function<Observable<OrderEvent>, Observable<OrderEvent>> mergeCompose() {
        return mergeCompose;
    }

    public final MergeExecutionMode executionMode() {
        return executionMode;
    }

    public static MergeOption newBuilder(final String mergeOrderLabel) {
        return new Builder(checkNotNull(mergeOrderLabel));
    }

    public static class Builder implements
                                MergeOption,
                                CancelSLOption,
                                CancelTPOption,
                                SingleCancelSLOption,
                                SingleCancelTPOption,
                                ExecutionOption,
                                MergeComposeOption,
                                BuildOption {

        private final String mergeOrderLabel;
        private Function<Observable<OrderEvent>, Observable<OrderEvent>> cancelSLTPCompose;
        private Function<Observable<OrderEvent>, Observable<OrderEvent>> mergeCompose;
        private MergeExecutionMode executionMode;
        private Function<Observable<OrderEvent>, Observable<OrderEvent>> cancelSLCompose;
        private Function<Observable<OrderEvent>, Observable<OrderEvent>> cancelTPCompose;
        private BiFunction<Observable<OrderEvent>, IOrder, Observable<OrderEvent>> singleCancelSLCompose;
        private BiFunction<Observable<OrderEvent>, IOrder, Observable<OrderEvent>> singleCancelTPCompose;

        public Builder(final String mergeOrderLabel) {
            this.mergeOrderLabel = mergeOrderLabel;
        }

        @Override
        public MergeCommand build() {
            return new MergeCommand(this);
        }

        @Override
        public CancelSLOption withCancelSLAndTP(final Function<Observable<OrderEvent>,
                                                               Observable<OrderEvent>> cancelSLTPCompose) {
            this.cancelSLTPCompose = checkNotNull(cancelSLTPCompose);
            return this;
        }

        @Override
        public BuildOption withMerge(final Function<Observable<OrderEvent>, Observable<OrderEvent>> mergeCompose) {
            this.mergeCompose = checkNotNull(mergeCompose);
            return this;
        }

        @Override
        public MergeComposeOption withExecutionMode(final MergeExecutionMode executionMode) {
            this.executionMode = executionMode;
            return this;
        }

        @Override
        public ExecutionOption withSingleCancelTP(final BiFunction<Observable<OrderEvent>,
                                                                   IOrder,
                                                                   Observable<OrderEvent>> singleCancelTPCompose) {
            this.singleCancelTPCompose = checkNotNull(singleCancelTPCompose);
            return this;
        }

        @Override
        public CancelTPOption withSingleCancelSL(final BiFunction<Observable<OrderEvent>,
                                                                  IOrder,
                                                                  Observable<OrderEvent>> singleCancelSLCompose) {
            this.singleCancelSLCompose = checkNotNull(singleCancelSLCompose);
            return this;
        }

        @Override
        public SingleCancelTPOption withCancelTP(final Function<Observable<OrderEvent>,
                                                                Observable<OrderEvent>> cancelTPCompose) {
            this.cancelTPCompose = checkNotNull(cancelTPCompose);
            return this;
        }

        @Override
        public SingleCancelSLOption withCancelSL(final Function<Observable<OrderEvent>,
                                                                Observable<OrderEvent>> cancelSLCompose) {
            this.cancelSLCompose = checkNotNull(cancelSLCompose);
            return this;
        }
    }
}
