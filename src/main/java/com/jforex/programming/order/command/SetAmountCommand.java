package com.jforex.programming.order.command;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.function.Function;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.OrderStaticUtil;
import com.jforex.programming.order.call.OrderCallReason;
import com.jforex.programming.order.process.option.AmountOption;

import rx.Completable;

public class SetAmountCommand extends CommonCommand {

    private final IOrder order;
    private final double newAmount;

    public interface Option extends AmountOption<Option> {

        public SetAmountCommand build();
    }

    private SetAmountCommand(final Builder builder) {
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

    public static final Option create(final IOrder order,
                                      final double newAmount,
                                      final Function<SetAmountCommand, Completable> startFunction) {
        return new Builder(checkNotNull(order),
                           newAmount,
                           startFunction);
    }

    public static class Builder extends CommonBuilder<Option>
                                implements Option {

        private final IOrder order;
        private final double newAmount;

        private Builder(final IOrder order,
                        final double newAmount,
                        final Function<SetAmountCommand, Completable> startFunction) {
            this.order = order;
            this.newAmount = newAmount;
            this.callable = OrderStaticUtil.runnableToCallable(() -> order.setRequestedAmount(newAmount), order);
            this.callReason = OrderCallReason.CHANGE_AMOUNT;
            this.startFunction = startFunction;
        }

        public SetAmountCommand build() {
            return new SetAmountCommand(this);
        }
    }
}
