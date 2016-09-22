package com.jforex.programming.order;

import com.dukascopy.api.Instrument;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.position.ClosePositionCommand;

import io.reactivex.Observable;

public class OrderCloseTask {

    private final ClosePositionCommandHandler commandHandler;

    public OrderCloseTask(final ClosePositionCommandHandler commandHandler) {
        this.commandHandler = commandHandler;
    }

    public Observable<OrderEvent> close(final ClosePositionCommand command) {
        return Observable.defer(() -> {
            final Observable<OrderEvent> merge = commandHandler.observeMerge(command);
            final Observable<OrderEvent> close = commandHandler.observeClose(command);

            return merge.concatWith(close);
        });
    }

    public Observable<OrderEvent> closeAll(final java.util.function.Function<Instrument,
                                                                             ClosePositionCommand> commandFactory) {
//        return Observable.defer(() -> {
//            final List<Observable<OrderEvent>> observables = positionFactory
//                .all()
//                .stream()
//                .map(Position::instrument)
//                .map(instrument -> close(commandFactory.apply(instrument)))
//                .collect(Collectors.toList());
//            return Observable.merge(observables);
//        });
        return Observable.empty();
    }
}
