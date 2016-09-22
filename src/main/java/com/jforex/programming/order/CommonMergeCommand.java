package com.jforex.programming.order;

import static com.google.common.base.Preconditions.checkNotNull;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.event.OrderEvent;

import io.reactivex.Observable;
import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Function;

public class CommonMergeCommand {

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

    public interface MergeOption<V> {

        public CancelSLOption<V> withCancelSLAndTP(Function<Observable<OrderEvent>,
                                                            Observable<OrderEvent>> cancelSLTPCompose);

        public V done();
    }

    public interface CancelSLOption<V> {

        public SingleCancelSLOption<V> withCancelSL(Function<Observable<OrderEvent>,
                                                             Observable<OrderEvent>> cancelSLCompose);
    }

    public interface SingleCancelSLOption<V> {

        public CancelTPOption<V> withSingleCancelSL(BiFunction<Observable<OrderEvent>,
                                                               IOrder,
                                                               Observable<OrderEvent>> singleCancelSLCompose);
    }

    public interface CancelTPOption<V> {

        public SingleCancelTPOption<V> withCancelTP(Function<Observable<OrderEvent>,
                                                             Observable<OrderEvent>> cancelTPCompose);
    }

    public interface SingleCancelTPOption<V> {

        public ExecutionOption<V> withSingleCancelTP(BiFunction<Observable<OrderEvent>,
                                                                IOrder,
                                                                Observable<OrderEvent>> singleCancelTPCompose);
    }

    public interface ExecutionOption<V> {

        public MergeComposeOption<V> withExecutionMode(MergeExecutionMode executionMode);
    }

    public interface MergeComposeOption<V> {

        public BuildOption<V> withMerge(Function<Observable<OrderEvent>,
                                                 Observable<OrderEvent>> mergeCompose);
    }

    public interface BuildOption<V> {

        public V done();
    }

    private CommonMergeCommand(final Builder<?> builder) {
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

    public static <T> MergeOption<T> newBuilder(final CommandParent<T> commandParent,
                                                final String mergeOrderLabel) {
        return new Builder<>(mergeOrderLabel, commandParent);
    }

    public static class Builder<T> implements
                               MergeOption<T>,
                               CancelSLOption<T>,
                               CancelTPOption<T>,
                               SingleCancelSLOption<T>,
                               SingleCancelTPOption<T>,
                               ExecutionOption<T>,
                               MergeComposeOption<T>,
                               BuildOption<T> {

        private final CommandParent<T> commandParent;
        private final String mergeOrderLabel;
        private Function<Observable<OrderEvent>, Observable<OrderEvent>> cancelSLTPCompose;
        private Function<Observable<OrderEvent>, Observable<OrderEvent>> mergeCompose;
        private MergeExecutionMode executionMode;
        private Function<Observable<OrderEvent>, Observable<OrderEvent>> cancelSLCompose;
        private Function<Observable<OrderEvent>, Observable<OrderEvent>> cancelTPCompose;
        private BiFunction<Observable<OrderEvent>, IOrder, Observable<OrderEvent>> singleCancelSLCompose;
        private BiFunction<Observable<OrderEvent>, IOrder, Observable<OrderEvent>> singleCancelTPCompose;

        public Builder(final String mergeOrderLabel,
                       final CommandParent<T> commandParent) {
            this.commandParent = commandParent;
            this.mergeOrderLabel = mergeOrderLabel;
        }

        private CommonMergeCommand build() {
            return new CommonMergeCommand(this);
        }

        @SuppressWarnings("unchecked")
        public T done() {
            commandParent.addChild(this.build());
            return (T) commandParent;
        }

        @Override
        public CancelSLOption<T> withCancelSLAndTP(final Function<Observable<OrderEvent>,
                                                                  Observable<OrderEvent>> cancelSLTPCompose) {
            this.cancelSLTPCompose = checkNotNull(cancelSLTPCompose);
            return this;
        }

        @Override
        public BuildOption<T> withMerge(final Function<Observable<OrderEvent>, Observable<OrderEvent>> mergeCompose) {
            this.mergeCompose = checkNotNull(mergeCompose);
            return this;
        }

        @Override
        public MergeComposeOption<T> withExecutionMode(final MergeExecutionMode executionMode) {
            this.executionMode = executionMode;
            return this;
        }

        @Override
        public ExecutionOption<T> withSingleCancelTP(final BiFunction<Observable<OrderEvent>,
                                                                      IOrder,
                                                                      Observable<OrderEvent>> singleCancelTPCompose) {
            this.singleCancelTPCompose = checkNotNull(singleCancelTPCompose);
            return this;
        }

        @Override
        public CancelTPOption<T> withSingleCancelSL(final BiFunction<Observable<OrderEvent>,
                                                                     IOrder,
                                                                     Observable<OrderEvent>> singleCancelSLCompose) {
            this.singleCancelSLCompose = checkNotNull(singleCancelSLCompose);
            return this;
        }

        @Override
        public SingleCancelTPOption<T> withCancelTP(final Function<Observable<OrderEvent>,
                                                                   Observable<OrderEvent>> cancelTPCompose) {
            this.cancelTPCompose = checkNotNull(cancelTPCompose);
            return this;
        }

        @Override
        public SingleCancelSLOption<T> withCancelSL(final Function<Observable<OrderEvent>,
                                                                   Observable<OrderEvent>> cancelSLCompose) {
            this.cancelSLCompose = checkNotNull(cancelSLCompose);
            return this;
        }
    }
}
