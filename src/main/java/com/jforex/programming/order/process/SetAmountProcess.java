package com.jforex.programming.order.process;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.function.Consumer;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.event.OrderEventType;
import com.jforex.programming.order.process.option.AmountOption;

public class SetAmountProcess extends CommonProcess {

    private final IOrder order;
    private final double newAmount;

    public interface Option extends AmountOption<Option> {

        public SetAmountProcess build();
    }

    private SetAmountProcess(final Builder builder) {
        super(builder);
        order = builder.order;
        newAmount = builder.newAmount;
    }

    public final IOrder order() {
        return order;
    }

    public final double newAmount() {
        return newAmount;
    }

    public static final Option forParams(final IOrder order,
                                         final double newAmount) {
        return new Builder(checkNotNull(order), checkNotNull(newAmount));
    }

    public static class Builder extends CommonBuilder<Option>
                                implements Option {

        private final IOrder order;
        private final double newAmount;

        private Builder(final IOrder order,
                        final double newAmount) {
            this.order = order;
            this.newAmount = newAmount;
        }

        public Option onAmountReject(final Consumer<IOrder> rejectAction) {
            eventHandlerForType.put(OrderEventType.CHANGE_AMOUNT_REJECTED, checkNotNull(rejectAction));
            return this;
        }

        public Option onAmountChange(final Consumer<IOrder> okAction) {
            eventHandlerForType.put(OrderEventType.CHANGED_AMOUNT, checkNotNull(okAction));
            return this;
        }

        public SetAmountProcess build() {
            return new SetAmountProcess(this);
        }
    }
}
