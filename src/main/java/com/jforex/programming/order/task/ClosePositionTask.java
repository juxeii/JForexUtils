package com.jforex.programming.order.task;

import com.dukascopy.api.Instrument;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.task.params.position.CloseAllPositionsParams;
import com.jforex.programming.order.task.params.position.ClosePositionParamsHandler;
import com.jforex.programming.order.task.params.position.ClosePositionParams;
import com.jforex.programming.position.PositionUtil;

import io.reactivex.Observable;
import io.reactivex.functions.Function;

public class ClosePositionTask {

    private final ClosePositionParamsHandler positionParamsHandler;
    private final PositionUtil positionUtil;

    public ClosePositionTask(final ClosePositionParamsHandler positionParamsHandler,
                             final PositionUtil positionUtil) {
        this.positionParamsHandler = positionParamsHandler;
        this.positionUtil = positionUtil;
    }

    public Observable<OrderEvent> close(final Instrument instrument,
                                        final ClosePositionParams complexClosePositionParams) {
        return Observable.defer(() -> {
            final Observable<OrderEvent> merge = positionParamsHandler.observeMerge(instrument,
                                                                                    complexClosePositionParams);
            final Observable<OrderEvent> close = positionParamsHandler.observeClose(instrument,
                                                                                    complexClosePositionParams);

            return merge.concatWith(close);
        });
    }

    public Observable<OrderEvent> closeAll(final CloseAllPositionsParams closeAllPositionParams) {
        return Observable.defer(() -> {
            final Function<Instrument, Observable<OrderEvent>> observablesForParams =
                    instrument -> close(instrument, closeAllPositionParams.complexClosePositionParams());
            return Observable.merge(positionUtil.observablesFromFactory(observablesForParams));
        });
    }
}
