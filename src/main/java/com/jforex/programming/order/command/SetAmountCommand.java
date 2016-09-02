package com.jforex.programming.order.command;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.jforex.programming.order.OrderStaticUtil.isAmountSetTo;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.OrderStaticUtil;
import com.jforex.programming.order.OrderUtilHandler;
import com.jforex.programming.order.call.OrderCallReason;
import com.jforex.programming.order.process.option.AmountOption;

import rx.Observable;

public class SetAmountCommand extends CommonCommand {

    private final IOrder order;
    private final double newAmount;

    public interface Option extends AmountOption<Option> {

        public SetAmountCommand build();
    }

    private SetAmountCommand(final Builder builder,
                             final OrderUtilHandler orderUtilHandler) {
        super(builder);
        order = builder.order;
        newAmount = builder.newAmount;

        final String commonLog = "amount from " + order.getRequestedAmount() + " to " + newAmount;
        this.observable = Observable
            .just(order)
            .filter(order -> !isAmountSetTo(newAmount).test(order))
            .flatMap(order -> changeObservable(orderUtilHandler.callObservable(this),
                                               order,
                                               commonLog));
    }

    public final IOrder order() {
        return order;
    }

    public final double newAmount() {
        return newAmount;
    }

    public static final Option create(final IOrder order,
                                      final double newAmount,
                                      final OrderUtilHandler orderUtilHandler) {
        return new Builder(checkNotNull(order),
                           newAmount,
                           orderUtilHandler);
    }

    public static class Builder extends CommonBuilder<Option>
                                implements Option {

        private final IOrder order;
        private final double newAmount;

        private Builder(final IOrder order,
                        final double newAmount,
                        final OrderUtilHandler orderUtilHandler) {
            this.order = order;
            this.newAmount = newAmount;
            this.orderUtilHandler = orderUtilHandler;
            this.callable = OrderStaticUtil.runnableToCallable(() -> order.setRequestedAmount(newAmount), order);
            this.callReason = OrderCallReason.CHANGE_AMOUNT;
        }

        public SetAmountCommand build() {
            return new SetAmountCommand(this, orderUtilHandler);
        }
    }
}
