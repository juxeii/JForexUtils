package com.jforex.programming.order;

import static com.google.common.base.Preconditions.checkNotNull;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.command.CloseCommand;
import com.jforex.programming.order.command.SetSLCommand;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.task.BasicTask;

import io.reactivex.Observable;

public class JFOrder {

    private final IOrder nativeOrder;
    private final BasicTask basicTask;

    public JFOrder(final IOrder nativeOrder,
                   final BasicTask basicTask) {
        this.nativeOrder = nativeOrder;
        this.basicTask = basicTask;
    }

    public IOrder nativeOrder() {
        return nativeOrder;
    }

    public Observable<OrderEvent> close(final CloseCommand closeCommand) {
        checkNotNull(closeCommand);

        return basicTask.close(nativeOrder, closeCommand);
    }

    public Observable<OrderEvent> setLabel(final String label) {
        checkNotNull(label);

        return basicTask.setLabel(nativeOrder, label);
    }

    public Observable<OrderEvent> setGoodTillTime(final long newGTT) {
        return basicTask.setGoodTillTime(nativeOrder, newGTT);
    }

    public Observable<OrderEvent> setRequestedAmount(final double newRequestedAmount) {
        return basicTask.setRequestedAmount(nativeOrder, newRequestedAmount);
    }

    public Observable<OrderEvent> setOpenPrice(final double newOpenPrice) {
        return basicTask.setOpenPrice(nativeOrder, newOpenPrice);
    }

    public Observable<OrderEvent> setStopLossPrice(final SetSLCommand setSLCommand) {
        checkNotNull(setSLCommand);

        return basicTask.setStopLossPrice(nativeOrder, setSLCommand);
    }

    public Observable<OrderEvent> setTakeProfitPrice(final double newTP) {
        return basicTask.setTakeProfitPrice(nativeOrder, newTP);
    }
}
