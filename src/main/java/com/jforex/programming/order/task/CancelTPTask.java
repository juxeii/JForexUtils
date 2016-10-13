package com.jforex.programming.order.task;

import java.util.Collection;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.command.MergeCommand;
import com.jforex.programming.order.event.OrderEvent;

import io.reactivex.Observable;

public class CancelTPTask {

    private final BatchChangeTask orderChangeBatch;

    public CancelTPTask(final BatchChangeTask orderChangeBatch) {
        this.orderChangeBatch = orderChangeBatch;
    }

    public Observable<OrderEvent> observe(final Collection<IOrder> ordersForCancelTP,
                                          final MergeCommand mergeCommand) {
        return Observable.defer(() -> orderChangeBatch.cancelTP(ordersForCancelTP,
                                                                mergeCommand.orderCancelTPMode(),
                                                                mergeCommand::orderCancelTPComposer));
    }
}
