package com.jforex.programming.order.task;

import com.dukascopy.api.Instrument;
import com.jforex.programming.order.command.ClosePositionCommand;
import com.jforex.programming.order.command.ClosePositionCommandHandler;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.position.PositionUtil;

import io.reactivex.Observable;
import io.reactivex.functions.Function;

public class CloseTask {

    private final ClosePositionCommandHandler commandHandler;
    private final PositionUtil positionUtil;

    public CloseTask(final ClosePositionCommandHandler commandHandler,
                          final PositionUtil positionUtil) {
        this.commandHandler = commandHandler;
        this.positionUtil = positionUtil;
    }

    public Observable<OrderEvent> close(final ClosePositionCommand command) {
        return Observable.defer(() -> {
            final Observable<OrderEvent> merge = commandHandler.observeMerge(command);
            final Observable<OrderEvent> close = commandHandler.observeClose(command);

            return merge.concatWith(close);
        });
    }

    public Observable<OrderEvent> closeAllPositions(final Function<Instrument, ClosePositionCommand> commandFactory) {
        return Observable.defer(() -> {
            final Function<Instrument, Observable<OrderEvent>> observablesFromFactory =
                    instrument -> close(commandFactory.apply(instrument));
            return Observable.merge(positionUtil.observablesFromFactory(observablesFromFactory));
        });
    }
}
