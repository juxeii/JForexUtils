package com.jforex.programming.order.command;

import static com.google.common.base.Preconditions.checkNotNull;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.process.option.AmountOption;

import rx.Observable;

public class SetAmountCommand extends CommonCommand {

    private final IOrder order;
    private final double newAmount;

    public interface Option extends AmountOption<Option> {

        public SetAmountCommand build();
    }

    private SetAmountCommand(final Builder builder) {
        super(builder);
        order = builder.order;
        newAmount = builder.newAmount;
    }

    public final IOrder order() {
        return order;
    }

    public final double newAmount() {
        return newAmount;
    }

    public static final Option create(final IOrder order,
                                      final double newAmount,
                                      final Observable<OrderEvent> observable) {
        return new Builder(checkNotNull(order),
                           newAmount,
                           observable);
    }

    public static class Builder extends CommonBuilder<Option>
                                implements Option {

        private final IOrder order;
        private final double newAmount;

        private Builder(final IOrder order,
                        final double newAmount,
                        final Observable<OrderEvent> observable) {
            this.order = order;
            this.newAmount = newAmount;
            final String commonLog = "amount from " + order.getRequestedAmount() + " to " + newAmount;
            this.observable = changeObservable(observable, order, commonLog);
        }

        public SetAmountCommand build() {
            return new SetAmountCommand(this);
        }
    }
}
