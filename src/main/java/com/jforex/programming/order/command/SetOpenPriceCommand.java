package com.jforex.programming.order.command;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.function.Function;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.OrderStaticUtil;
import com.jforex.programming.order.call.OrderCallReason;
import com.jforex.programming.order.process.option.OpenPriceOption;

import rx.Completable;

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

    public final double newPrice() {
        return newPrice;
    }

    public static final Option create(final IOrder order,
                                      final double newPrice,
                                      final Function<SetOpenPriceCommand, Completable> startFunction) {
        return new Builder(checkNotNull(order),
                           newPrice,
                           startFunction);
    }

    private static class Builder extends CommonBuilder<Option>
                                 implements Option {

        private final IOrder order;
        private final double newPrice;

        private Builder(final IOrder order,
                        final double newPrice,
                        final Function<SetOpenPriceCommand, Completable> startFunction) {
            this.order = order;
            this.newPrice = newPrice;
            this.callable = OrderStaticUtil.runnableToCallable(() -> order.setOpenPrice(newPrice), order);
            this.callReason = OrderCallReason.CHANGE_PRICE;
            this.startFunction = startFunction;
        }

        @Override
        public SetOpenPriceCommand build() {
            return new SetOpenPriceCommand(this);
        }
    }
}