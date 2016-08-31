package com.jforex.programming.order.builder;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.function.Consumer;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.event.OrderEventType;

public class SetPriceProcess extends OrderProcess {

    private final IOrder order;
    private final double newPrice;

    public interface SetPriceOption extends CommonOption<SetPriceOption> {
        public SetPriceOption onReject(Consumer<IOrder> rejectAction);

        public SetPriceOption onOK(Consumer<IOrder> okAction);

        public SetPriceProcess build();
    }

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

    public static final SetPriceOption forParams(final IOrder order,
                                                 final double newPrice) {
        return new Builder(checkNotNull(order), checkNotNull(newPrice));
    }

    private static class Builder extends CommonProcess<Builder> implements SetPriceOption {

        private final IOrder order;
        private final double newPrice;

        private Builder(final IOrder order,
                        final double newPrice) {
            this.order = order;
            this.newPrice = newPrice;
        }

        @Override
        public SetPriceOption onReject(final Consumer<IOrder> rejectAction) {
            eventHandlerForType.put(OrderEventType.CHANGE_PRICE_REJECTED, checkNotNull(rejectAction));
            return this;
        }

        @Override
        public SetPriceOption onOK(final Consumer<IOrder> okAction) {
            eventHandlerForType.put(OrderEventType.CHANGED_PRICE, checkNotNull(okAction));
            return this;
        }

        @Override
        public SetPriceProcess build() {
            return new SetPriceProcess(this);
        }
    }
}
