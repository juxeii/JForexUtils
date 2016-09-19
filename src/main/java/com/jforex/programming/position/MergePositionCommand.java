package com.jforex.programming.position;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Optional;

import com.dukascopy.api.IOrder;
import com.dukascopy.api.Instrument;
import com.jforex.programming.order.event.OrderEvent;

import io.reactivex.Observable;
import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Function;

public class MergePositionCommand {

    private final Instrument instrument;
    private final String mergeOrderLabel;
    private final ExecutionMode executionMode;
    private final Function<Observable<OrderEvent>, Observable<OrderEvent>> cancelSLTPCompose;
    private final Function<Observable<OrderEvent>, Observable<OrderEvent>> mergeCompose;
    private final BiFunction<Observable<OrderEvent>, IOrder, Observable<OrderEvent>> cancelSLCompose;
    private final BiFunction<Observable<OrderEvent>, IOrder, Observable<OrderEvent>> cancelTPCompose;

    public enum ExecutionMode {
        ConcatSLAndTP,
        ConcatTPAndSL,
        MergeSLAndTP
    }

    public interface MergePositionOption {

        public CancelSLOption withCancelSLAndTP(Function<Observable<OrderEvent>,
                                                         Observable<OrderEvent>> cancelSLTPCompose);

        public MergePositionCommand build();
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

        public MergeOption withExecutionMode(ExecutionMode executionMode);
    }

    public interface MergeOption {

        public BuildOption withMerge(Function<Observable<OrderEvent>,
                                              Observable<OrderEvent>> mergeCompose);
    }

    public interface BuildOption {

        public MergePositionCommand build();
    }

    private MergePositionCommand(final Builder builder) {
        instrument = builder.instrument;
        mergeOrderLabel = builder.mergeOrderLabel;
        cancelSLTPCompose = builder.cancelSLTPCompose;
        cancelSLCompose = builder.cancelSLCompose;
        cancelTPCompose = builder.cancelTPCompose;
        executionMode = builder.executionMode;
        mergeCompose = builder.mergeCompose;
    }

    public static final MergePositionOption with(final Instrument instrument,
                                                 final String mergeOrderLabel) {
        return new Builder(checkNotNull(instrument),
                           checkNotNull(mergeOrderLabel));
    }

    public final Instrument instrument() {
        return instrument;
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

    public final ExecutionMode executionMode() {
        return executionMode;
    }

    private static class Builder implements
                                 MergePositionOption,
                                 CancelSLOption,
                                 CancelTPOption,
                                 ExecutionOption,
                                 MergeOption,
                                 BuildOption {

        private final Instrument instrument;
        private final String mergeOrderLabel;
        private Function<Observable<OrderEvent>, Observable<OrderEvent>> cancelSLTPCompose;
        private BiFunction<Observable<OrderEvent>,
                           IOrder,
                           Observable<OrderEvent>> cancelSLCompose = (observable, o) -> observable;
        private BiFunction<Observable<OrderEvent>,
                           IOrder,
                           Observable<OrderEvent>> cancelTPCompose = (observable, o) -> observable;
        private ExecutionMode executionMode;
        private Function<Observable<OrderEvent>,
                         Observable<OrderEvent>> mergeCompose = observable -> observable;

        private Builder(final Instrument instrument,
                        final String mergeOrderLabel) {
            this.instrument = instrument;
            this.mergeOrderLabel = mergeOrderLabel;
        }

        @Override
        public MergeOption withExecutionMode(final ExecutionMode executionMode) {
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
        public MergePositionCommand build() {
            return new MergePositionCommand(this);
        }
    }
}
