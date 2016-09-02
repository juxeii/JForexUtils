package com.jforex.programming.order.command;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.jforex.programming.order.OrderStaticUtil.isOpenPriceSetTo;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.OrderStaticUtil;
import com.jforex.programming.order.OrderUtilHandler;
import com.jforex.programming.order.call.OrderCallReason;
import com.jforex.programming.order.process.option.OpenPriceOption;

import rx.Observable;

public class SetOpenPriceCommand extends CommonCommand {

    private final IOrder order;
    private final double newPrice;

    public interface Option extends OpenPriceOption<Option> {

        public SetOpenPriceCommand build();
    }

    private SetOpenPriceCommand(final Builder builder,
                                final OrderUtilHandler orderUtilHandler) {
        super(builder);
        order = builder.order;
        newPrice = builder.newPrice;

        final String commonLog = "open price from " + order.getOpenPrice() + " to " + newPrice;
        this.observable = Observable
            .just(order)
            .filter(order -> !isOpenPriceSetTo(newPrice).test(order))
            .flatMap(order -> changeObservable(orderUtilHandler.callObservable(this),
                                               order,
                                               commonLog));
    }

    public final IOrder order() {
        return order;
    }

    public final double newOpenPrice() {
        return newPrice;
    }

    public static final Option create(final IOrder order,
                                      final double newPrice,
                                      final OrderUtilHandler orderUtilHandler) {
        return new Builder(checkNotNull(order),
                           newPrice,
                           orderUtilHandler);
    }

    private static class Builder extends CommonBuilder<Option>
                                 implements Option {

        private final IOrder order;
        private final double newPrice;

        private Builder(final IOrder order,
                        final double newPrice,
                        final OrderUtilHandler orderUtilHandler) {
            this.order = order;
            this.newPrice = newPrice;
            this.orderUtilHandler = orderUtilHandler;
            this.callable = OrderStaticUtil.runnableToCallable(() -> order.setOpenPrice(newPrice), order);
            this.callReason = OrderCallReason.CHANGE_PRICE;
        }

        @Override
        public SetOpenPriceCommand build() {
            return new SetOpenPriceCommand(this, orderUtilHandler);
        }
    }
}
