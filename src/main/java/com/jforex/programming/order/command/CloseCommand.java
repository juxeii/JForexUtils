package com.jforex.programming.order.command;

import static com.google.common.base.Preconditions.checkNotNull;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.process.option.CloseOption;

import rx.Observable;

public class CloseCommand extends CommonCommand {

    private final IOrder orderToClose;

    public interface Option extends CloseOption<Option> {

        public CloseCommand build();
    }

    private CloseCommand(final Builder builder) {
        super(builder);
        orderToClose = builder.orderToClose;
    }

    public final IOrder orderToClose() {
        return orderToClose;
    }

    public static final Option create(final IOrder orderToClose,
                                      final Observable<OrderEvent> observable) {
        return new Builder(checkNotNull(orderToClose), observable);
    }

    public static class Builder extends CommonBuilder<Option>
                                implements Option {

        private final IOrder orderToClose;

        private Builder(final IOrder orderToClose,
                        final Observable<OrderEvent> observable) {
            this.orderToClose = orderToClose;
            final String commonLog = "state from " + orderToClose.getState() + " to " + IOrder.State.CLOSED;
            this.observable = changeObservable(observable, orderToClose, commonLog);
        }

        @Override
        public CloseCommand build() {
            return new CloseCommand(this);
        }
    }
}
