package com.jforex.programming.order.command;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.function.Consumer;
import java.util.function.Function;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.OrderStaticUtil;
import com.jforex.programming.order.call.OrderCallReason;
import com.jforex.programming.order.event.OrderEventType;
import com.jforex.programming.order.process.option.OpenPriceOption;

import rx.Completable;

public class SetOpenPriceCommand extends CommonCommand {

    private final IOrder order;
    private final double newPrice;

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

    public static final OpenPriceOption create(final IOrder order,
                                               final double newPrice,
                                               final Function<SetOpenPriceCommand, Completable> startFunction) {
        return new Builder(checkNotNull(order),
                           newPrice,
                           startFunction);
    }

    private static class Builder extends CommonBuilder<OpenPriceOption>
                                 implements OpenPriceOption {

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
        public OpenPriceOption doOnSetOpenPriceReject(final Consumer<IOrder> rejectAction) {
            eventHandlerForType.put(OrderEventType.CHANGE_PRICE_REJECTED, checkNotNull(rejectAction));
            return this;
        }

        @Override
        public OpenPriceOption doOnSetOpenPrice(final Consumer<IOrder> doneAction) {
            eventHandlerForType.put(OrderEventType.CHANGED_PRICE, checkNotNull(doneAction));
            return this;
        }

        @Override
        public SetOpenPriceCommand build() {
            return new SetOpenPriceCommand(this);
        }
    }
}
