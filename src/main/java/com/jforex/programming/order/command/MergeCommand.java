package com.jforex.programming.order.command;

import static com.google.common.base.Preconditions.checkNotNull;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.event.OrderEvent;

import io.reactivex.Observable;
import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Function;

public class MergeCommand {

    private final String mergeOrderLabel;
    private final MergeExecutionMode executionMode;
    private final Function<Observable<OrderEvent>, Observable<OrderEvent>> cancelSLTPComposer;
    private final Function<Observable<OrderEvent>, Observable<OrderEvent>> mergeComposer;
    private final Function<Observable<OrderEvent>, Observable<OrderEvent>> cancelSLComposer;
    private final Function<Observable<OrderEvent>, Observable<OrderEvent>> cancelTPComposer;
    private final BiFunction<Observable<OrderEvent>, IOrder, Observable<OrderEvent>> orderCancelSLComposer;
    private final BiFunction<Observable<OrderEvent>, IOrder, Observable<OrderEvent>> orderCancelTPComposer;

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
        cancelSLTPComposer = builder.cancelSLTPComposer;
        cancelSLComposer = builder.cancelSLComposer;
        cancelTPComposer = builder.cancelTPComposer;
        orderCancelSLComposer = builder.singleCancelSLComposer;
        orderCancelTPComposer = builder.orderCancelTPComposer;
        executionMode = builder.executionMode;
        mergeComposer = builder.mergeComposer;
    }

    public String mergeOrderLabel() {
        return mergeOrderLabel;
    }

    public Function<Observable<OrderEvent>, Observable<OrderEvent>> cancelSLTPCompose() {
        return cancelSLTPComposer;
    }

    public Function<Observable<OrderEvent>, Observable<OrderEvent>> cancelSLCompose() {
        return cancelSLComposer;
    }

    public Function<Observable<OrderEvent>, Observable<OrderEvent>> cancelTPCompose() {
        return cancelTPComposer;
    }

    public Function<Observable<OrderEvent>, Observable<OrderEvent>> orderCancelSLComposer(final IOrder order) {
        return obs -> orderCancelSLComposer.apply(obs, order);
    }

    public Function<Observable<OrderEvent>, Observable<OrderEvent>> orderCancelTPComposer(final IOrder order) {
        return obs -> orderCancelTPComposer.apply(obs, order);
    }

    public Function<Observable<OrderEvent>, Observable<OrderEvent>> mergeCompose() {
        return mergeComposer;
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
        private MergeExecutionMode executionMode = MergeExecutionMode.MergeCancelSLAndTP;
        private Function<Observable<OrderEvent>, Observable<OrderEvent>> cancelSLTPComposer =
                observable -> observable;
        private Function<Observable<OrderEvent>, Observable<OrderEvent>> cancelSLComposer =
                observable -> observable;
        private Function<Observable<OrderEvent>, Observable<OrderEvent>> cancelTPComposer =
                observable -> observable;
        private BiFunction<Observable<OrderEvent>, IOrder, Observable<OrderEvent>> singleCancelSLComposer =
                (observable, order) -> observable;
        private BiFunction<Observable<OrderEvent>, IOrder, Observable<OrderEvent>> orderCancelTPComposer =
                (observable, order) -> observable;
        private Function<Observable<OrderEvent>, Observable<OrderEvent>> mergeComposer =
                observable -> observable;

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
            this.cancelSLTPComposer = checkNotNull(cancelSLTPCompose);
            return this;
        }

        @Override
        public MergeOption composeMerge(final Function<Observable<OrderEvent>, Observable<OrderEvent>> mergeCompose) {
            this.mergeComposer = checkNotNull(mergeCompose);
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
            this.singleCancelSLComposer = checkNotNull(singleCancelSLCompose);
            return this;
        }

        @Override
        public CancelSLAndTPOption composeCancelTP(final Function<Observable<OrderEvent>,
                                                                  Observable<OrderEvent>> cancelTPCompose) {
            this.cancelTPComposer = checkNotNull(cancelTPCompose);
            return this;
        }

        @Override
        public CancelSLAndTPOption composeCancelSL(final Function<Observable<OrderEvent>,
                                                                  Observable<OrderEvent>> cancelSLCompose) {
            this.cancelSLComposer = checkNotNull(cancelSLCompose);
            return this;
        }

        @Override
        public MergeOption done() {
            return this;
        }
    }
}
