package com.jforex.programming.order.process;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.function.Consumer;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.event.OrderEventType;
import com.jforex.programming.order.process.option.OpenPriceOption;

public class SetOpenPriceProcess extends CommonProcess {

    private final IOrder order;
    private final double newPrice;

    private SetOpenPriceProcess(final Builder builder) {
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

    public static final OpenPriceOption forParams(final IOrder order,
                                                  final double newPrice) {
        return new Builder(checkNotNull(order), checkNotNull(newPrice));
    }

    private static class Builder extends CommonBuilder<OpenPriceOption>
                                 implements OpenPriceOption {

        private final IOrder order;
        private final double newPrice;

        private Builder(final IOrder order,
                        final double newPrice) {
            this.order = order;
            this.newPrice = newPrice;
        }

        public OpenPriceOption onOpenPriceReject(final Consumer<IOrder> rejectAction) {
            eventHandlerForType.put(OrderEventType.CHANGE_PRICE_REJECTED, checkNotNull(rejectAction));
            return this;
        }

        public OpenPriceOption onOpenPriceChange(final Consumer<IOrder> doneAction) {
            eventHandlerForType.put(OrderEventType.CHANGED_PRICE, checkNotNull(doneAction));
            return this;
        }

        @Override
        public SetOpenPriceProcess build() {
            return new SetOpenPriceProcess(this);
        }
    }
}
