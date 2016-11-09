package com.jforex.programming.order.task;

import com.dukascopy.api.Instrument;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.task.params.position.CloseAllPositionsParams;
import com.jforex.programming.order.task.params.position.ClosePositionParams;
import com.jforex.programming.order.task.params.position.ClosePositionParamsHandler;
import com.jforex.programming.position.PositionUtil;

import io.reactivex.Observable;
import io.reactivex.functions.Function;

public class ClosePositionTaskObservable {

    private final ClosePositionParamsHandler closePositionParamsHandler;
    private final PositionUtil positionUtil;

    public ClosePositionTaskObservable(final ClosePositionParamsHandler closePositionParamsHandler,
                                       final PositionUtil positionUtil) {
        this.closePositionParamsHandler = closePositionParamsHandler;
        this.positionUtil = positionUtil;
    }

    public Observable<OrderEvent> close(final Instrument instrument,
                                        final ClosePositionParams closePositionParams) {
        return Observable.defer(() -> {
            final Observable<OrderEvent> merge =
                    closePositionParamsHandler.observeMerge(instrument, closePositionParams);
            final Observable<OrderEvent> close =
                    closePositionParamsHandler.observeClose(instrument, closePositionParams);

            return merge.concatWith(close);
        });
    }

    public Observable<OrderEvent> closeAll(final CloseAllPositionsParams closeAllPositionParams) {
        return Observable.defer(() -> {
            final Function<Instrument, Observable<OrderEvent>> observablesForParams =
                    instrument -> close(instrument, closeAllPositionParams.closePositionParams());
            return Observable.merge(positionUtil.observablesFromFactory(observablesForParams));
        });
    }
}
