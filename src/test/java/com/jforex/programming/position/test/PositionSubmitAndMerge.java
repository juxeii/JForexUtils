package com.jforex.programming.position.test;

import com.jforex.programming.order.OrderParams;
import com.jforex.programming.position.Position;
import com.jforex.programming.position.PositionEvent;

import rx.Observable;

public final class PositionSubmitAndMerge {

    private final Position position;
    private final Observable<PositionEvent> submitDoneObs;

    public PositionSubmitAndMerge(final Position position) {
        this.position = position;

        submitDoneObs = Observable.create(subscriber -> {
            position.positionEventObs()
                    .filter(positonEvent -> positonEvent == PositionEvent.SUBMITTASK_DONE)
                    .subscribe(positonEvent -> {
                        subscriber.onNext(positonEvent);
                        subscriber.onCompleted();
                    });
        });
    }

    public final void submitAndMerge(final OrderParams orderParams,
                                     final String mergeLabel) {
        submitDoneObs.subscribe(positonEvent -> position.merge(mergeLabel));
        position.submit(orderParams);
    }
}
