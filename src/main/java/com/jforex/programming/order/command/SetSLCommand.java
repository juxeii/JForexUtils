package com.jforex.programming.order.command;

import static com.google.common.base.Preconditions.checkNotNull;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.process.option.SLOption;

import rx.Observable;

public class SetSLCommand extends CommonCommand {

    private final IOrder order;
    private final double newSL;

    public interface Option extends SLOption<Option> {

        public SetSLCommand build();
    }

    private SetSLCommand(final Builder builder) {
        super(builder);
        order = builder.order;
        newSL = builder.newSL;
    }

    public final IOrder order() {
        return order;
    }

    public final double newSL() {
        return newSL;
    }

    public static final Option create(final IOrder order,
                                      final double newSL,
                                      final Observable<OrderEvent> observable) {
        return new Builder(checkNotNull(order),
                           newSL,
                           observable);
    }

    private static class Builder extends CommonBuilder<Option>
                                 implements Option {

        private final IOrder order;
        private final double newSL;

        private Builder(final IOrder order,
                        final double newSL,
                        final Observable<OrderEvent> observable) {
            this.order = order;
            this.newSL = newSL;
            final String commonLog = "SL from " + order.getStopLossPrice() + " to " + newSL;
            this.observable = changeObservable(observable, order, commonLog);
        }

        @Override
        public SetSLCommand build() {
            return new SetSLCommand(this);
        }
    }
}
