package com.jforex.programming.order.process;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.function.Consumer;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.event.OrderEventType;

public class SetPriceProcess extends OrderProcess {

    private final IOrder order;
    private final double newPrice;

    private SetPriceProcess(final Builder builder) {
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

    public static final ChangeOption<SetPriceProcess> forParams(final IOrder order,
                                                                final double newPrice) {
        return new Builder(checkNotNull(order), checkNotNull(newPrice));
    }

    private static class Builder extends CommonBuilder<Builder> implements ChangeOption<SetPriceProcess> {

        private final IOrder order;
        private final double newPrice;

        private Builder(final IOrder order,
                        final double newPrice) {
            this.order = order;
            this.newPrice = newPrice;
        }

        @Override
        public ChangeOption<SetPriceProcess> onReject(final Consumer<IOrder> rejectAction) {
            eventHandlerForType.put(OrderEventType.CHANGE_PRICE_REJECTED, checkNotNull(rejectAction));
            return this;
        }

        @Override
        public ChangeOption<SetPriceProcess> onDone(final Consumer<IOrder> okAction) {
            eventHandlerForType.put(OrderEventType.CHANGED_PRICE, checkNotNull(okAction));
            return this;
        }

        @Override
        public SetPriceProcess build() {
            return new SetPriceProcess(this);
        }
    }
}
