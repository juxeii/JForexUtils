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
    private final BiFunction<Observable<OrderEvent>, IOrder, Observable<OrderEvent>> cancelSLCompose;
    private final BiFunction<Observable<OrderEvent>, IOrder, Observable<OrderEvent>> cancelTPCompose;

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

        public CancelTPOption<V> withCancelSL(BiFunction<Observable<OrderEvent>,
                                                         IOrder,
                                                         Observable<OrderEvent>> cancelSLCompose);
    }

    public interface CancelTPOption<V> {

        public ExecutionOption<V> withCancelTP(BiFunction<Observable<OrderEvent>,
                                                          IOrder,
                                                          Observable<OrderEvent>> cancelTPCompose);
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

    public static <T> MergeOption<T> newBuilder(final CommandParent<T> commandParent,
                                                final String mergeOrderLabel) {
        return new Builder<>(mergeOrderLabel, commandParent);
    }

    public static class Builder<T> implements
                               MergeOption<T>,
                               CancelSLOption<T>,
                               CancelTPOption<T>,
                               ExecutionOption<T>,
                               MergeComposeOption<T>,
                               BuildOption<T> {

        private final CommandParent<T> commandParent;
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
        public ExecutionOption<T> withCancelTP(final BiFunction<Observable<OrderEvent>, IOrder,
                                                                Observable<OrderEvent>> cancelTPCompose) {
            this.cancelTPCompose = checkNotNull(cancelTPCompose);
            return this;
        }

        @Override
        public CancelTPOption<T> withCancelSL(final BiFunction<Observable<OrderEvent>, IOrder,
                                                               Observable<OrderEvent>> cancelSLCompose) {
            this.cancelSLCompose = checkNotNull(cancelSLCompose);
            return this;
        }
    }
}
