package com.jforex.programming.position;

import static com.google.common.base.Preconditions.checkNotNull;

import com.dukascopy.api.IOrder;
import com.dukascopy.api.Instrument;
import com.jforex.programming.order.event.OrderEvent;

import io.reactivex.Observable;
import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Function;

public class ClosePositionCommand {

    private final Instrument instrument;
    private final String mergeOrderLabel;
    private final Function<Observable<OrderEvent>, Observable<OrderEvent>> mergeCompose;
    private final BiFunction<Observable<OrderEvent>, IOrder, Observable<OrderEvent>> closeCompose;

    public interface ClosePositionCompose {

        public ClosePositionCompose withMergeCompose(Function<Observable<OrderEvent>,
                                                              Observable<OrderEvent>> mergeCompose);

        public ClosePositionCompose withCloseCompose(BiFunction<Observable<OrderEvent>,
                                                                IOrder,
                                                                Observable<OrderEvent>> closeCompose);

        public ClosePositionCommand build();
    }

    private ClosePositionCommand(final Builder builder) {
        instrument = builder.instrument;
        mergeOrderLabel = builder.mergeOrderLabel;
        mergeCompose = builder.mergeCompose;
        closeCompose = builder.closeCompose;
    }

    public static final ClosePositionCompose with(final Instrument instrument,
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

    public final Function<Observable<OrderEvent>, Observable<OrderEvent>> mergeComposer() {
        return mergeCompose;
    }

    public final Function<Observable<OrderEvent>, Observable<OrderEvent>> closeComposer(final IOrder order) {
        return obs -> closeCompose.apply(obs, order);
    }

    private static class Builder implements ClosePositionCompose {

        private final Instrument instrument;
        private final String mergeOrderLabel;
        private Function<Observable<OrderEvent>, Observable<OrderEvent>> mergeCompose =
                observable -> observable;
        private BiFunction<Observable<OrderEvent>, IOrder, Observable<OrderEvent>> closeCompose =
                (observable, o) -> observable;

        private Builder(final Instrument instrument,
                        final String mergeOrderLabel) {
            this.instrument = instrument;
            this.mergeOrderLabel = mergeOrderLabel;
        }

        @Override
        public ClosePositionCompose
               withMergeCompose(final Function<Observable<OrderEvent>, Observable<OrderEvent>> mergeCompose) {
            this.mergeCompose = checkNotNull(mergeCompose);
            return this;
        }

        @Override
        public ClosePositionCompose withCloseCompose(final BiFunction<Observable<OrderEvent>,
                                                                      IOrder,
                                                                      Observable<OrderEvent>> closeCompose) {
            this.closeCompose = checkNotNull(closeCompose);
            return this;
        }

        @Override
        public ClosePositionCommand build() {
            return new ClosePositionCommand(this);
        }
    }
}
