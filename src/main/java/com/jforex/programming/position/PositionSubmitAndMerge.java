package com.jforex.programming.position;

import com.jforex.programming.order.OrderParams;

public final class PositionSubmitAndMerge {

    private final Position position;

    public PositionSubmitAndMerge(final Position position) {
        this.position = position;
    }

    public final void submitAndMerge(final OrderParams orderParams,
                                     final String mergeLabel) {
        position.positionEventObs()
                .takeFirst(positonEvent -> positonEvent == PositionEvent.SUBMITTASK_DONE)
                .subscribe(submitEvent -> position.merge(mergeLabel));
        position.submit(orderParams);
    }
}
