package com.jforex.programming.position;

import static com.google.common.base.Preconditions.checkNotNull;

import com.dukascopy.api.IOrder;
import com.dukascopy.api.Instrument;
import com.jforex.programming.order.event.OrderEvent;

import io.reactivex.Observable;
import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Function;

public class CancelTPPositionCommand {

    private final Instrument instrument;
    private final BiFunction<Observable<OrderEvent>, IOrder, Observable<OrderEvent>> cancelTPCompose;

    public interface CancelTPPositionCompose {

        public CancelTPPositionCompose
               withcancelTPCompose(BiFunction<Observable<OrderEvent>, IOrder, Observable<OrderEvent>> cancelTPCompose);

        public CancelTPPositionCommand build();
    }

    private CancelTPPositionCommand(final Builder builder) {
        instrument = builder.instrument;
        cancelTPCompose = builder.cancelTPCompose;
    }

    public static final CancelTPPositionCompose with(final Instrument instrument) {
        return new Builder(checkNotNull(instrument));
    }

    public final Instrument instrument() {
        return instrument;
    }

    public final Function<Observable<OrderEvent>, Observable<OrderEvent>> cancelTPComposer(final IOrder order) {
        return obs -> cancelTPCompose.apply(obs, order);
    }

    private static class Builder implements CancelTPPositionCompose {

        private final Instrument instrument;
        private BiFunction<Observable<OrderEvent>, IOrder, Observable<OrderEvent>> cancelTPCompose =
                (observable, o) -> observable;

        private Builder(final Instrument instrument) {
            this.instrument = instrument;
        }

        @Override
        public CancelTPPositionCompose
               withcancelTPCompose(final BiFunction<Observable<OrderEvent>, IOrder, Observable<OrderEvent>> cancelTPCompose) {
            this.cancelTPCompose = checkNotNull(cancelTPCompose);
            return this;
        }

        @Override
        public CancelTPPositionCommand build() {
            return new CancelTPPositionCommand(this);
        }
    }
}
