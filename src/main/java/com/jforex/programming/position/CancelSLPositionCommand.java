package com.jforex.programming.position;

import static com.google.common.base.Preconditions.checkNotNull;

import com.dukascopy.api.IOrder;
import com.dukascopy.api.Instrument;
import com.jforex.programming.order.event.OrderEvent;

import io.reactivex.Observable;
import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Function;

public class CancelSLPositionCommand {

    private final Instrument instrument;
    private final BiFunction<Observable<OrderEvent>, IOrder, Observable<OrderEvent>> cancelSLCompose;

    public interface CancelSLPositionCompose {

        public CancelSLPositionCompose
               withcancelSLCompose(BiFunction<Observable<OrderEvent>, IOrder, Observable<OrderEvent>> cancelSLCompose);

        public CancelSLPositionCommand build();
    }

    private CancelSLPositionCommand(final Builder builder) {
        instrument = builder.instrument;
        cancelSLCompose = builder.cancelSLCompose;
    }

    public static final CancelSLPositionCompose with(final Instrument instrument) {
        return new Builder(checkNotNull(instrument));
    }

    public final Instrument instrument() {
        return instrument;
    }

    public final Function<Observable<OrderEvent>, Observable<OrderEvent>> cancelSLComposer(final IOrder order) {
        return obs -> cancelSLCompose.apply(obs, order);
    }

    private static class Builder implements CancelSLPositionCompose {

        private final Instrument instrument;
        private BiFunction<Observable<OrderEvent>, IOrder, Observable<OrderEvent>> cancelSLCompose =
                (observable, o) -> observable;

        private Builder(final Instrument instrument) {
            this.instrument = instrument;
        }

        @Override
        public CancelSLPositionCompose
               withcancelSLCompose(final BiFunction<Observable<OrderEvent>, IOrder, Observable<OrderEvent>> cancelSLCompose) {
            this.cancelSLCompose = checkNotNull(cancelSLCompose);
            return this;
        }

        @Override
        public CancelSLPositionCommand build() {
            return new CancelSLPositionCommand(this);
        }
    }
}
