package com.jforex.programming.position.test;

import com.jforex.programming.order.OrderParams;
import com.jforex.programming.position.Position;
import com.jforex.programming.position.PositionEvent;

public final class PositionSubmitAndMerge {

    private final Position position;
    private String mergeLabel;

    public PositionSubmitAndMerge(final Position position) {
        this.position = position;

        subscribeToPositionEvents();
    }

    private final void subscribeToPositionEvents() {
        position.positionEventObs()
                .filter(positonEvent -> positonEvent == PositionEvent.SUBMITTASK_DONE)
                .subscribe(positonEvent -> position.merge(mergeLabel));
    }

    public final void submitAndMerge(final OrderParams orderParams,
                                     final String mergeLabel) {
        this.mergeLabel = mergeLabel;
        position.submit(orderParams);
    }
}
