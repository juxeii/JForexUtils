package com.jforex.programming.order.command;

import static com.google.common.base.Preconditions.checkNotNull;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.process.option.TPOption;

import rx.Observable;

public class SetTPCommand extends CommonCommand {

    private final IOrder order;
    private final double newTP;

    public interface Option extends TPOption<Option> {

        public SetTPCommand build();
    }

    private SetTPCommand(final Builder builder) {
        super(builder);
        order = builder.order;
        newTP = builder.newTP;
    }

    public final IOrder order() {
        return order;
    }

    public final double newTP() {
        return newTP;
    }

    public static final Option create(final IOrder order,
                                      final double newTP,
                                      final Observable<OrderEvent> observable) {
        return new Builder(checkNotNull(order),
                           newTP,
                           observable);
    }

    private static class Builder extends CommonBuilder<Option>
                                 implements Option {

        private final IOrder order;
        private final double newTP;

        private Builder(final IOrder order,
                        final double newTP,
                        final Observable<OrderEvent> observable) {
            this.order = order;
            this.newTP = newTP;
            final String commonLog = "TP from " + order.getTakeProfitPrice() + " to " + newTP;
            this.observable = changeObservable(observable, order, commonLog);
        }

        @Override
        public SetTPCommand build() {
            return new SetTPCommand(this);
        }
    }
}
