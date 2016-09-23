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
    private final BiFunction<Observable<OrderEvent>, IOrder, Observable<OrderEvent>> orderCancelSLComposer;
    private final BiFunction<Observable<OrderEvent>, IOrder, Observable<OrderEvent>> orderCancelTPComposer;

    public enum MergeExecutionMode {
        ConcatCancelSLAndTP,
        ConcatCancelTPAndSL,
        MergeCancelSLAndTP
    }

    public interface MergeOption {

        public CancelSLAndTPOption composeCancelSLAndTP(Function<Observable<OrderEvent>,
                                                                 Observable<OrderEvent>> cancelSLTPCompose);

        public MergeOption composeMerge(Function<Observable<OrderEvent>,
                                                 Observable<OrderEvent>> mergeCompose);

        public MergeCommand build();
    }

    public interface CancelSLAndTPOption {

        public CancelSLAndTPOption composeCancelSL(Function<Observable<OrderEvent>,
                                                            Observable<OrderEvent>> cancelSLCompose);

        public CancelSLAndTPOption composeCancelTP(Function<Observable<OrderEvent>,
                                                            Observable<OrderEvent>> cancelTPCompose);

        public CancelSLAndTPOption composeOrderCancelSL(BiFunction<Observable<OrderEvent>,
                                                                   IOrder,
                                                                   Observable<OrderEvent>> singleCancelSLCompose);

        public CancelSLAndTPOption composeOrderCancelTP(BiFunction<Observable<OrderEvent>,
                                                                   IOrder,
                                                                   Observable<OrderEvent>> orderCancelTPComposer);

        public CancelSLAndTPOption withExecutionMode(MergeExecutionMode executionMode);

        public MergeOption done();
    }

    private MergeCommand(final Builder builder) {
        mergeOrderLabel = builder.mergeOrderLabel;
        cancelSLTPCompose = builder.cancelSLTPCompose;
        cancelSLCompose = builder.cancelSLCompose;
        cancelTPCompose = builder.cancelTPCompose;
        orderCancelSLComposer = builder.singleCancelSLCompose;
        orderCancelTPComposer = builder.orderCancelTPComposer;
        executionMode = builder.executionMode;
        mergeCompose = builder.mergeCompose;
    }

    public String mergeOrderLabel() {
        return mergeOrderLabel;
    }

    public Function<Observable<OrderEvent>, Observable<OrderEvent>> cancelSLTPCompose() {
        return cancelSLTPCompose;
    }

    public Function<Observable<OrderEvent>, Observable<OrderEvent>> cancelSLCompose() {
        return cancelSLCompose;
    }

    public Function<Observable<OrderEvent>, Observable<OrderEvent>> cancelTPCompose() {
        return cancelTPCompose;
    }

    public Function<Observable<OrderEvent>, Observable<OrderEvent>> orderCancelSLComposer(final IOrder order) {
        return obs -> orderCancelSLComposer.apply(obs, order);
    }

    public Function<Observable<OrderEvent>, Observable<OrderEvent>> orderCancelTPComposer(final IOrder order) {
        return obs -> orderCancelTPComposer.apply(obs, order);
    }

    public Function<Observable<OrderEvent>, Observable<OrderEvent>> mergeCompose() {
        return mergeCompose;
    }

    public MergeExecutionMode executionMode() {
        return executionMode;
    }

    public static MergeOption newBuilder(final String mergeOrderLabel) {
        return new Builder(checkNotNull(mergeOrderLabel));
    }

    public static class Builder implements
                                MergeOption,
                                CancelSLAndTPOption {

        private final String mergeOrderLabel;
        private MergeExecutionMode executionMode;
        private Function<Observable<OrderEvent>, Observable<OrderEvent>> cancelSLTPCompose =
                observable -> observable;
        private Function<Observable<OrderEvent>, Observable<OrderEvent>> mergeCompose =
                observable -> observable;
        private Function<Observable<OrderEvent>, Observable<OrderEvent>> cancelSLCompose =
                observable -> observable;
        private Function<Observable<OrderEvent>, Observable<OrderEvent>> cancelTPCompose =
                observable -> observable;
        private BiFunction<Observable<OrderEvent>, IOrder, Observable<OrderEvent>> singleCancelSLCompose =
                (observable, order) -> observable;
        private BiFunction<Observable<OrderEvent>, IOrder, Observable<OrderEvent>> orderCancelTPComposer =
                (observable, order) -> observable;

        public Builder(final String mergeOrderLabel) {
            this.mergeOrderLabel = mergeOrderLabel;
        }

        @Override
        public MergeCommand build() {
            return new MergeCommand(this);
        }

        @Override
        public CancelSLAndTPOption composeCancelSLAndTP(final Function<Observable<OrderEvent>,
                                                                       Observable<OrderEvent>> cancelSLTPCompose) {
            this.cancelSLTPCompose = checkNotNull(cancelSLTPCompose);
            return this;
        }

        @Override
        public MergeOption composeMerge(final Function<Observable<OrderEvent>, Observable<OrderEvent>> mergeCompose) {
            this.mergeCompose = checkNotNull(mergeCompose);
            return this;
        }

        @Override
        public CancelSLAndTPOption withExecutionMode(final MergeExecutionMode executionMode) {
            this.executionMode = executionMode;
            return this;
        }

        @Override
        public CancelSLAndTPOption composeOrderCancelTP(final BiFunction<Observable<OrderEvent>,
                                                                         IOrder,
                                                                         Observable<OrderEvent>> orderCancelTPComposer) {
            this.orderCancelTPComposer = checkNotNull(orderCancelTPComposer);
            return this;
        }

        @Override
        public CancelSLAndTPOption composeOrderCancelSL(final BiFunction<Observable<OrderEvent>,
                                                                         IOrder,
                                                                         Observable<OrderEvent>> singleCancelSLCompose) {
            this.singleCancelSLCompose = checkNotNull(singleCancelSLCompose);
            return this;
        }

        @Override
        public CancelSLAndTPOption composeCancelTP(final Function<Observable<OrderEvent>,
                                                                  Observable<OrderEvent>> cancelTPCompose) {
            this.cancelTPCompose = checkNotNull(cancelTPCompose);
            return this;
        }

        @Override
        public CancelSLAndTPOption composeCancelSL(final Function<Observable<OrderEvent>,
                                                                  Observable<OrderEvent>> cancelSLCompose) {
            this.cancelSLCompose = checkNotNull(cancelSLCompose);
            return this;
        }

        @Override
        public MergeOption done() {
            return this;
        }
    }
}
