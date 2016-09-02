package com.jforex.programming.order.command;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.jforex.programming.order.OrderStaticUtil.isSLSetTo;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.OrderStaticUtil;
import com.jforex.programming.order.OrderUtilHandler;
import com.jforex.programming.order.call.OrderCallReason;
import com.jforex.programming.order.process.option.SLOption;

import rx.Observable;

public class SetSLCommand extends CommonCommand {

    private final IOrder order;
    private final double newSL;

    public interface Option extends SLOption<Option> {

        public SetSLCommand build();
    }

    private SetSLCommand(final Builder builder,
                         final OrderUtilHandler orderUtilHandler) {
        super(builder);
        order = builder.order;
        newSL = builder.newSL;

        final String commonLog = "SL from " + order.getStopLossPrice() + " to " + newSL;
        this.observable = Observable
            .just(order)
            .filter(order -> !isSLSetTo(newSL).test(order))
            .flatMap(order -> changeObservable(orderUtilHandler.callObservable(this),
                                               order,
                                               commonLog));
    }

    public static final Option create(final IOrder order,
                                      final double newSL,
                                      final OrderUtilHandler orderUtilHandler) {
        return new Builder(checkNotNull(order),
                           newSL,
                           orderUtilHandler);
    }

    private static class Builder extends CommonBuilder<Option>
                                 implements Option {

        private final IOrder order;
        private final double newSL;

        private Builder(final IOrder order,
                        final double newSL,
                        final OrderUtilHandler orderUtilHandler) {
            this.order = order;
            this.newSL = newSL;
            this.orderUtilHandler = orderUtilHandler;
            this.callable = OrderStaticUtil.runnableToCallable(() -> order.setStopLossPrice(newSL), order);
            this.callReason = OrderCallReason.CHANGE_SL;
        }

        @Override
        public SetSLCommand build() {
            return new SetSLCommand(this, orderUtilHandler);
        }
    }
}
