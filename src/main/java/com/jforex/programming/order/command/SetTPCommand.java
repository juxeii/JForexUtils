package com.jforex.programming.order.command;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.jforex.programming.order.OrderStaticUtil.isTPSetTo;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.OrderStaticUtil;
import com.jforex.programming.order.OrderUtilHandler;
import com.jforex.programming.order.call.OrderCallReason;
import com.jforex.programming.order.process.option.TPOption;

import rx.Observable;

public class SetTPCommand extends CommonCommand {

    private final IOrder order;
    private final double newTP;

    public interface Option extends TPOption<Option> {

        public SetTPCommand build();
    }

    private SetTPCommand(final Builder builder,
                         final OrderUtilHandler orderUtilHandler) {
        super(builder);
        order = builder.order;
        newTP = builder.newTP;

        final String commonLog = "TP from " + order.getTakeProfitPrice() + " to " + newTP;
        this.observable = Observable
            .just(order)
            .filter(order -> !isTPSetTo(newTP).test(order))
            .flatMap(order -> changeObservable(orderUtilHandler.callObservable(this),
                                               order,
                                               commonLog));
    }

    public final IOrder order() {
        return order;
    }

    public final double newTP() {
        return newTP;
    }

    public static final Option create(final IOrder order,
                                      final double newTP,
                                      final OrderUtilHandler orderUtilHandler) {
        return new Builder(checkNotNull(order),
                           newTP,
                           orderUtilHandler);
    }

    private static class Builder extends CommonBuilder<Option>
                                 implements Option {

        private final IOrder order;
        private final double newTP;

        private Builder(final IOrder order,
                        final double newTP,
                        final OrderUtilHandler orderUtilHandler) {
            this.order = order;
            this.newTP = newTP;
            this.orderUtilHandler = orderUtilHandler;
            this.callable = OrderStaticUtil.runnableToCallable(() -> order.setTakeProfitPrice(newTP), order);
            this.callReason = OrderCallReason.CHANGE_TP;
        }

        @Override
        public SetTPCommand build() {
            return new SetTPCommand(this, orderUtilHandler);
        }
    }
}
