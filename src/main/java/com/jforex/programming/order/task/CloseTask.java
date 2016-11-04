package com.jforex.programming.order.task;

import com.dukascopy.api.Instrument;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.task.params.ClosePositionParams;
import com.jforex.programming.order.task.params.ClosePositionParamsHandler;
import com.jforex.programming.position.PositionUtil;

import io.reactivex.Observable;
import io.reactivex.functions.Function;

public class CloseTask {

    private final ClosePositionParamsHandler positionParamsHandler;
    private final PositionUtil positionUtil;

    public CloseTask(final ClosePositionParamsHandler positionParamsHandler,
                     final PositionUtil positionUtil) {
        this.positionParamsHandler = positionParamsHandler;
        this.positionUtil = positionUtil;
    }

    public Observable<OrderEvent> close(final ClosePositionParams positionParams) {
        return Observable.defer(() -> {
            final Observable<OrderEvent> merge = positionParamsHandler.observeMerge(positionParams);
            final Observable<OrderEvent> close = positionParamsHandler.observeClose(positionParams);

            return merge.concatWith(close);
        });
    }

    public Observable<OrderEvent>
           closeAllPositions(final Function<Instrument, ClosePositionParams> positionParamsFactory) {
        return Observable.defer(() -> {
            final Function<Instrument, Observable<OrderEvent>> observablesFromFactory =
                    instrument -> close(positionParamsFactory.apply(instrument));
            return Observable.merge(positionUtil.observablesFromFactory(observablesFromFactory));
        });
    }
}
