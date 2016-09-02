package com.jforex.programming.order.command;

import static com.google.common.base.Preconditions.checkNotNull;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.process.option.OpenPriceOption;

import rx.Observable;

public class SetOpenPriceCommand extends CommonCommand {

    private final IOrder order;
    private final double newPrice;

    public interface Option extends OpenPriceOption<Option> {

        public SetOpenPriceCommand build();
    }

    private SetOpenPriceCommand(final Builder builder) {
        super(builder);
        order = builder.order;
        newPrice = builder.newPrice;
    }

    public final IOrder order() {
        return order;
    }

    public final double newOpenPrice() {
        return newPrice;
    }

    public static final Option create(final IOrder order,
                                      final double newPrice,
                                      final Observable<OrderEvent> observable) {
        return new Builder(checkNotNull(order),
                           newPrice,
                           observable);
    }

    private static class Builder extends CommonBuilder<Option>
                                 implements Option {

        private final IOrder order;
        private final double newPrice;

        private Builder(final IOrder order,
                        final double newPrice,
                        final Observable<OrderEvent> observable) {
            this.order = order;
            this.newPrice = newPrice;
            final String commonLog = "open price from " + order.getOpenPrice() + " to " + newPrice;
            this.observable = changeObservable(observable, order, commonLog);
        }

        @Override
        public SetOpenPriceCommand build() {
            return new SetOpenPriceCommand(this);
        }
    }
}
