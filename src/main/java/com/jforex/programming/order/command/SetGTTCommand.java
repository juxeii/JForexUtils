package com.jforex.programming.order.command;

import static com.google.common.base.Preconditions.checkNotNull;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.process.option.GTTOption;

import rx.Observable;

public class SetGTTCommand extends CommonCommand {

    private final IOrder order;
    private final long newGTT;

    public interface Option extends GTTOption<Option> {

        public SetGTTCommand build();
    }

    private SetGTTCommand(final Builder builder) {
        super(builder);
        order = builder.order;
        newGTT = builder.newGTT;
    }

    public final IOrder order() {
        return order;
    }

    public final long newGTT() {
        return newGTT;
    }

    public static final Option create(final IOrder order,
                                      final long newGTT,
                                      final Observable<OrderEvent> observable) {
        return new Builder(checkNotNull(order),
                           newGTT,
                           observable);
    }

    private static class Builder extends CommonBuilder<Option>
                                 implements Option {

        private final IOrder order;
        private final long newGTT;

        private Builder(final IOrder order,
                        final long newGTT,
                        final Observable<OrderEvent> observable) {
            this.order = order;
            this.newGTT = newGTT;
            final String commonLog = "GTT from " + order.getGoodTillTime() + " to " + newGTT;
            this.observable = changeObservable(observable, order, commonLog);
        }

        @Override
        public SetGTTCommand build() {
            return new SetGTTCommand(this);
        }
    }
}
